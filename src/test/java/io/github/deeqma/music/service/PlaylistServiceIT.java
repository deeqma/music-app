package io.github.deeqma.music.service;


import static org.junit.jupiter.api.Assertions.*;

import io.github.deeqma.music.dbcontainer.AbstractPostgresContainer;
import io.github.deeqma.music.dto.*;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.PlaylistException;
import io.github.deeqma.music.model.*;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.SongRepository;
import io.github.deeqma.music.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;


@SpringBootTest
@ActiveProfiles("test")
class PlaylistServiceIT extends AbstractPostgresContainer {

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        playlistRepository.deleteAll();
        songRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUsername("testUser");
        testUser.setHashedPassword("hashedPassword");
        userRepository.save(testUser);
        testUserId = testUser.getId();
    }

    private Playlist savedPlaylist(String name, PlaylistVisibility visibility) {
        CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
        dto.setPlaylistName(name);
        dto.setDescription("Test description");
        dto.setVisibility(visibility);
        return playlistRepository.findById(
                playlistService.createPlaylist(testUserId, dto).getPlaylistId()
        ).orElseThrow();
    }

    private Song savedSong() {
        Song song = new Song();
        song.setSongName("Highway Star");
        song.setArtistName("Deep Purple");
        song.setReleaseYear(2000);
        song.setFilePath("mock/" + "Highway Star" + ".mp3");
        song.setFileHash("hash-" + "Highway Star");
        return songRepository.save(song);
    }

    @Nested
    class CreatePlaylist {

        @Test
        void createsPlaylistSuccessfully() {
            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("My Playlist");
            dto.setDescription("Cool songs");
            dto.setVisibility(PlaylistVisibility.PRIVATE);

            PlaylistDetailsDto result = playlistService.createPlaylist(testUserId, dto);

            assertAll(
                    () -> assertNotNull(result.getPlaylistId()),
                    () -> assertEquals("My Playlist", result.getPlaylistName()),
                    () -> assertEquals("Cool songs", result.getDescription()),
                    () -> assertEquals(PlaylistVisibility.PRIVATE, result.getVisibility()),
                    () -> assertEquals("my-playlist", result.getSlug())
            );
        }

        @Test
        void defaultsToPrivateWhenVisibilityNotProvided() {
            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("No Visibility");
            dto.setVisibility(null);

            PlaylistDetailsDto result = playlistService.createPlaylist(testUserId, dto);

            assertEquals(PlaylistVisibility.PRIVATE, result.getVisibility());
        }

        @Test
        void throwsWhenPlaylistNameAlreadyExistsForUser() {
            savedPlaylist("Duplicate", PlaylistVisibility.PRIVATE);
            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("Duplicate");

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.createPlaylist(testUserId, dto));

            assertEquals(ErrorType.PLAYLIST_ALREADY_EXISTS, ex.getErrorType());
        }

        @Test
        void throwsWhenUserNotFound() {
            UUID unknownUserId = UUID.randomUUID();
            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("Ghost Playlist");

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.createPlaylist(unknownUserId, dto));

            assertEquals(ErrorType.USER_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void throwsWhenPlaylistLimitReached() {
            for (int i = 1; i <= 30; i++) {
                CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
                dto.setPlaylistName("Playlist " + i);
                playlistService.createPlaylist(testUserId, dto);
            }

            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("One Too Many");

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.createPlaylist(testUserId, dto));

            assertEquals(ErrorType.PLAYLIST_LIMIT_REACHED, ex.getErrorType());
        }
    }

    @Nested
    class GetAllPlaylists {

        @Test
        void returnsPublicPlaylistsAndOwnedPlaylists() {
            savedPlaylist("My Private", PlaylistVisibility.PRIVATE);
            savedPlaylist("My Public", PlaylistVisibility.PUBLIC);

            User otherUser = new User();
            otherUser.setUsername("otherUser");
            otherUser.setHashedPassword("hashedPassword");
            userRepository.save(otherUser);

            CreateOrUpdatePlaylistDto otherDto = new CreateOrUpdatePlaylistDto();
            otherDto.setPlaylistName("Other Public");
            otherDto.setVisibility(PlaylistVisibility.PUBLIC);
            playlistService.createPlaylist(otherUser.getId(), otherDto);

            List<PlaylistDto> result = playlistService.getAllPlaylists(testUserId);

            assertEquals(3, result.size());
        }

        @Test
        void doesNotReturnOtherUsersPrivatePlaylists() {
            User otherUser = new User();
            otherUser.setUsername("otherUser");
            otherUser.setHashedPassword("hashedPassword");
            userRepository.save(otherUser);

            CreateOrUpdatePlaylistDto otherDto = new CreateOrUpdatePlaylistDto();
            otherDto.setPlaylistName("Other Private");
            otherDto.setVisibility(PlaylistVisibility.PRIVATE);
            playlistService.createPlaylist(otherUser.getId(), otherDto);

            List<PlaylistDto> result = playlistService.getAllPlaylists(testUserId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    class GetPlaylistById {

        @Test
        void returnsPublicPlaylistForAnyUser() {
            Playlist playlist = savedPlaylist("Public One", PlaylistVisibility.PUBLIC);

            PlaylistDetailsDto result = playlistService.getPlaylistById(
                    playlist.getId(), UUID.randomUUID(), new SongFilterDto(), 0, 15
            );

            assertEquals("Public One", result.getPlaylistName());
        }

        @Test
        void returnsPrivatePlaylistForOwner() {
            Playlist playlist = savedPlaylist("Private One", PlaylistVisibility.PRIVATE);

            PlaylistDetailsDto result = playlistService.getPlaylistById(
                    playlist.getId(), testUserId, new SongFilterDto(), 0, 15
            );

            assertEquals("Private One", result.getPlaylistName());
        }

        @Test
        void throwsWhenAccessingPrivatePlaylistAsOtherUser() {
            Playlist playlist = savedPlaylist("Private One", PlaylistVisibility.PRIVATE);
            UUID otherUserId = UUID.randomUUID();
            SongFilterDto filter = new SongFilterDto();
            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.getPlaylistById(playlistId, otherUserId, filter, 0, 15));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void throwsWhenPlaylistNotFound() {
            SongFilterDto filter = new SongFilterDto();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.getPlaylistById(999L, testUserId, filter, 0, 15));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }
    }

    @Nested
    class AddAndRemoveSong {

        @Test
        void addsSongToPlaylistSuccessfully() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            Song song = savedSong();

            PlaylistDetailsDto result = playlistService.addSongToPlaylist(playlist.getId(), song.getId(), testUserId);

            assertEquals(1, result.getTotalSongs());
        }

        @Test
        void throwsWhenSongAlreadyInPlaylist() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            Song song = savedSong();

            playlistService.addSongToPlaylist(playlist.getId(), song.getId(), testUserId);
            Long playlistId = playlist.getId();
            Long songId = song.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.addSongToPlaylist(playlistId, songId, testUserId));

            assertEquals(ErrorType.SONG_ALREADY_IN_PLAYLIST, ex.getErrorType());
        }

        @Test
        void throwsWhenAddingSongToPlaylistNotOwned() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            Song song = savedSong();
            UUID otherUserId = UUID.randomUUID();
            Long playlistId = playlist.getId();
            Long songId = song.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.addSongToPlaylist(playlistId, songId, otherUserId));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void removesSongFromPlaylistSuccessfully() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            Song song = savedSong();

            playlistService.addSongToPlaylist(playlist.getId(), song.getId(), testUserId);
            PlaylistDetailsDto result = playlistService.removeSongFromPlaylist(playlist.getId(), song.getId(), testUserId);

            assertEquals(0, result.getTotalSongs());
        }

        @Test
        void throwsWhenRemovingSongNotInPlaylist() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            Song song = savedSong();
            Long playlistId = playlist.getId();
            Long songId = song.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.removeSongFromPlaylist(playlistId, songId, testUserId));

            assertEquals(ErrorType.SONG_NOT_IN_PLAYLIST, ex.getErrorType());
        }
    }

    @Nested
    class ToggleVisibility {

        @Test
        void setsPlaylistToPrivate() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PUBLIC);

            PlaylistDetailsDto result = playlistService.toggleVisibility(playlist.getId(), true, testUserId);

            assertEquals(PlaylistVisibility.PRIVATE, result.getVisibility());
        }

        @Test
        void setsPlaylistToPublicAndRemovesShareToken() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            playlistService.generateShareToken(playlist.getId(), testUserId);

            PlaylistDetailsDto result = playlistService.toggleVisibility(playlist.getId(), false, testUserId);

            assertEquals(PlaylistVisibility.PUBLIC, result.getVisibility());
            assertNull(result.getShareToken());
        }

        @Test
        void throwsWhenTogglingVisibilityNotOwned() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            UUID otherUserId = UUID.randomUUID();
            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.toggleVisibility(playlistId, false, otherUserId));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }
    }

    @Nested
    class GenerateShareToken {

        @Test
        void generatesShareTokenForPrivatePlaylist() {
            Playlist playlist = savedPlaylist("Secret", PlaylistVisibility.PRIVATE);

            PlaylistDetailsDto result = playlistService.generateShareToken(playlist.getId(), testUserId);

            assertNotNull(result.getShareToken());
            assertEquals(11, result.getShareToken().length());
        }

        @Test
        void throwsWhenPlaylistIsPublic() {
            Playlist playlist = savedPlaylist("Public", PlaylistVisibility.PUBLIC);
            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.generateShareToken(playlistId, testUserId));

            assertEquals(ErrorType.PLAYLIST_SHARE_NOT_ALLOWED, ex.getErrorType());
        }

        @Test
        void throwsWhenShareTokenAlreadyExists() {
            Playlist playlist = savedPlaylist("Secret", PlaylistVisibility.PRIVATE);
            playlistService.generateShareToken(playlist.getId(), testUserId);
            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.generateShareToken(playlistId, testUserId));

            assertEquals(ErrorType.PLAYLIST_SHARE_ALREADY_EXISTS, ex.getErrorType());
        }

        @Test
        void throwsWhenNotOwner() {
            Playlist playlist = savedPlaylist("Secret", PlaylistVisibility.PRIVATE);
            UUID otherUserId = UUID.randomUUID();
            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.generateShareToken(playlistId, otherUserId));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }
    }

    @Nested
    class SearchSongsInPlaylist {

        @Test
        void returnsMatchingSongsInPlaylist() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PUBLIC);
            Song song = savedSong();
            playlistService.addSongToPlaylist(playlist.getId(), song.getId(), testUserId);

            List<SongDto> result = playlistService.searchSongsInPlaylist(
                    playlist.getId(), "Highway", null, testUserId, 0, 15
            );

            assertEquals(1, result.size());
            assertEquals("Highway Star", result.getFirst().getSongName());
        }

        @Test
        void doesNotReturnSongsOutsidePlaylist() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PUBLIC);
            savedSong();

            List<SongDto> result = playlistService.searchSongsInPlaylist(
                    playlist.getId(), "Highway", null, testUserId, 0, 15
            );

            assertTrue(result.isEmpty());
        }

        @Test
        void allowsAccessToPrivatePlaylistWithShareToken() {
            Playlist playlist = savedPlaylist("Secret", PlaylistVisibility.PRIVATE);
            Song song = savedSong();
            playlistService.addSongToPlaylist(playlist.getId(), song.getId(), testUserId);
            PlaylistDetailsDto withToken = playlistService.generateShareToken(playlist.getId(), testUserId);

            List<SongDto> result = playlistService.searchSongsInPlaylist(
                    playlist.getId(), "Highway", withToken.getShareToken(), UUID.randomUUID(), 0, 15
            );

            assertEquals(1, result.size());
        }

        @Test
        void throwsWhenAccessingPrivatePlaylistWithoutTokenOrOwnership() {
            Playlist playlist = savedPlaylist("Secret", PlaylistVisibility.PRIVATE);
            UUID otherUserId = UUID.randomUUID();
            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.searchSongsInPlaylist(playlistId, "anything", null, otherUserId, 0, 15));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }
    }
    @Nested
    class UpdatePlaylist {

        @Test
        void updatesPlaylistNameAndSlugSuccessfully() {
            Playlist playlist = savedPlaylist("Old Name", PlaylistVisibility.PRIVATE);

            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("New Name");
            dto.setDescription("New description");

            PlaylistDetailsDto result = playlistService.updatePlaylist(playlist.getId(), testUserId, dto);

            assertAll(
                    () -> assertEquals("New Name", result.getPlaylistName()),
                    () -> assertEquals("new-name", result.getSlug()),
                    () -> assertEquals("New description", result.getDescription())
            );
        }

        @Test
        void updatesDescriptionOnlyWhenNameUnchanged() {
            Playlist playlist = savedPlaylist("Same Name", PlaylistVisibility.PRIVATE);

            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("Same Name");
            dto.setDescription("Brand new description");

            PlaylistDetailsDto result = playlistService.updatePlaylist(playlist.getId(), testUserId, dto);

            assertAll(
                    () -> assertEquals("Same Name", result.getPlaylistName()),
                    () -> assertEquals("same-name", result.getSlug()),
                    () -> assertEquals("Brand new description", result.getDescription())
            );
        }

        @Test
        void throwsWhenNewNameAlreadyExistsForUser() {
            savedPlaylist("Taken Name", PlaylistVisibility.PRIVATE);
            Playlist playlist = savedPlaylist("Original Name", PlaylistVisibility.PRIVATE);

            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("Taken Name");

            Long playlistId = playlist.getId();

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.updatePlaylist(playlistId, testUserId, dto));

            assertEquals(ErrorType.PLAYLIST_ALREADY_EXISTS, ex.getErrorType());
        }

        @Test
        void throwsWhenPlaylistNotFound() {
            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("Doesnt Matter");

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.updatePlaylist(999L, testUserId, dto));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void throwsWhenNotOwner() {
            Playlist playlist = savedPlaylist("Rock", PlaylistVisibility.PRIVATE);
            UUID otherUserId = UUID.randomUUID();
            Long playlistId = playlist.getId();

            CreateOrUpdatePlaylistDto dto = new CreateOrUpdatePlaylistDto();
            dto.setPlaylistName("Hijacked Name");

            PlaylistException ex = assertThrows(PlaylistException.class,
                    () -> playlistService.updatePlaylist(playlistId, otherUserId, dto));

            assertEquals(ErrorType.PLAYLIST_NOT_FOUND, ex.getErrorType());
        }
    }
}