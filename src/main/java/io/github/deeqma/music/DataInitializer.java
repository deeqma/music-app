package io.github.deeqma.music;

import io.github.deeqma.music.dto.CreatePlaylistDto;
import io.github.deeqma.music.dto.CreateSongDto;
import io.github.deeqma.music.model.Playlist;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.PlaylistRepository;
import io.github.deeqma.music.repository.SongRepository;
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
//@Profile("dev")
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final SongRepository songRepository;
    private final PlaylistRepository playlistRepository;
    private final ObjectMapper objectMapper;

    public DataInitializer(SongRepository songRepository, PlaylistRepository playlistRepository, ObjectMapper objectMapper) {
        this.songRepository = songRepository;
        this.playlistRepository = playlistRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) {
        loadSongs();
        loadPlaylists();
    }

    private void loadSongs() {
        try {
            InputStream inputStream = new ClassPathResource("songs.json").getInputStream();
            List<CreateSongDto> dtos = objectMapper.readValue(inputStream, new TypeReference<>() {
            });

            int added = 0;
            int skipped = 0;

            for (CreateSongDto dto : dtos) {
                if (songRepository.existsBySongNameAndArtistName(dto.getSongName(), dto.getArtistName())) {
                    skipped++;
                } else {
                    Song song = new Song();
                    song.setSongName(dto.getSongName());
                    song.setArtistName(dto.getArtistName());
                    song.setAlbum(dto.getAlbum());
                    song.setGenre(dto.getGenre());
                    song.setReleaseYear(dto.getReleaseYear());
                    song.setFilePath(dto.getFilePath());
                    songRepository.save(song);
                    added++;
                }
            }

            log.info("DataInitializer: songs added {}, skipped {}", added, skipped);

        } catch (Exception e) {
            log.error("DataInitializer: failed to load songs", e);
        }
    }

    private void loadPlaylists() {
        try {
            InputStream inputStream = new ClassPathResource("playlists.json").getInputStream();
            List<CreatePlaylistDto> dtos = objectMapper.readValue(inputStream, new TypeReference<List<CreatePlaylistDto>>() {});

            int added = 0;
            int skipped = 0;

            for (CreatePlaylistDto dto : dtos) {
                if (playlistRepository.existsByName(dto.getPlaylistName())) {
                    skipped++;
                } else {
                    Playlist playlist = new Playlist();
                    playlist.setName(dto.getPlaylistName());
                    playlist.setDescription(dto.getDescription());
                    playlistRepository.save(playlist);
                    added++;
                }
            }

            log.info("DataInitializer: playlists added {}, skipped {}", added, skipped);

        } catch (Exception e) {
            log.error("DataInitializer: failed to load playlists", e);
        }
    }
}