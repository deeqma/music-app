package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.UserProfileDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.UserException;
import io.github.deeqma.music.model.User;
import io.github.deeqma.music.repository.LikedSongRepository;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private static final  Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final LikedSongRepository likedSongRepository;
    private final PlaylistRepository playlistRepository;

    public UserService(UserRepository userRepository, LikedSongRepository likedSongRepository, PlaylistRepository playlistRepository) {
        this.userRepository = userRepository;
        this.likedSongRepository = likedSongRepository;

        this.playlistRepository = playlistRepository;
    }

    public UserProfileDto getProfile(UUID userId) {

        log.info("getProfile: fetching profile for user {}", userId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(ErrorType.NOT_FOUND, "User not found")
        );

        int totalLikedSongs = likedSongRepository.countByUserId(userId);
        int totalPlaylists = playlistRepository.countByOwnerId(userId);

        log.info("getProfile: found profile for user {}", userId);

        UserProfileDto dto = new UserProfileDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setTotalLikedSongs(totalLikedSongs);
        dto.setTotalPlaylists(totalPlaylists);
        dto.setPlaylistLimit(30);
        return dto;
    }

    public void deleteAccount(UUID userId) {

        log.info("deleteAccount: deleting account for user {}", userId);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserException(ErrorType.NOT_FOUND, "User not found")
        );

        userRepository.delete(user);
        log.info("deleteAccount: account deleted for user {}", userId);
    }

}
