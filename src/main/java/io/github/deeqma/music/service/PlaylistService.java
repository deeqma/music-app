package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.CreateOrUpdatePlaylistDto;
import io.github.deeqma.music.dto.PlaylistDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.PlaylistException;
import io.github.deeqma.music.model.Playlist;
import io.github.deeqma.music.model.PlaylistVisibility;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.model.User;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.SongRepository;
import io.github.deeqma.music.repository.UserRepository;
import io.github.deeqma.music.utils.SongSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
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

    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository, SongService songService, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.songService = songService;
        this.songRepository = songRepository;
    }

    public PlaylistDto createPlaylist(UUID userId, CreateOrUpdatePlaylistDto dto) {

        log.info("createPlaylist: creating playlist '{}' for user {}", dto.getPlaylistName(), userId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new PlaylistException(ErrorType.USER_NOT_FOUND, "User not found")
        );

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
        return toDto(saved);
    }

    public List<PlaylistDto> getAllPlaylists(UUID userId) {

        log.info("getAllPlaylists: fetching playlists for user {}", userId);

        findUserById(userId);

        List<Playlist> playlists = playlistRepository.findAllByOwnerId(userId);
        log.info("getAllPlaylists: found {} playlists for user {}", playlists.size(), userId);

        List<PlaylistDto> result = new ArrayList<>();
        for (Playlist playlist : playlists) {
            result.add(toDto(playlist));
        }
        return result;
    }

    public PlaylistDto getPlaylistById(Long playlistId, UUID userId, SongFilterDto filterDto, int page, int pageSize) {

        log.info("getPlaylistById: fetching playlist ID {} for user {}", playlistId, userId);

        Playlist playlist = findPlaylistById(playlistId);

        if (playlist.getVisibility() != PlaylistVisibility.PUBLIC) {
            validateOwnership(playlist, userId);
        }

        Set<Long> songIds = playlist.getSongs().stream()
                .map(Song::getId)
                .collect(Collectors.toSet());

        Specification<Song> spec = SongSpecification.filter(filterDto)
                .and((root, _, _) -> root.get("id").in(songIds));

        List<Song> songs = songRepository.findAll(spec, PageRequest.of(page, pageSize)).getContent();

        PlaylistDto dto = toDto(playlist);
        dto.setSongDtos(songs.stream().map(songService::toDto).toList());
        return dto;
    }

    public PlaylistDto generateShareToken(Long playlistId, UUID userId) {

        log.info("generateShareToken: generating share token for playlist ID {}", playlistId);

        Playlist playlist = findPlaylistById(playlistId);
        validateOwnership(playlist, userId);

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
        return toDto(saved);
    }

    public List<SongDto> searchSongsInPlaylist(Long playlistId, String query, String shareToken,
                                               UUID userId, int page, int pageSize) {

        log.info("searchSongsInPlaylist: searching in playlist ID {}", playlistId);

        Playlist playlist = findPlaylistById(playlistId);

        if (playlist.getVisibility() == PlaylistVisibility.PRIVATE) {
            boolean hasShareToken = StringUtils.hasText(shareToken) && shareToken.equals(playlist.getShareToken());
            if (!hasShareToken) {
                validateOwnership(playlist, userId);
            }
        }

        Set<Long> songIds = playlist.getSongs().stream()
                .map(Song::getId)
                .collect(Collectors.toSet());

        Specification<Song> spec = SongSpecification.search(query)
                .and((root, _, _) -> root.get("id").in(songIds));

        List<Song> songs = songRepository.findAll(spec, PageRequest.of(page, pageSize)).getContent();
        log.info("searchSongsInPlaylist: found {} songs in playlist ID {}", songs.size(), playlistId);

        return songs.stream().map(songService::toDto).toList();

    }

    public PlaylistDto toggleVisibility(Long playlistId, boolean isPrivate, UUID userId) {

        log.info("toggleVisibility: setting playlist ID {} to {}", playlistId, isPrivate ? "PRIVATE" : "PUBLIC");

        Playlist playlist = findPlaylistById(playlistId);
        validateOwnership(playlist, userId);

        if (isPrivate) {
            playlist.setVisibility(PlaylistVisibility.PRIVATE);
            log.info("toggleVisibility: playlist ID {} set to PRIVATE", playlistId);
        } else {
            playlist.setVisibility(PlaylistVisibility.PUBLIC);
            playlist.setShareToken(null);
            log.info("toggleVisibility: playlist ID {} set to PUBLIC, share token removed", playlistId);
        }

        return toDto(playlistRepository.save(playlist));
    }

    private void validateOwnership(Playlist playlist, UUID userId) {
        if (!playlist.getOwner().getId().equals(userId)) {
            throw new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found");
        }
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

    private void findUserById(UUID userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new PlaylistException(ErrorType.USER_NOT_FOUND, "User not found")
        );
    }

    private Playlist findPlaylistById(Long id) {
        return playlistRepository.findById(id).orElseThrow(
                () -> new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found")
        );
    }

    private PlaylistDto toDto(Playlist playlist) {
        PlaylistDto dto = new PlaylistDto();
        dto.setPlaylistId(playlist.getId());
        dto.setPlaylistName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setSlug(playlist.getSlug());
        dto.setVisibility(playlist.getVisibility());
        dto.setTotalSongs(playlist.getSongs().size());
        if (playlist.getVisibility() == PlaylistVisibility.PRIVATE) {
            dto.setShareToken(playlist.getShareToken());
        }
        return dto;
    }

}