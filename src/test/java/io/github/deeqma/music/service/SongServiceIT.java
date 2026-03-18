package io.github.deeqma.music.service;

import static org.junit.jupiter.api.Assertions.*;

import io.github.deeqma.music.SongTestData;
import io.github.deeqma.music.dbcontainer.AbstractPostgresContainer;
import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


@SpringBootTest
@ActiveProfiles("test")
class SongServiceIT extends AbstractPostgresContainer {

    @Autowired
    private SongService songService;

    @Autowired
    private SongRepository songRepository;

    @BeforeEach
    void setUp() {
        songRepository.deleteAll();
        songRepository.saveAll(SongTestData.all());
    }

    @Nested
    class GetAllSongs {

        @Test
        void returnsFirstPageWithCorrectSize() {
            List<SongDto> result = songService.getAllSongs(0, 3);
            assertEquals(3, result.size());
        }

        @Test
        void returnsSecondPageWithRemainingItems() {
            List<SongDto> result = songService.getAllSongs(1, 3);
            assertEquals(2, result.size());
        }

        @Test
        void returnsEmptyListWhenPageExceedsTotalSongs() {
            List<SongDto> result = songService.getAllSongs(10, 15);
            assertTrue(result.isEmpty());
        }

        @Test
        void returnsAllSongsWhenPageSizeIsLargerThanTotal() {
            List<SongDto> result = songService.getAllSongs(0, 50);
            assertEquals(5, result.size());
        }

        @Test
        void dtoMappingIsCorrect() {
            List<SongDto> result = songService.getAllSongs(0, 50);
            SongDto dto = result.stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();
            assertAll(
                    () -> assertEquals("Deep Purple", dto.getArtistName()),
                    () -> assertEquals("Machine Head", dto.getAlbum()),
                    () -> assertEquals(1972, dto.getReleaseYear()),
                    () -> assertEquals("Hard Rock", dto.getGenre()),
                    () -> assertEquals(370, dto.getDurationSeconds())
            );
        }
    }

    @Nested
    class FilterSongs {

        @Test
        void filterByArtistReturnsOnlyMatchingSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setArtistName("Deep Purple");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(s -> s.getArtistName().equals("Deep Purple")));
        }

        @Test
        void filterByArtistIsCaseInsensitive() {
            SongFilterDto filter = new SongFilterDto();
            filter.setArtistName("deep purple");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
        }

        @Test
        void filterByArtistPartialMatchWorks() {
            SongFilterDto filter = new SongFilterDto();
            filter.setArtistName("deep");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
        }

        @Test
        void filterByGenreReturnsOnlyMatchingSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setGenre("Metal");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(1, result.size());
            assertEquals("Metallica", result.getFirst().getArtistName());
        }

        @Test
        void filterByGenreIsCaseInsensitive() {
            SongFilterDto filter = new SongFilterDto();
            filter.setGenre("metal");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(1, result.size());
        }

        @Test
        void filterByAlbumReturnsOnlyMatchingSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setAlbum("Machine Head");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(s -> s.getAlbum().equals("Machine Head")));
        }

        @Test
        void filterByAlbumPartialMatchWorks() {
            SongFilterDto filter = new SongFilterDto();
            filter.setAlbum("machine");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
        }

        @Test
        void filterByYearRangeReturnsCorrectSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setYearFrom(1972);
            filter.setYearTo(1973);

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(3, result.size());
            assertTrue(result.stream().allMatch(s -> s.getReleaseYear() >= 1972 && s.getReleaseYear() <= 1973));
        }

        @Test
        void filterByYearFromOnlyReturnsCorrectSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setYearFrom(1983);

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(s -> s.getReleaseYear() >= 1983));
        }

        @Test
        void filterByYearToOnlyReturnsCorrectSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setYearTo(1972);

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(2, result.size());
            assertTrue(result.stream().allMatch(s -> s.getReleaseYear() <= 1972));
        }

        @Test
        void filterWithMultipleCriteriaReturnsCorrectSongs() {
            SongFilterDto filter = new SongFilterDto();
            filter.setArtistName("ZZ Top");
            filter.setYearFrom(1980);

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(1, result.size());
            assertEquals("Sharp Dressed Man", result.getFirst().getSongName());
        }

        @Test
        void filterWithNoMatchReturnsEmptyList() {
            SongFilterDto filter = new SongFilterDto();
            filter.setArtistName("Beatles");

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertTrue(result.isEmpty());
        }

        @Test
        void filterWithNoFiltersReturnsAllSongs() {
            SongFilterDto filter = new SongFilterDto();

            List<SongDto> result = songService.filterSongs(filter, 0, 15);

            assertEquals(5, result.size());
        }

        @Test
        void filterWithPaginationReturnsCorrectPage() {
            SongFilterDto filter = new SongFilterDto();

            List<SongDto> firstPage = songService.filterSongs(filter, 0, 3);
            List<SongDto> secondPage = songService.filterSongs(filter, 1, 3);

            assertEquals(3, firstPage.size());
            assertEquals(2, secondPage.size());
        }

        @Test
        void filterResultsDoNotOverlapBetweenPages() {
            SongFilterDto filter = new SongFilterDto();

            List<SongDto> firstPage = songService.filterSongs(filter, 0, 3);
            List<SongDto> secondPage = songService.filterSongs(filter, 1, 3);

            List<Long> firstPageIds = firstPage.stream().map(SongDto::getId).toList();
            List<Long> secondPageIds = secondPage.stream().map(SongDto::getId).toList();

            assertTrue(firstPageIds.stream().noneMatch(secondPageIds::contains));
        }
    }

    @Nested
    class UpdateSong {

        @Test
        void updatesSongNameSuccessfully() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName("Updated Highway Star");
            dto.setArtistName(saved.getArtistName());
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Updated Highway Star", result.getSongName());
        }

        @Test
        void updatesArtistNameSuccessfully() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName("Updated Artist");
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Updated Artist", result.getArtistName());
        }

        @Test
        void updatesReleaseYearSuccessfully() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName(saved.getArtistName());
            dto.setReleaseYear(2000);

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals(2000, result.getReleaseYear());
        }

        @Test
        void doesNotOverrideSongNameWhenBlank() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName("");
            dto.setArtistName(saved.getArtistName());
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Highway Star", result.getSongName());
        }

        @Test
        void doesNotOverrideArtistNameWhenBlank() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName("");
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Deep Purple", result.getArtistName());
        }

        @Test
        void doesNotOverrideAlbumWhenBlank() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName(saved.getArtistName());
            dto.setAlbum("");
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Machine Head", result.getAlbum());
        }

        @Test
        void doesNotOverrideAlbumWhenNull() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName(saved.getArtistName());
            dto.setAlbum(null);
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Machine Head", result.getAlbum());
        }

        @Test
        void doesNotOverrideGenreWhenBlank() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName(saved.getArtistName());
            dto.setGenre("");
            dto.setReleaseYear(saved.getReleaseYear());

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals("Hard Rock", result.getGenre());
        }

        @Test
        void doesNotOverrideReleaseYearWhenZero() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(saved.getSongName());
            dto.setArtistName(saved.getArtistName());
            dto.setReleaseYear(0);

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertEquals(1972, result.getReleaseYear());
        }

        @Test
        void throwsSongNotFoundWhenIdDoesNotExist() {
            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName("Any");
            dto.setArtistName("Any");
            dto.setReleaseYear(2000);

            SongException ex = assertThrows(SongException.class,
                    () -> songService.updateSong(999L, dto));

            assertEquals(ErrorType.SONG_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void throwsDuplicateSongWhenSongNameAndArtistAlreadyExist() {
            Song song1 = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            Song song2 = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Smoke on the Water"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName(song2.getSongName());
            dto.setArtistName(song2.getArtistName());
            dto.setReleaseYear(song2.getReleaseYear());

            SongException ex = assertThrows(SongException.class,
                    () -> songService.updateSong(song1.getId(), dto));

            assertEquals(ErrorType.DUPLICATED_SONG, ex.getErrorType());
        }

        @Test
        void dtoMappingIsCorrectAfterUpdate() {
            Song saved = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
            dto.setSongName("New Name");
            dto.setArtistName("New Artist");
            dto.setAlbum("New Album");
            dto.setGenre("New Genre");
            dto.setReleaseYear(2000);

            SongDto result = songService.updateSong(saved.getId(), dto);

            assertAll(
                    () -> assertEquals(saved.getId(), result.getId()),
                    () -> assertEquals("New Name", result.getSongName()),
                    () -> assertEquals("New Artist", result.getArtistName()),
                    () -> assertEquals("New Album", result.getAlbum()),
                    () -> assertEquals("New Genre", result.getGenre()),
                    () -> assertEquals(2000, result.getReleaseYear()),
                    () -> assertEquals(saved.getFilePath(), result.getFilePath()),
                    () -> assertEquals(saved.getDurationSeconds(), result.getDurationSeconds())
            );
        }
    }

    @Nested
    class StreamSong {

        private Path tempFile;

        @BeforeEach
        void createTempFile() throws IOException {
            tempFile = Files.createTempFile("test-song", ".mp3");
            Files.write(tempFile, "fake-mp3-content".getBytes());
        }

        @AfterEach
        void deleteTempFile() throws IOException {
            Files.deleteIfExists(tempFile);
        }

        @Test
        void returnsResourceRegionWhenNoRangeHeader() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            song.setFilePath(tempFile.toString());
            songRepository.save(song);

            HttpHeaders headers = new HttpHeaders();
            ResourceRegion region = songService.StreamSong(song.getId(), headers);

            assertNotNull(region);
            assertEquals(0, region.getPosition());
        }

        @Test
        void returnsResourceRegionWithRangeHeader() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            song.setFilePath(tempFile.toString());
            songRepository.save(song);

            HttpHeaders headers = new HttpHeaders();
            headers.setRange(List.of(HttpRange.createByteRange(0, 7)));

            ResourceRegion region = songService.StreamSong(song.getId(), headers);

            assertNotNull(region);
            assertEquals(0, region.getPosition());
            assertEquals(8, region.getCount());
        }

        @Test
        void throwsFileNotFoundWhenSongIdDoesNotExist() {
            HttpHeaders headers = new HttpHeaders();

            SongException ex = assertThrows(SongException.class,
                    () -> songService.StreamSong(999L, headers));

            assertEquals(ErrorType.SONG_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void throwsFileNotFoundWhenFileDoesNotExistOnDisk() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            song.setFilePath("nonexistent/path/song.mp3");
            songRepository.save(song);

            HttpHeaders headers = new HttpHeaders();

            SongException ex = assertThrows(SongException.class,
                    () -> songService.StreamSong(song.getId(), headers));

            assertEquals(ErrorType.FILE_NOT_FOUND, ex.getErrorType());
        }
    }

    @Nested
    class DeleteSong {

        private Path tempFile;

        @BeforeEach
        void createTempFile() throws IOException {
            tempFile = Files.createTempFile("test-song", ".mp3");
            Files.write(tempFile, "fake-mp3-content".getBytes());
        }

        @Test
        void deletesSongFromDatabase() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            Long id = song.getId();
            songService.deleteSong(id);

            assertFalse(songRepository.existsById(id));
        }

        @Test
        void deletesFileFromDiskWhenExists() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            song.setFilePath(tempFile.toString());
            songRepository.save(song);

            songService.deleteSong(song.getId());

            assertFalse(Files.exists(tempFile));
        }

        @Test
        void doesNotThrowWhenFileAlreadyMissingFromDisk() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            song.setFilePath("nonexistent/path/song.mp3");
            songRepository.save(song);

            assertDoesNotThrow(() -> songService.deleteSong(song.getId()));
        }

        @Test
        void throwsSongNotFoundWhenIdDoesNotExist() {
            SongException ex = assertThrows(SongException.class,
                    () -> songService.deleteSong(999L));

            assertEquals(ErrorType.SONG_NOT_FOUND, ex.getErrorType());
        }

        @Test
        void deletingOneSongDoesNotAffectOthers() {
            Song song = songRepository.findAll().stream()
                    .filter(s -> s.getSongName().equals("Highway Star"))
                    .findFirst()
                    .orElseThrow();

            songService.deleteSong(song.getId());

            assertEquals(4, songRepository.count());
        }
    }

}