package Rift.Radio.controller;

import Rift.Radio.api.SongController;
import Rift.Radio.error.ErrorType;
import Rift.Radio.error.SongException;
import Rift.Radio.service.SongService;
import Rift.Radio.Tests;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@TestPropertySource(locations = "classpath:test_local.properties")
public class SongControllerUnitTest extends Tests {

    @Mock
    private SongService songService;

    @InjectMocks
    private SongController songController;

    public SongControllerUnitTest() {
        super();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testUploadFile_Success() {
        MultipartFile file = new MockMultipartFile("file", "new_song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        when(songService.uploadSong(file, SONG_SHOT_IN_THE_DARK.getSongName(), SONG_SHOT_IN_THE_DARK.getArtistName(),
                SONG_SHOT_IN_THE_DARK.getAlbum(), SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre()))
                .thenReturn(SONG_SHOT_IN_THE_DARK);

        ResponseEntity<?> response = songController.uploadFile(file, SONG_SHOT_IN_THE_DARK.getSongName(),
                SONG_SHOT_IN_THE_DARK.getArtistName(), SONG_SHOT_IN_THE_DARK.getAlbum(),
                SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SONG_SHOT_IN_THE_DARK, response.getBody());
        verify(songService, times(1)).uploadSong(file, SONG_SHOT_IN_THE_DARK.getSongName(),
                SONG_SHOT_IN_THE_DARK.getArtistName(), SONG_SHOT_IN_THE_DARK.getAlbum(),
                SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre());
    }

    @Test
    public void testUploadFile_SongNameExistsException() {
        MultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        when(songService.uploadSong(file, SONG_SHOT_IN_THE_DARK.getSongName(), SONG_SHOT_IN_THE_DARK.getArtistName(),
                SONG_SHOT_IN_THE_DARK.getAlbum(), SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre()))
                .thenThrow(new SongException(ErrorType.Duplicated_SONG, "Song name already exists"));

        ResponseEntity<?> response = songController.uploadFile(file, SONG_SHOT_IN_THE_DARK.getSongName(),
                SONG_SHOT_IN_THE_DARK.getArtistName(), SONG_SHOT_IN_THE_DARK.getAlbum(),
                SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre());

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Song name already exists"));
        verify(songService, times(1)).uploadSong(file, SONG_SHOT_IN_THE_DARK.getSongName(),
                SONG_SHOT_IN_THE_DARK.getArtistName(), SONG_SHOT_IN_THE_DARK.getAlbum(),
                SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre());
    }

    @Test
    public void testGetSongPath_Success() {
        when(songService.getSongPath(SONG_SHOT_IN_THE_DARK.getId()))
                .thenReturn(SONG_SHOT_IN_THE_DARK.getFilePath());

        ResponseEntity<?> response = songController.getSongPath(SONG_SHOT_IN_THE_DARK.getId());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SONG_SHOT_IN_THE_DARK.getFilePath(), response.getBody());
        verify(songService, times(1)).getSongPath(SONG_SHOT_IN_THE_DARK.getId());
    }

    @Test
    public void testGetSongPath_NotFoundException() {
        when(songService.getSongPath(1000L))
                .thenThrow(new SongException(ErrorType.SONG_NOT_FOUND, "Song not found"));

        ResponseEntity<?> response = songController.getSongPath(1000L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Song not found"));
        verify(songService, times(1)).getSongPath(1000L);
    }

    @Test
    public void testDeleteSong_Success() {
        doNothing().when(songService).deleteSong(SONG_SHOT_IN_THE_DARK.getId());

        ResponseEntity<?> response = songController.deleteSong(SONG_SHOT_IN_THE_DARK.getId());

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(songService, times(1)).deleteSong(SONG_SHOT_IN_THE_DARK.getId());
    }

    @Test
    public void testDeleteSong_NotFoundException() {
        doThrow(new SongException(ErrorType.SONG_NOT_FOUND, "Song not found"))
                .when(songService).deleteSong(1000L);

        ResponseEntity<?> response = songController.deleteSong(1000L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Song not found"));
        verify(songService, times(1)).deleteSong(1000L);
    }

    @Test
    public void testEditSong_Success() {
        MultipartFile file = new MockMultipartFile("file", "edited_song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        when(songService.editSong(eq(SONG_SHOT_IN_THE_DARK.getId()), any(), eq("Edited Song"),
                eq("Edited Artist"), eq("Edited Album"), eq(2021), eq("Edited Genre")))
                .thenReturn(SONG_SHOT_IN_THE_DARK);

        ResponseEntity<?> response = songController.editSong(SONG_SHOT_IN_THE_DARK.getId(), file, "Edited Song",
                "Edited Artist", "Edited Album", 2021, "Edited Genre");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(SONG_SHOT_IN_THE_DARK, response.getBody());
        verify(songService, times(1)).editSong(eq(SONG_SHOT_IN_THE_DARK.getId()), any(), eq("Edited Song"),
                eq("Edited Artist"), eq("Edited Album"), eq(2021), eq("Edited Genre"));
    }

    @Test
    public void testEditSong_SongNameExistsException() {
        MultipartFile file = new MockMultipartFile("file", "edited_song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        when(songService.editSong(eq(SONG_SHOT_IN_THE_DARK.getId()), any(), eq(SONG_BACK_IN_THE_SADDLE.getSongName()),
                eq(SONG_SHOT_IN_THE_DARK.getArtistName()), eq(SONG_SHOT_IN_THE_DARK.getAlbum()), eq(2021), eq("Edited Genre")))
                .thenThrow(new SongException(ErrorType.Duplicated_SONG, "Song name already exists"));

        ResponseEntity<?> response = songController.editSong(SONG_SHOT_IN_THE_DARK.getId(), file,
                SONG_BACK_IN_THE_SADDLE.getSongName(), SONG_SHOT_IN_THE_DARK.getArtistName(),
                SONG_SHOT_IN_THE_DARK.getAlbum(), 2021, "Edited Genre");

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Song name already exists"));
        verify(songService, times(1)).editSong(eq(SONG_SHOT_IN_THE_DARK.getId()), any(), eq(SONG_BACK_IN_THE_SADDLE.getSongName()),
                eq(SONG_SHOT_IN_THE_DARK.getArtistName()), eq(SONG_SHOT_IN_THE_DARK.getAlbum()), eq(2021), eq("Edited Genre"));
    }

    @Test
    public void testEditSong_NotFoundException() {
        MultipartFile file = new MockMultipartFile("file", "edited_song.mp3", "audio/mpeg", new byte[]{1, 2, 3});
        when(songService.editSong(eq(1000L), any(), eq("Edited Song"), eq("Edited Artist"),
                eq("Edited Album"), eq(2021), eq("Edited Genre")))
                .thenThrow(new SongException(ErrorType.SONG_NOT_FOUND, "Song not found"));

        ResponseEntity<?> response = songController.editSong(1000L, file, "Edited Song", "Edited Artist",
                "Edited Album", 2021, "Edited Genre");

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("Song not found"));
        verify(songService, times(1)).editSong(eq(1000L), any(), eq("Edited Song"), eq("Edited Artist"),
                eq("Edited Album"), eq(2021), eq("Edited Genre"));
    }

    @Test
    public void testDownloadSong_Success() throws IOException {
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        ServletOutputStream outputStream = mock(ServletOutputStream.class);
        when(mockResponse.getOutputStream()).thenReturn(outputStream);

        songController.downloadSong(SONG_SHOT_IN_THE_DARK.getId(), mockResponse);
        verify(songService, times(1)).downloadSong(SONG_SHOT_IN_THE_DARK.getId(), mockResponse);
    }
}
