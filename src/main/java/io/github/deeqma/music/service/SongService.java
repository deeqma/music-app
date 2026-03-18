package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SongService {

    private static final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    public List<SongDto> getAllSongs(int page, int pageSize) {
        List<Song> songs = songRepository.findAll(PageRequest.of(page, pageSize)).getContent();
        log.info("getAllSongs: fetched {} songs (page {}, size {})", songs.size(), page, pageSize);
        List<SongDto> result = new ArrayList<>();
        for (Song song : songs) {
            result.add(toDto(song));
        }
        return result;
    }

    public SongDto toDto(Song song) {
        SongDto dto = new SongDto();
        dto.setId(song.getId());
        dto.setSongName(song.getSongName());
        dto.setArtistName(song.getArtistName());
        dto.setAlbum(song.getAlbum());
        dto.setGenre(song.getGenre());
        dto.setReleaseYear(song.getReleaseYear());
        dto.setFilePath(song.getFilePath());
        dto.setDurationSeconds(song.getDurationSeconds());
        return dto;
    }

}
