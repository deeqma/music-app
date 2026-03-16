package Rift.Radio.service;


import Rift.Radio.error.SongException;
import Rift.Radio.model.Song;
import Rift.Radio.repository.SongRepository;
import Rift.Radio.Tests;
import static org.junit.jupiter.api.Assertions.assertEquals;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(locations = "classpath:test_local.properties")
public class SongServiceUnitTest extends Tests {

    private static final Long EXISTING_SONG_ID = 1L;
    private static final Long NON_EXISTING_SONG_ID = 100L;

    @Mock
    private HttpServletResponse response;
    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private SongService songService;

    @BeforeAll
    protected static void beforeAll() {
        Tests.beforeAll();
    }

    // A new file to be used for editing tests
    private static final MockMultipartFile NEW_SONG_FILE = new MockMultipartFile(
            "file", "new_song.mp3", "audio/mpeg", new byte[]{1, 2, 3});

    // Test for uploading a song successfully
    @Test
    public void testUploadSong_Success() throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(FILE_DIRECTORY + SHOT_IN_THE_DARK_MP3));
        // Use spy to stub transferTo so it does nothing
        MockMultipartFile file = spy(new MockMultipartFile("file", SHOT_IN_THE_DARK_MP3, "audio/mpeg", fileContent));
        doNothing().when(file).transferTo(any(File.class));

        when(songRepository.existsBySongName(any())).thenReturn(false);
        when(songRepository.existsByFilePath(any())).thenReturn(false);
        when(songRepository.save(any(Song.class))).thenReturn(SONG_SHOT_IN_THE_DARK);

        Song uploadedSong = songService.uploadSong(file, "Shot in the dark", "AC DC", "Power Up", 2020, "Klassisk rock");

        assertNotNull(uploadedSong);
        assertEquals("Shot in the dark", uploadedSong.getSongName());
        assertEquals("AC DC", uploadedSong.getArtistName());
        assertEquals("Power Up", uploadedSong.getAlbum());
        assertEquals(2020, uploadedSong.getReleaseYear());
        assertEquals("Klassisk rock", uploadedSong.getGenre());
        assertEquals(FILE_DIRECTORY + SHOT_IN_THE_DARK_MP3, uploadedSong.getFilePath());

        verify(songRepository, times(1)).existsBySongName(any());
        verify(songRepository, times(1)).existsByFilePath(any());
        verify(songRepository, times(1)).save(any(Song.class));
    }

    // Test for handling SongNameExistsException during song upload
    @Test
    public void testUploadSong_SongNameExistsException() {
        MockMultipartFile file = new MockMultipartFile("file", "song.mp3", "audio/mpeg", new byte[]{});
        when(songRepository.existsBySongName(any())).thenReturn(true);

        SongException ex = assertThrows(SongException.class,
                () -> songService.uploadSong(file, "Shot in the dark", "AC DC", "Power Up", 2020, "Klassisk rock"));
        assertTrue(ex.getMessage().contains("Song name already exists"));

        verify(songRepository, times(1)).existsBySongName(any());
        verify(songRepository, never()).existsByFilePath(any());
        verify(songRepository, never()).save(any(Song.class));
    }

    // Test for getting the file path when song is not found
    @Test
    public void testGetSongPath_NotFoundException() {
        when(songRepository.findById(10L)).thenReturn(Optional.empty());
        SongException ex = assertThrows(SongException.class, () -> songService.getSongPath(10L));
        assertTrue(ex.getMessage().contains("Song not found"));
        verify(songRepository, times(1)).findById(10L);
    }

    // Test for getting the song file successfully using a temporary file
    @Test
    public void testGetSongFile_Success(@TempDir Path tempDir) throws IOException {
        // Create a temporary file to simulate an existing MP3 file
        Path tempFile = tempDir.resolve("existing_song.mp3");
        Files.write(tempFile, "dummy content".getBytes());
        Song song = new Song();
        song.setId(1L);
        song.setFilePath(tempFile.toString());
        when(songRepository.findById(1L)).thenReturn(Optional.of(song));

        Resource resource = songService.getSongFile(1L);
        assertNotNull(resource);
        assertTrue(resource.exists());

        verify(songRepository, times(1)).findById(1L);
    }

    // Test for handling exception when song file is not found
    @Test
    public void testGetSongFile_NotFoundException() {
        when(songRepository.findById(10L)).thenReturn(Optional.empty());
        SongException ex = assertThrows(SongException.class, () -> songService.getSongFile(10L));
        assertTrue(ex.getMessage().contains("Song not found"));
        verify(songRepository, times(1)).findById(10L);
    }

    // Test for editing a song successfully
    @Test
    public void testEditSong_Success() throws IOException {
        Song existingSong = new Song();
        existingSong.setId(EXISTING_SONG_ID);
        existingSong.setSongName("Old Song Name");
        existingSong.setArtistName("Old Artist");
        existingSong.setAlbum("Old Album");
        existingSong.setReleaseYear(2000);
        existingSong.setGenre("Old Genre");
        existingSong.setFilePath("src/main/resources/LocalStorage/MP3/old_song.mp3");

        when(songRepository.findById(EXISTING_SONG_ID)).thenReturn(Optional.of(existingSong));
        when(songRepository.existsBySongNameAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(songRepository.existsByFilePath(anyString())).thenReturn(false);
        when(songRepository.save(any(Song.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Use spy to stub transferTo for the new file
        MockMultipartFile newFile = spy(new MockMultipartFile("file", "new_song.mp3", "audio/mpeg", "New Song Content".getBytes()));
        doNothing().when(newFile).transferTo(any(File.class));

        Song editedSong = songService.editSong(
                EXISTING_SONG_ID, newFile, "New Song Name", "New Artist", "New Album", 2022, "New Genre"
        );

        assertNotNull(editedSong);
        assertEquals(EXISTING_SONG_ID, editedSong.getId());
        assertEquals("New Song Name", editedSong.getSongName());
        assertEquals("New Artist", editedSong.getArtistName());
        assertEquals("New Album", editedSong.getAlbum());
        assertEquals(2022, editedSong.getReleaseYear());
        assertEquals("New Genre", editedSong.getGenre());

        verify(songRepository, times(1)).findById(EXISTING_SONG_ID);
        verify(songRepository, times(1)).existsBySongNameAndIdNot(anyString(), anyLong());
        verify(songRepository, times(1)).existsByFilePath(anyString());
        verify(songRepository, times(1)).save(any(Song.class));
    }

    // Test for handling NotFoundException when trying to edit a non-existing song
    @Test
    public void testEditSong_NotFoundException() {
        when(songRepository.findById(NON_EXISTING_SONG_ID)).thenReturn(Optional.empty());
        SongException ex = assertThrows(SongException.class, () -> songService.editSong(
                NON_EXISTING_SONG_ID, NEW_SONG_FILE, "New Song Name", "New Artist", "New Album", 2022, "New Genre"
        ));
        assertTrue(ex.getMessage().contains("Song not found"));
        verify(songRepository, times(1)).findById(NON_EXISTING_SONG_ID);
    }

    // Test for handling exception when trying to download a non-existing song
    @Test
    public void testDownloadSong_NotFoundException() {
        when(songRepository.findById(NON_EXISTING_SONG_ID)).thenReturn(Optional.empty());
        SongException ex = assertThrows(SongException.class, () -> songService.downloadSong(NON_EXISTING_SONG_ID, response));
        assertTrue(ex.getMessage().contains("Song not found"));
        verify(songRepository, times(1)).findById(NON_EXISTING_SONG_ID);
        verify(songRepository, never()).delete(any());
    }

    // Test for handling exception when trying to delete a non-existing song
    @Test
    public void testDeleteSong_NotFoundException() {
        when(songRepository.findById(NON_EXISTING_SONG_ID)).thenReturn(Optional.empty());
        SongException ex = assertThrows(SongException.class, () -> songService.deleteSong(NON_EXISTING_SONG_ID));
        assertTrue(ex.getMessage().contains("Song not found"));
        verify(songRepository, times(1)).findById(NON_EXISTING_SONG_ID);
        verify(songRepository, never()).delete(any());
    }

    // Test for deleting a song successfully
    @Test
    public void testDeleteSong_Success() throws IOException {
        Long EXISTING_SONG_ID = 1L;
        Song existingSong = new Song();
        existingSong.setId(EXISTING_SONG_ID);
        // Provide a file path for a dummy file
        existingSong.setFilePath("src/test/java/Rift/Radio/songMP3Test/existing_song.mp3");
        when(songRepository.findById(EXISTING_SONG_ID)).thenReturn(Optional.of(existingSong));
        doNothing().when(songRepository).delete(existingSong);

        // Create a dummy file at the given location so that delete logic can run
        File dummyFile = new File(existingSong.getFilePath());
        dummyFile.getParentFile().mkdirs();
        dummyFile.createNewFile();

        assertDoesNotThrow(() -> songService.deleteSong(EXISTING_SONG_ID));

        verify(songRepository).findById(EXISTING_SONG_ID);
        verify(songRepository).delete(existingSong);

        if (dummyFile.exists()) {
            dummyFile.delete();
        }
    }
}

