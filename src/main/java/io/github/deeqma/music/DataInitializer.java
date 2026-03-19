package io.github.deeqma.music;

import io.github.deeqma.music.model.Playlist;
import io.github.deeqma.music.model.PlaylistVisibility;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.model.User;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.SongRepository;
import io.github.deeqma.music.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

@Component
@Profile("dev")
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private static final String SYSTEM_USERNAME = "system";

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(SongRepository songRepository,
                           PlaylistRepository playlistRepository,
                           UserRepository userRepository,
                           ObjectMapper objectMapper) {
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    private record SongData(
            String songName,
            String artistName,
            String album,
            String genre,
            int releaseYear,
            String filePath
    ) {}

    private record PlaylistData(
            String playlistName,
            String description,
            String visibility
    ) {}

    @Override
    public void run(ApplicationArguments args) {
        User systemUser = loadSystemUser();
        loadSongs();
        loadPlaylists(systemUser);
    }

    private User loadSystemUser() {
        return userRepository.findByUsername(SYSTEM_USERNAME).orElseGet(() -> {
            User system = new User();
            system.setUsername(SYSTEM_USERNAME);
            system.setHashedPassword("system-no-login");
            User saved = userRepository.save(system);
            log.info("DataInitializer: system user created");
            return saved;
        });
    }

    private void loadSongs() {
        try {
            InputStream inputStream = new ClassPathResource("songs.json").getInputStream();
            List<SongData> songs = objectMapper.readValue(inputStream, new TypeReference<>() {});

            int added = 0;
            int skipped = 0;

            for (SongData data : songs) {
                if (songRepository.existsBySongNameAndArtistName(data.songName(), data.artistName())) {
                    skipped++;
                } else {
                    Song song = new Song();
                    song.setSongName(data.songName());
                    song.setArtistName(data.artistName());
                    song.setAlbum(data.album());
                    song.setGenre(data.genre());
                    song.setReleaseYear(data.releaseYear());
                    song.setFilePath(data.filePath());
                    songRepository.save(song);
                    added++;
                }
            }

            log.info("DataInitializer: songs added {}, skipped {}", added, skipped);

        } catch (Exception e) {
            log.error("DataInitializer: failed to load songs", e);
        }
    }

    private void loadPlaylists(User systemUser) {
        try {
            InputStream inputStream = new ClassPathResource("playlists.json").getInputStream();
            List<PlaylistData> playlists = objectMapper.readValue(inputStream, new TypeReference<>() {});

            int added = 0;
            int skipped = 0;

            for (PlaylistData data : playlists) {
                if (playlistRepository.existsByName(data.playlistName())) {
                    skipped++;
                    continue;
                }

                Playlist playlist = new Playlist();
                playlist.setName(data.playlistName());
                playlist.setDescription(data.description());
                playlist.setSlug(data.playlistName().toLowerCase().trim().replaceAll("\\s+", "-"));
                playlist.setVisibility(PlaylistVisibility.PUBLIC);
                playlist.setOwner(systemUser);

                assignSongsToPlaylist(playlist, data.playlistName());

                playlistRepository.save(playlist);
                added++;
            }

            log.info("DataInitializer: playlists added {}, skipped {}", added, skipped);

        } catch (Exception e) {
            log.error("DataInitializer: failed to load playlists", e);
        }
    }

    private void assignSongsToPlaylist(Playlist playlist, String playlistName) {
        List<Song> allSongs = songRepository.findAll();

        switch (playlistName) {
            case "Rock" -> allSongs.stream()
                    .filter(s -> containsIgnoreCase(s.getGenre(), "rock"))
                    .filter(s -> !containsIgnoreCase(s.getGenre(), "hard rock"))
                    .filter(s -> !containsIgnoreCase(s.getGenre(), "heavy metal"))
                    .forEach(s -> playlist.getSongs().add(s));

            case "Blues" -> allSongs.stream()
                    .filter(s -> containsIgnoreCase(s.getGenre(), "blues"))
                    .forEach(s -> playlist.getSongs().add(s));

            case "Hard Rock" -> allSongs.stream()
                    .filter(s -> containsIgnoreCase(s.getGenre(), "hard rock"))
                    .forEach(s -> playlist.getSongs().add(s));

            case "Metal" -> allSongs.stream()
                    .filter(s -> containsIgnoreCase(s.getGenre(), "metal"))
                    .forEach(s -> playlist.getSongs().add(s));

            case "Soul" -> allSongs.stream()
                    .filter(s -> containsIgnoreCase(s.getGenre(), "soul") ||
                            containsIgnoreCase(s.getGenre(), "r&b"))
                    .forEach(s -> playlist.getSongs().add(s));

            default -> log.warn("DataInitializer: no song mapping defined for playlist '{}'", playlistName);
        }

        log.info("DataInitializer: assigned {} songs to playlist '{}'", playlist.getSongs().size(), playlistName);
    }

    private boolean containsIgnoreCase(String text, String keyword) {
        if (text == null) return false;
        return text.toLowerCase().contains(keyword.toLowerCase());
    }
}