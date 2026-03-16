package io.github.deeqma.music.service;

import io.github.deeqma.music.error.LikedException;
import io.github.deeqma.music.model.LikedSong;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.LikedSongRepository;
import io.github.deeqma.music.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LikedServiceUnitTest {

    @Mock
    private LikedSongRepository likedRepository;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private LikedSongService likedService;

    private Song testSong;
    private LikedSong testLikedSong;

    @BeforeEach
    public void setUp() {
        testSong = new Song();
        testSong.setId(1L);
        testSong.setSongName("Test Song");
        testSong.setArtistName("Test Artist");
        testSong.setAlbum("Test Album");
        testSong.setGenre("Test Genre");
        testSong.setReleaseYear(2020);
        testSong.setFilePath("dummy/path.mp3");
        testSong.setLiked(false);

        testLikedSong = new LikedSong();
        testLikedSong.setId(1L);
        testLikedSong.setSong(testSong);
    }

    @Test
    public void testLikeSong_Success() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(likedRepository.save(any(LikedSong.class))).thenReturn(testLikedSong);
        when(songRepository.save(testSong)).thenReturn(testSong);

        LikedSong result = likedService.likeSong(1L);
        assertNotNull(result);
        assertEquals(testSong, result.getSong());
        assertTrue(testSong.isLiked());

        verify(songRepository, times(1)).findById(1L);
        verify(likedRepository, times(1)).save(any(LikedSong.class));
        verify(songRepository, times(1)).save(testSong);
    }

    @Test
    public void testLikeSong_SongNotFound() {
        when(songRepository.findById(1L)).thenReturn(Optional.empty());
        LikedException ex = assertThrows(LikedException.class, () -> likedService.likeSong(1L));
        assertTrue(ex.getMessage().contains("Song not found"));

        verify(songRepository, times(1)).findById(1L);
        verify(likedRepository, never()).save(any(LikedSong.class));
        verify(songRepository, never()).save(any(Song.class));
    }



    @Test
    public void testLikeSong_AlreadyLiked() {
        testSong.setLiked(true);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(likedRepository.findBySong(testSong)).thenReturn(Optional.of(new LikedSong()));

        LikedException ex = assertThrows(LikedException.class, () -> likedService.likeSong(1L));

        assertTrue(ex.getMessage().contains("Song already liked"));

        verify(songRepository, times(1)).findById(1L);
        verify(likedRepository, times(1)).findBySong(testSong);
        verify(likedRepository, never()).save(any(LikedSong.class));
        verify(songRepository, never()).save(any(Song.class));
    }

    @Test
    public void testGetAllLikedSongs() {
        List<LikedSong> likedSongs = Collections.singletonList(testLikedSong);
        when(likedRepository.findAll()).thenReturn(likedSongs);

        List<Song> songs = likedService.getAllLikedSongs();
        assertNotNull(songs);
        assertEquals(1, songs.size());
        assertEquals(testSong, songs.get(0));

        verify(likedRepository, times(1)).findAll();
    }

    @Test
    public void testRemoveLikedSong_Success() {
        testSong.setLiked(true);
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(likedRepository.findBySong(testSong)).thenReturn(Optional.of(testLikedSong));
        when(songRepository.save(testSong)).thenReturn(testSong);

        assertDoesNotThrow(() -> likedService.removeLikedSong(1L));
        verify(likedRepository, times(1)).delete(testLikedSong);
        verify(songRepository, times(1)).save(testSong);
        assertFalse(testSong.isLiked());
    }

    @Test
    public void testRemoveLikedSong_SongNotFound() {
        when(songRepository.findById(1L)).thenReturn(Optional.empty());
        LikedException ex = assertThrows(LikedException.class, () -> likedService.removeLikedSong(1L));
        assertTrue(ex.getMessage().contains("Song not found"));

        verify(songRepository, times(1)).findById(1L);
        verify(likedRepository, never()).delete(any(LikedSong.class));
    }

    @Test
    public void testRemoveLikedSong_LikedSongNotFound() {
        when(songRepository.findById(1L)).thenReturn(Optional.of(testSong));
        when(likedRepository.findBySong(testSong)).thenReturn(Optional.empty());
        LikedException ex = assertThrows(LikedException.class, () -> likedService.removeLikedSong(1L));
        assertTrue(ex.getMessage().contains("Liked song not found"));

        verify(songRepository, times(1)).findById(1L);
        verify(likedRepository, times(1)).findBySong(testSong);
        verify(songRepository, never()).save(any(Song.class));
        verify(likedRepository, never()).delete(any(LikedSong.class));
    }
}
