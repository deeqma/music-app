package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.CreateOrUpdatePlaylistDto;
import io.github.deeqma.music.dto.PlaylistDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.PlaylistException;
import io.github.deeqma.music.model.Playlist;
import io.github.deeqma.music.model.PlaylistVisibility;
import io.github.deeqma.music.model.User;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PlaylistService {

    private static final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;

    public PlaylistService(PlaylistRepository playlistRepository, UserRepository userRepository) {
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
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

    private String generateSlug(String name) {
        return name.toLowerCase().trim().replaceAll("\\s+", "-");
    }

    private PlaylistDto toDto(Playlist playlist) {
        PlaylistDto dto = new PlaylistDto();
        dto.setPlaylistId(playlist.getId());
        dto.setPlaylistName(playlist.getName());
        dto.setDescription(playlist.getDescription());
        dto.setVisibility(playlist.getVisibility());
        dto.setSlug(playlist.getSlug());
        dto.setTotalSongs(playlist.getSongs().size());
        return dto;
    }

    private Playlist findPlaylistById(Long id) {
        return playlistRepository.findById(id).orElseThrow(
                () -> new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found")
        );
    }

}