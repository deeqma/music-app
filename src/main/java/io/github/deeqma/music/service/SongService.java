package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.LikedSongRepository;
import io.github.deeqma.music.repository.SongRepository;
import io.github.deeqma.music.utils.SongSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SongService {

    private static final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;
    private final LikedSongRepository likedSongRepository;

    public SongService(SongRepository songRepository, LikedSongRepository likedSongRepository) {
        this.songRepository = songRepository;
        this.likedSongRepository = likedSongRepository;
    }

    public List<SongDto> getAllSongs(SongFilterDto filterDto, UUID userId, int page, int pageSize) {
        log.info("getAllSongs: fetching songs (page {}, size {})", page, pageSize);
        return fetchSongs(SongSpecification.filter(filterDto), userId, page, pageSize);
    }

    public List<SongDto> searchSongs(String query, UUID userId, int page, int pageSize) {
        log.info("searchSongs: searching for '{}'", query);
        return fetchSongs(SongSpecification.search(query), userId, page, pageSize);
    }

    public List<SongDto> getLikedSongs(SongFilterDto filterDto, UUID userId, int page, int pageSize) {
        log.info("getLikedSongs: fetching liked songs for user {}", userId);
        Set<Long> likedSongIds = likedSongRepository.findSongIdsByUserId(userId);

        if (likedSongIds.isEmpty()) {
            return new ArrayList<>();
        }

        Specification<Song> spec = SongSpecification.filter(filterDto)
                .and((root, _, _) -> root.get("id").in(likedSongIds));

        return fetchSongs(spec, userId, page, pageSize);
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

    private List<SongDto> fetchSongs(Specification<Song> spec, UUID userId, int page, int pageSize) {
        List<Song> songs = songRepository.findAll(spec, PageRequest.of(page, pageSize)).getContent();
        Set<Long> likedSongIds = likedSongRepository.findSongIdsByUserId(userId);
        List<SongDto> result = new ArrayList<>();
        for (Song song : songs) {
            SongDto dto = toDto(song);
            dto.setLiked(likedSongIds.contains(song.getId()));
            result.add(dto);
        }
        log.info("fetchSongs: returning {} songs", result.size());
        return result;
    }

    public ResourceRegion StreamSong(Long id, HttpHeaders headers) {

        log.info("getSongRegion: streaming song ID {}", id);

        Song song = findSongById(id);

        try {
            Path path = Paths.get(song.getFilePath());
            Resource resource = new UrlResource(path.toUri());

            if (!resource.exists()) {
                throw new SongException(ErrorType.FILE_NOT_FOUND, "Song file not found on disk");
            }

            return resolveRegion(resource, headers);

        } catch (IOException e) {
            throw new SongException(ErrorType.FILE_NOT_FOUND, "Song file not found", e);
        }
    }

    private ResourceRegion resolveRegion(Resource resource, HttpHeaders headers) throws IOException {
        long contentLength = resource.contentLength();
        HttpRange range = headers.getRange().isEmpty() ? null : headers.getRange().getFirst();

        if (range != null) {
            long start = range.getRangeStart(contentLength);
            long end = range.getRangeEnd(contentLength);
            long rangeLength = Math.min(1048576, end - start + 1);
            return new ResourceRegion(resource, start, rangeLength);
        }

        return new ResourceRegion(resource, 0, Math.min(1048576, contentLength));
    }

    public void deleteSong(Long id) {

        log.info("deleteSong: deleting song ID {}", id);

        Song song = findSongById(id);
        String filePath = song.getFilePath();
        songRepository.delete(song);

        try {
            Files.delete(Paths.get(filePath));
            log.info("deleteSong: file '{}' deleted", filePath);
        } catch (NoSuchFileException _) {
            log.warn("deleteSong: file '{}' was already missing", filePath);
        } catch (IOException _) {
            log.warn("deleteSong: could not delete file '{}'", filePath);
        }
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
