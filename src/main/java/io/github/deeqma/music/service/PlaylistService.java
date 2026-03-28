package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.*;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.PlaylistException;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Playlist;
import io.github.deeqma.music.model.PlaylistVisibility;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.model.User;
import io.github.deeqma.music.repository.LikedSongRepository;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.SongRepository;
import io.github.deeqma.music.repository.UserRepository;
import io.github.deeqma.music.utils.SongSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final SongService songService;
    private final SongRepository songRepository;
    private final LikedSongRepository likedSongRepository;
    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository, SongService songService, SongRepository songRepository, LikedSongRepository likedSongRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songService = songService;
        this.songRepository = songRepository;
        this.likedSongRepository = likedSongRepository;
    }

    public PlaylistDto createPlaylist(UUID userId, CreateOrUpdatePlaylistDto dto) {

        log.info("createPlaylist: creating playlist '{}' for user {}", dto.getPlaylistName(), userId);

        User user = findUserById(userId);

        if (playlistRepository.countByOwnerId(userId) >= 30) {
            throw new PlaylistException(ErrorType.PLAYLIST_LIMIT_REACHED, "Playlist limit of 30 reached");
        }

        String slug = generateSlug(dto.getPlaylistName());

        if (playlistRepository.existsByNameAndOwnerId(dto.getPlaylistName(), userId)) {
            throw new PlaylistException(ErrorType.PLAYLIST_ALREADY_EXISTS, "Playlist with this name already exists");
        }

        Playlist playlist = new Playlist();
        playlist.setName(dto.getPlaylistName());
        playlist.setDescription(dto.getDescription());
        playlist.setSlug(slug);
        playlist.setVisibility(dto.getVisibility() != null ? dto.getVisibility() : PlaylistVisibility.PRIVATE);
        playlist.setOwner(user);

        Playlist saved = playlistRepository.save(playlist);
        log.info("createPlaylist: created playlist ID {} for user {}", saved.getId(), userId);
        return toDto(saved, userId);
    }

    @Transactional(readOnly = true)
    public List<PlaylistDto> getAllPlaylists(UUID userId) {

        log.info("getAllPlaylists: fetching playlists for user {}", userId);

        List<Playlist> playlists = playlistRepository.findAllPublicAndOwnedBy(userId);
        log.info("getAllPlaylists: found {} playlists for user {}", playlists.size(), userId);

        List<PlaylistDto> result = new ArrayList<>();
        for (Playlist playlist : playlists) {
            result.add(toDto(playlist, userId));
        }
        return result;
    }

    public PlaylistDetailsDto getPlaylistById(Long playlistId, UUID userId, SongFilterDto filterDto, int page, int pageSize) {

        log.info("getPlaylistById: fetching playlist ID {} for user {}", playlistId, userId);

        Playlist playlist = findPlaylistById(playlistId);

        if (playlist.getVisibility() != PlaylistVisibility.PUBLIC) {
            validateOwnership(playlist, userId);
        }

        Set<Long> songIds = playlist.getSongs().stream()
                .map(Song::getId)
                .collect(Collectors.toSet());

        if (songIds.isEmpty()) {
            PlaylistDetailsDto dto = toDetailsDto(playlist, userId);
            dto.setSongDtos(new ArrayList<>());
            dto.setTotalDurationSeconds(0);
            return dto;
        }

        Specification<Song> inPlaylist = (root, _, _) -> root.get("id").in(songIds);
        Specification<Song> spec = SongSpecification.filter(filterDto).and(inPlaylist);

        List<Song> songs = songRepository.findAll(spec, PageRequest.of(page, pageSize)).getContent();

        int totalDurationSeconds = songs.stream()
                .mapToInt(Song::getDurationSeconds)
                .sum();

        Set<Long> likedSongIds = likedSongRepository.findSongIdsByUserId(userId);
        List<SongDto> songDtos = songs.stream()
                .map(song -> songService.toDto(song, likedSongIds))
                .toList();

        PlaylistDetailsDto dto = toDetailsDto(playlist, userId);
        dto.setSongDtos(songDtos);
        dto.setTotalDurationSeconds(totalDurationSeconds);
        return dto;
    }


    public PlaylistDetailsDto updatePlaylist(Long playlistId, UUID userId, CreateOrUpdatePlaylistDto dto) {

        log.info("updatePlaylist: updating playlist ID {} for user {}", playlistId, userId);

        Playlist playlist = findPlaylistById(playlistId);
        checkOwnership(playlist, userId, "You don't own this playlist and cannot edit it");

        String newName = dto.getPlaylistName();

        if (!playlist.getName().equals(newName) && playlistRepository.existsByNameAndOwnerId(newName, userId)) {
            throw new PlaylistException(ErrorType.PLAYLIST_ALREADY_EXISTS, "Playlist with this name already exists");
        }

        playlist.setName(newName);
        playlist.setSlug(generateSlug(newName));

        if (dto.getDescription() != null) {
            playlist.setDescription(dto.getDescription());
        }

        Playlist saved = playlistRepository.save(playlist);
        log.info("updatePlaylist: updated playlist ID {} for user {}", playlistId, userId);
        return toDetailsDto(saved, userId);
    }

    public void deletePlaylist(Long playlistId, UUID userId) {

        log.info("deletePlaylist: deleting playlist ID {} for user {}", playlistId, userId);

        Playlist playlist = findPlaylistById(playlistId);
        checkOwnership(playlist, userId, "You don't own this playlist and cannot delete it");

        playlistRepository.delete(playlist);
        log.info("deletePlaylist: deleted playlist ID {} for user {}", playlistId, userId);
    }

    @Transactional
    public PlaylistDetailsDto addSongToPlaylist(Long playlistId, Long songId, UUID userId) {

        log.info("addSongToPlaylist: adding song {} to playlist {}", songId, playlistId);

        Playlist playlist = findPlaylistById(playlistId);
        checkOwnership(playlist, userId, "You don't own this playlist and cannot add songs to it");

        Song song = songRepository.findById(songId).orElseThrow(
                () -> new SongException(ErrorType.SONG_NOT_FOUND, "Song not found")
        );

        if (playlist.getSongs().contains(song)) {
            throw new PlaylistException(ErrorType.SONG_ALREADY_IN_PLAYLIST, "Song already exists in playlist");
        }

        playlist.getSongs().add(song);
        Playlist saved = playlistRepository.save(playlist);
        log.info("addSongToPlaylist: song {} added to playlist {}", songId, playlistId);
        return toDetailsDto(saved, userId);
    }

    @Transactional
    public PlaylistDetailsDto removeSongFromPlaylist(Long playlistId, Long songId, UUID userId) {

        log.info("removeSongFromPlaylist: removing song {} from playlist {}", songId, playlistId);

        Playlist playlist = findPlaylistById(playlistId);
        checkOwnership(playlist, userId, "You don't own this playlist and cannot remove songs from it");

        Song song = songRepository.findById(songId).orElseThrow(
                () -> new SongException(ErrorType.SONG_NOT_FOUND, "Song not found")
        );

        if (!playlist.getSongs().contains(song)) {
            throw new PlaylistException(ErrorType.SONG_NOT_IN_PLAYLIST, "Song not in playlist");
        }

        playlist.getSongs().remove(song);
        Playlist saved = playlistRepository.save(playlist);
        log.info("removeSongFromPlaylist: song {} removed from playlist {}", songId, playlistId);
        return toDetailsDto(saved, userId);
    }

    public PlaylistDetailsDto generateShareToken(Long playlistId, UUID userId) {

        log.info("generateShareToken: generating share token for playlist ID {}", playlistId);

        Playlist playlist = findPlaylistById(playlistId);
        checkOwnership(playlist, userId, "You don't own this playlist and cannot generate a share token");

        if (playlist.getVisibility() == PlaylistVisibility.PUBLIC) {
            throw new PlaylistException(ErrorType.PLAYLIST_SHARE_NOT_ALLOWED, "Public playlists cannot generate a share link");
        }

        if (StringUtils.hasText(playlist.getShareToken())) {
            log.info("generateShareToken: share token already exists for playlist ID {}", playlistId);
            throw new PlaylistException(ErrorType.PLAYLIST_SHARE_ALREADY_EXISTS, "Share link already exists");
        }

        playlist.setShareToken(generateUniqueToken());
        Playlist saved = playlistRepository.save(playlist);
        log.info("generateShareToken: share token generated for playlist ID {}", playlistId);
        return toDetailsDto(saved, userId);
    }

    public List<SongDto> searchSongsInPlaylist(Long playlistId, String query, String shareToken,
                                               UUID userId, int page, int pageSize) {

        log.info("searchSongsInPlaylist: searching in playlist ID {}", playlistId);

        Playlist playlist = findPlaylistById(playlistId);

        if (playlist.getVisibility() == PlaylistVisibility.PRIVATE) {
            boolean hasShareToken = StringUtils.hasText(shareToken) && shareToken.equals(playlist.getShareToken());
            if (!hasShareToken) {
                checkOwnership(playlist, userId, "You don't own this playlist and cannot search its songs");
            }
        }

        Set<Long> songIds = playlist.getSongs().stream()
                .map(Song::getId)
                .collect(Collectors.toSet());

        Specification<Song> spec = SongSpecification.search(query)
                .and((root, _, _) -> root.get("id").in(songIds));

        List<Song> songs = songRepository.findAll(spec, PageRequest.of(page, pageSize)).getContent();
        log.info("searchSongsInPlaylist: found {} songs in playlist ID {}", songs.size(), playlistId);

        Set<Long> likedSongIds = likedSongRepository.findSongIdsByUserId(userId);
        return songs.stream()
                .map(song -> songService.toDto(song, likedSongIds))
                .toList();
    }

    public PlaylistDetailsDto toggleVisibility(Long playlistId, boolean isPrivate, UUID userId) {

        log.info("toggleVisibility: setting playlist ID {} to {}", playlistId, isPrivate ? "PRIVATE" : "PUBLIC");

        Playlist playlist = findPlaylistById(playlistId);
        checkOwnership(playlist, userId, "You don't own this playlist and cannot change its visibility");

        if (isPrivate) {
            playlist.setVisibility(PlaylistVisibility.PRIVATE);
            log.info("toggleVisibility: playlist ID {} set to PRIVATE", playlistId);
        } else {
            playlist.setVisibility(PlaylistVisibility.PUBLIC);
            playlist.setShareToken(null);
            log.info("toggleVisibility: playlist ID {} set to PUBLIC, share token removed", playlistId);
        }

        return toDetailsDto(playlistRepository.save(playlist), userId);
    }

    private void checkOwnership(Playlist playlist, UUID userId, String message) {
        if (playlist.getOwner() == null || !playlist.getOwner().getId().equals(userId)) {
            throw new PlaylistException(ErrorType.PLAYLIST_NOT_OWNED, message);
        }
    }

    private void validateOwnership(Playlist playlist, UUID userId) {
        if (playlist.getOwner() == null || !playlist.getOwner().getId().equals(userId)) {
            throw new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found");
        }
    }

    private boolean isOwner(Playlist playlist, UUID userId) {
        return playlist.getOwner() != null && playlist.getOwner().getId().equals(userId);
    }

    private String generateSlug(String name) {
        return name.toLowerCase().trim().replaceAll("\\s+", "-");
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString().replace("-", "").substring(0, 11);
        } while (playlistRepository.existsByShareToken(token));
        return token;
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId).orElseThrow(
                () -> new PlaylistException(ErrorType.USER_NOT_FOUND, "User not found")
        );
    }

    private Playlist findPlaylistById(Long id) {
        return playlistRepository.findById(id).orElseThrow(
                () -> new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found")
        );
    }

    private PlaylistDto toDto(Playlist playlist, UUID userId) {
        PlaylistDto dto = new PlaylistDto();
        dto.setPlaylistId(playlist.getId());
        dto.setPlaylistName(playlist.getName());
        dto.setSlug(playlist.getSlug());
        dto.setOwner(isOwner(playlist, userId));
        return dto;
    }

    private PlaylistDetailsDto toDetailsDto(Playlist playlist, UUID userId) {
        PlaylistDetailsDto dto = new PlaylistDetailsDto();
        dto.setPlaylistId(playlist.getId());
        dto.setPlaylistName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setSlug(playlist.getSlug());
        dto.setVisibility(playlist.getVisibility());
        dto.setTotalSongs(playlist.getSongs().size());
        dto.setOwner(isOwner(playlist, userId));
        if (playlist.getVisibility() == PlaylistVisibility.PRIVATE) {
            dto.setShareToken(playlist.getShareToken());
        }
        return dto;
    }

}