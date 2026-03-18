package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import io.github.deeqma.music.utils.SongSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    public SongDto updateSong(Long id, CreateOrUpdateSongDto dto) {

        log.info("updateSong: updating song ID {}", id);

        Song song = findSongById(id);

        validateUpdate(id, dto);

        if (StringUtils.hasText(dto.getSongName())) {
            song.setSongName(dto.getSongName());
        }
        if (StringUtils.hasText(dto.getArtistName())) {
            song.setArtistName(dto.getArtistName());
        }
        if (StringUtils.hasText(dto.getAlbum())) {
            song.setAlbum(dto.getAlbum());
        }
        if (StringUtils.hasText(dto.getGenre())) {
            song.setGenre(dto.getGenre());
        }
        if (dto.getReleaseYear() != 0) {
            song.setReleaseYear(dto.getReleaseYear());
        }

        Song updated = songRepository.save(song);
        log.info("updateSong: song ID {} updated", id);
        return toDto(updated);
    }

    public List<SongDto> filterSongs(SongFilterDto filterDto, int page, int pageSize) {
        log.info("filterSongs: filtering songs");
        List<Song> songs = songRepository.findAll(
                SongSpecification.filter(filterDto),
                PageRequest.of(page, pageSize)
        ).getContent();
        log.info("filterSongs: found {} songs", songs.size());
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

    private void validateUpdate(Long id, CreateOrUpdateSongDto dto) {
        if (songRepository.existsBySongNameAndArtistNameAndIdNot(dto.getSongName(), dto.getArtistName(), id)) {
            throw new SongException(ErrorType.DUPLICATED_SONG, "Song with this name and artist already exists");
        }
    }

    private Song findSongById(Long id) {
        return songRepository.findById(id).orElseThrow(
                () -> new SongException(ErrorType.SONG_NOT_FOUND, "Song not found")
        );
    }

}
