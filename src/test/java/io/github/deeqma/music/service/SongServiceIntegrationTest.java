package io.github.deeqma.music.service;

import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import Rift.Radio.Tests;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:test_local.properties")
public class SongServiceIntegrationTest extends Tests {

    @Autowired
    private SongService songService;

    @MockBean
    private SongRepository songRepository;

    @BeforeEach
    public void setup() {
    }

    @Test
    public void testUploadSong_Success() throws IOException {
        Long EXISTING_SONG_ID = 1L;

        Song song = new Song();
        song.setId(EXISTING_SONG_ID);
        song.setSongName(SONG_SHOT_IN_THE_DARK.getSongName());
        song.setArtistName(SONG_SHOT_IN_THE_DARK.getArtistName());
        song.setAlbum(SONG_SHOT_IN_THE_DARK.getAlbum());
        song.setReleaseYear(SONG_SHOT_IN_THE_DARK.getReleaseYear());
        song.setGenre(SONG_SHOT_IN_THE_DARK.getGenre());
        song.setFilePath(FILE_DIRECTORY + SHOT_IN_THE_DARK_MP3);

        // Prepare test data
        MultipartFile file = createMockMultipartFile(SONG_SHOT_IN_THE_DARK.getFilePath());
        String songName = SONG_SHOT_IN_THE_DARK.getSongName();
        String artistName = SONG_SHOT_IN_THE_DARK.getArtistName();
        String album = SONG_SHOT_IN_THE_DARK.getAlbum();
        int releaseYear = SONG_SHOT_IN_THE_DARK.getReleaseYear();
        String genre = SONG_SHOT_IN_THE_DARK.getGenre();

        when(songRepository.existsBySongName(anyString())).thenReturn(false);
        when(songRepository.existsByFilePath(anyString())).thenReturn(false);
        when(songRepository.save(any())).thenReturn(song);

        // Perform the upload
        Song uploadedSong = songService.uploadSong(file, songName, artistName, album, releaseYear, genre);
        System.out.println(uploadedSong);

        // Assertions
        assertNotNull(uploadedSong);
        assertEquals(songName, uploadedSong.getSongName());
        assertEquals(artistName, uploadedSong.getArtistName());
        assertEquals(album, uploadedSong.getAlbum());
        assertEquals(releaseYear, uploadedSong.getReleaseYear());
        assertEquals(genre, uploadedSong.getGenre());

        // Verify interactions
        verify(songRepository, times(1)).existsBySongName(anyString());
        verify(songRepository, times(1)).existsByFilePath(anyString());
        verify(songRepository, times(1)).save(any());
    }

    @Test
    public void testUploadSong_MP3FileExistsException() throws IOException {
        // Prepare test data
        MultipartFile file = createMockMultipartFile(SONG_BACK_IN_THE_SADDLE.getFilePath());

        when(songRepository.existsBySongName(anyString())).thenReturn(false);
        when(songRepository.existsByFilePath(anyString())).thenReturn(true);

        // Perform the upload and assert the exception
        SongException ex = assertThrows(SongException.class, () -> songService.uploadSong(file, SONG_BACK_IN_THE_SADDLE.getSongName(),
                SONG_BACK_IN_THE_SADDLE.getArtistName(), SONG_BACK_IN_THE_SADDLE.getAlbum(),
                SONG_BACK_IN_THE_SADDLE.getReleaseYear(), SONG_BACK_IN_THE_SADDLE.getGenre()));
        assertTrue(ex.getMessage().contains("MP3 file already uploaded"));

        // Verify interactions
        verify(songRepository, times(1)).existsBySongName(anyString());
        verify(songRepository, times(1)).existsByFilePath(anyString());
        verify(songRepository, never()).save(any());
    }

    @Test
    public void testUploadSong_SongNameExistsException() throws IOException {
        // Prepare test data
        MultipartFile file = createMockMultipartFile(SONG_SHOT_IN_THE_DARK.getFilePath());

        when(songRepository.existsBySongName(anyString())).thenReturn(true);

        // Perform the upload and assert the exception
        SongException ex = assertThrows(SongException.class, () -> songService.uploadSong(file, SONG_SHOT_IN_THE_DARK.getSongName(),
                SONG_SHOT_IN_THE_DARK.getArtistName(), SONG_SHOT_IN_THE_DARK.getAlbum(),
                SONG_SHOT_IN_THE_DARK.getReleaseYear(), SONG_SHOT_IN_THE_DARK.getGenre()));
        assertTrue(ex.getMessage().contains("Song name already exists"));

        // Verify interactions
        verify(songRepository, times(1)).existsBySongName(anyString());
        verify(songRepository, never()).existsByFilePath(anyString());
        verify(songRepository, never()).save(any());
    }

    @Test
    public void testDeleteSong_Success(@TempDir Path tempDir) throws IOException {
        Long EXISTING_SONG_ID = 1L;
        Song existingSong = new Song();
        existingSong.setId(EXISTING_SONG_ID);
        existingSong.setSongName("Test Song");
        existingSong.setArtistName("Test Artist");
        existingSong.setAlbum("Test Album");
        existingSong.setReleaseYear(2022);
        existingSong.setGenre("Test Genre");
        existingSong.setFilePath(tempDir.resolve("test_song.mp3").toString());

        Files.copy(Paths.get(FILE_DIRECTORY, SHOT_IN_THE_DARK_MP3), tempDir.resolve("test_song.mp3"));

        when(songRepository.findById(EXISTING_SONG_ID)).thenReturn(Optional.of(existingSong));
        doNothing().when(songRepository).delete(existingSong);

        // Perform the delete
        songService.deleteSong(EXISTING_SONG_ID);

        // Verify interactions
        verify(songRepository, times(1)).findById(EXISTING_SONG_ID);
        verify(songRepository, times(1)).delete(existingSong);
        verifyNoMoreInteractions(songRepository);
    }

    @Test
    public void testEditSong_Success(@TempDir Path tempDir) throws IOException {
        Long EXISTING_SONG_ID = 1L;

        // Prepare the existing song
        Song existingSong = new Song();
        existingSong.setId(EXISTING_SONG_ID);
        existingSong.setSongName("Test Song");
        existingSong.setArtistName("Test Artist");
        existingSong.setAlbum("Test Album");
        existingSong.setReleaseYear(2022);
        existingSong.setGenre("Test Genre");
        existingSong.setFilePath(tempDir.resolve("test_song.mp3").toString());

        // Copy the test MP3 file to the temporary directory
        Files.copy(Paths.get(FILE_DIRECTORY, SHOT_IN_THE_DARK_MP3), tempDir.resolve("test_song.mp3"));

        when(songRepository.findById(EXISTING_SONG_ID)).thenReturn(Optional.of(existingSong));

        // Prepare the song object to be saved (with updated metadata)
        Song songToBeSaved = new Song();
        songToBeSaved.setId(EXISTING_SONG_ID);
        songToBeSaved.setSongName("Edited Song Name");
        songToBeSaved.setArtistName("Edited Artist");
        songToBeSaved.setAlbum("Edited Album");
        songToBeSaved.setReleaseYear(2023);
        songToBeSaved.setGenre("Edited Genre");
        songToBeSaved.setFilePath(tempDir.resolve("test_song.mp3").toString());

        // Capture the saved song using an ArgumentCaptor
        ArgumentCaptor<Song> songCaptor = ArgumentCaptor.forClass(Song.class);
        when(songRepository.save(songCaptor.capture())).thenReturn(songToBeSaved);

        // Perform the edit with no new file (metadata update only)
        Song editedSong = songService.editSong(
                EXISTING_SONG_ID,
                null, // No new file provided
                "Edited Song Name",
                "Edited Artist",
                "Edited Album",
                2023,
                "Edited Genre"
        );

        // Assertions for the edited song metadata
        assertNotNull(editedSong);
        assertEquals("Edited Song Name", editedSong.getSongName());
        assertEquals("Edited Artist", editedSong.getArtistName());
        assertEquals("Edited Album", editedSong.getAlbum());
        assertEquals(2023, editedSong.getReleaseYear());
        assertEquals("Edited Genre", editedSong.getGenre());

        // Verify interactions
        verify(songRepository, times(1)).findById(EXISTING_SONG_ID);
        verify(songRepository, times(1)).save(any());

        // Check that the MP3 file exists (not deleted during metadata edit)
        File mp3File = new File(existingSong.getFilePath());
        assertTrue(mp3File.exists());

        // Additional verification to ensure that the saved song file path exists
        File savedMp3File = new File(songCaptor.getValue().getFilePath());
        assertTrue(savedMp3File.exists());
    }

    private MultipartFile createMockMultipartFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] content = Files.readAllBytes(file.toPath());
        return new MockMultipartFile("file", file.getName(), "audio/mpeg", content);
    }
}
