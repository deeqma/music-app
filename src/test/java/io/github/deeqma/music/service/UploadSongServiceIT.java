package io.github.deeqma.music.service;

import io.github.deeqma.music.SongTestData;
import io.github.deeqma.music.dbcontainer.AbstractPostgresContainer;
import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UploadSongServiceIT extends AbstractPostgresContainer {

    @Autowired
    private UploadSongService uploadSongService;

    @Autowired
    private SongRepository songRepository;

    @BeforeEach
    void setUp() {
        songRepository.deleteAll();
    }

    @Test
    void throwsFileNotFoundWhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file", "empty.mp3", "audio/mpeg", new byte[0]
        );
        CreateOrUpdateSongDto dto = validDto();

        SongException ex = assertThrows(SongException.class,
                () -> uploadSongService.uploadSong(emptyFile, dto));

        assertEquals(ErrorType.FILE_NOT_FOUND, ex.getErrorType());
    }

    @Test
    void throwsFileStorageErrorWhenFileSizeExceedsLimit() {
        byte[] largeContent = new byte[31457281];
        MockMultipartFile largeFile = new MockMultipartFile(
                "file", "large.mp3", "audio/mpeg", largeContent
        );

        CreateOrUpdateSongDto dto = validDto();

        SongException ex = assertThrows(SongException.class,
                () -> uploadSongService.uploadSong(largeFile, dto));

        assertEquals(ErrorType.FILE_STORAGE_ERROR, ex.getErrorType());
    }

    @Test
    void throwsFileNotFoundWhenExtensionIsNotMp3() {
        MockMultipartFile wrongFile = new MockMultipartFile(
                "file", "song.wav", "audio/mpeg", "bytes".getBytes()
        );

        CreateOrUpdateSongDto dto = validDto();

        SongException ex = assertThrows(SongException.class,
                () -> uploadSongService.uploadSong(wrongFile, dto));

        assertEquals(ErrorType.FILE_NOT_FOUND, ex.getErrorType());
    }

    @Test
    void throwsFileNotFoundWhenContentTypeIsNotAudioMpeg() {
        MockMultipartFile wrongType = new MockMultipartFile(
                "file", "song.mp3", "application/octet-stream", "bytes".getBytes()
        );

        CreateOrUpdateSongDto dto = validDto();

        SongException ex = assertThrows(SongException.class,
                () -> uploadSongService.uploadSong(wrongType, dto));

        assertEquals(ErrorType.FILE_NOT_FOUND, ex.getErrorType());
    }

    @Test
    void throwsDuplicateSongWhenSongNameAndArtistAlreadyExist() {
        songRepository.save(SongTestData.highwayStar());

        CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
        dto.setSongName("Highway Star");
        dto.setArtistName("Deep Purple");
        dto.setReleaseYear(1972);

        MockMultipartFile file = validMp3File("highway.mp3");

        SongException ex = assertThrows(SongException.class, () ->
                uploadSongService.uploadSong(file, dto));

        assertEquals(ErrorType.DUPLICATED_SONG, ex.getErrorType());
    }

    @Test
    void returnsSongDtoAfterSuccessfulUpload() {
        SongDto result = uploadSongService.uploadSong(validMp3File("new-song.mp3"), validDto());

        assertAll(
                () -> assertNotNull(result.getId()),
                () -> assertEquals("New Song", result.getSongName()),
                () -> assertEquals("New Artist", result.getArtistName()),
                () -> assertEquals("New Album", result.getAlbum()),
                () -> assertEquals("Rock", result.getGenre()),
                () -> assertEquals(2000, result.getReleaseYear())
        );
    }

    @Test
    void savedSongExistsInDatabase() {
        SongDto result = uploadSongService.uploadSong(validMp3File("saved-song.mp3"), validDto());

        assertTrue(songRepository.existsById(result.getId()));
    }

    @Test
    void savedSongHasFileHashInDatabase() {
        SongDto result = uploadSongService.uploadSong(validMp3File("hashed-song.mp3"), validDto());

        Song saved = songRepository.findById(result.getId()).orElseThrow();
        assertNotNull(saved.getFileHash());
        assertFalse(saved.getFileHash().isEmpty());
    }

    @Test
    void throwsMp3AlreadyExistWhenSameFileUploaded() {
        CreateOrUpdateSongDto firstDto = validDto();
        MockMultipartFile firstFile = validMp3File("first-upload.mp3");
        uploadSongService.uploadSong(firstFile, firstDto);

        CreateOrUpdateSongDto secondDto = new CreateOrUpdateSongDto();
        secondDto.setSongName("Different Song");
        secondDto.setArtistName("Different Artist");
        secondDto.setReleaseYear(2001);
        MockMultipartFile secondFile = validMp3File("second-upload.mp3");

        SongException ex = assertThrows(SongException.class, () ->
                uploadSongService.uploadSong(secondFile, secondDto));

        assertEquals(ErrorType.MP3_ALREADY_EXIST, ex.getErrorType());
    }

    private MockMultipartFile validMp3File(String filename) {
        return new MockMultipartFile(
                "file",
                filename,
                "audio/mpeg",
                "fake-mp3-bytes".getBytes()
        );
    }

    private CreateOrUpdateSongDto validDto() {
        CreateOrUpdateSongDto dto = new CreateOrUpdateSongDto();
        dto.setSongName("New Song");
        dto.setArtistName("New Artist");
        dto.setAlbum("New Album");
        dto.setGenre("Rock");
        dto.setReleaseYear(2000);
        return dto;
    }

}