package io.github.deeqma.music;

import io.github.deeqma.music.dto.CreateSongDto;
import io.github.deeqma.music.model.Song;
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
public class SongDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SongDataInitializer.class);

    private final SongRepository songRepository;
    private final ObjectMapper objectMapper;

    public SongDataInitializer(SongRepository songRepository, ObjectMapper objectMapper) {
        this.songRepository = songRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        log.info("SongDataInitializer: starting seed for dev profile");

        InputStream inputStream = new ClassPathResource("songs.json").getInputStream();
        List<CreateSongDto> dtos = objectMapper.readValue(inputStream, new TypeReference<List<CreateSongDto>>() {});

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

        log.info("SongDataInitializer: added {}, skipped {} already existing songs", added, skipped);
    }
}