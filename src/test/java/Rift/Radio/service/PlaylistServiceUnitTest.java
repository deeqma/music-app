package Rift.Radio.service;

import Rift.Radio.error.PlaylistException;
import Rift.Radio.model.Playlist;
import Rift.Radio.model.Song;
import Rift.Radio.repository.PlaylistRepository;
import Rift.Radio.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PlaylistServiceUnitTest {

    @Mock
    private PlaylistRepository playlistRepository;

    @Mock
    private SongRepository songRepository;

    @InjectMocks
    private PlaylistService playlistService;

    private Song testSong;
    private Playlist testPlaylist;

    @BeforeEach
    public void setUp() {
        // Initialize a test song
        testSong = new Song();
        testSong.setId(1L);
        testSong.setSongName("Test Song");
        testSong.setArtistName("Test Artist");
        testSong.setAlbum("Test Album");
        testSong.setGenre("Test Genre");
        testSong.setReleaseYear(2020);
        testSong.setFilePath("dummy/path.mp3");

        // Initialize a test playlist
        testPlaylist = new Playlist();
        testPlaylist.setId(100L);
        testPlaylist.setName("Test Playlist");
        testPlaylist.setDescription("Test Description");
        testPlaylist.setSongs(new HashSet<>());
    }

    @Test
    public void testCreatePlaylist_Success() {
        when(playlistRepository.existsByName(testPlaylist.getName())).thenReturn(false);
        when(playlistRepository.save(testPlaylist)).thenReturn(testPlaylist);

        Playlist created = playlistService.createPlaylist(testPlaylist);
        assertNotNull(created);
        assertEquals(testPlaylist.getName(), created.getName());

        verify(playlistRepository, times(1)).existsByName(testPlaylist.getName());
        verify(playlistRepository, times(1)).save(testPlaylist);
    }

    @Test
    public void testCreatePlaylist_AlreadyExists() {
        when(playlistRepository.existsByName(testPlaylist.getName())).thenReturn(true);

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.createPlaylist(testPlaylist));
        assertTrue(ex.getMessage().contains("Playlist name already exists"));

        verify(playlistRepository, times(1)).existsByName(testPlaylist.getName());
        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    public void testAddSongToPlaylist_Success() {
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(testSong.getId())).thenReturn(Optional.of(testSong));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Playlist updatedPlaylist = playlistService.addSongToPlaylist(testPlaylist.getId(), testSong.getId());
        assertTrue(updatedPlaylist.getSongs().contains(testSong));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
        verify(songRepository, times(1)).findById(testSong.getId());
        verify(playlistRepository, times(1)).save(testPlaylist);
    }

    @Test
    public void testAddSongToPlaylist_PlaylistNotFound() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.addSongToPlaylist(999L, testSong.getId()));
        assertTrue(ex.getMessage().contains("Playlist not found"));

        verify(playlistRepository, times(1)).findById(999L);
        verify(songRepository, never()).findById(anyLong());
    }

    @Test
    public void testAddSongToPlaylist_SongNotFound() {
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.addSongToPlaylist(testPlaylist.getId(), 999L));
        assertTrue(ex.getMessage().contains("Song not found"));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
        verify(songRepository, times(1)).findById(999L);
    }

    @Test
    public void testAddSongToPlaylist_AlreadyInPlaylist() {
        // Add song initially
        testPlaylist.getSongs().add(testSong);
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(testSong.getId())).thenReturn(Optional.of(testSong));

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.addSongToPlaylist(testPlaylist.getId(), testSong.getId()));
        assertTrue(ex.getMessage().contains("Song already in playlist"));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
        verify(songRepository, times(1)).findById(testSong.getId());
        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    public void testDeleteSongFromPlaylist_Success() {
        // Setup playlist with song inside
        testPlaylist.getSongs().add(testSong);
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(testSong.getId())).thenReturn(Optional.of(testSong));
        when(playlistRepository.save(any(Playlist.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Playlist updatedPlaylist = playlistService.deleteSongFromPlaylist(testPlaylist.getId(), testSong.getId());
        assertFalse(updatedPlaylist.getSongs().contains(testSong));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
        verify(songRepository, times(1)).findById(testSong.getId());
        verify(playlistRepository, times(1)).save(testPlaylist);
    }

    @Test
    public void testDeleteSongFromPlaylist_PlaylistNotFound() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.deleteSongFromPlaylist(999L, testSong.getId()));
        assertTrue(ex.getMessage().contains("Playlist not found"));

        verify(playlistRepository, times(1)).findById(999L);
        verify(songRepository, never()).findById(anyLong());
    }

    @Test
    public void testDeleteSongFromPlaylist_SongNotFound() {
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(999L)).thenReturn(Optional.empty());

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.deleteSongFromPlaylist(testPlaylist.getId(), 999L));
        assertTrue(ex.getMessage().contains("Song not found"));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
        verify(songRepository, times(1)).findById(999L);
    }

    @Test
    public void testDeleteSongFromPlaylist_SongNotInPlaylist() {
        // Playlist is empty
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));
        when(songRepository.findById(testSong.getId())).thenReturn(Optional.of(testSong));

        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.deleteSongFromPlaylist(testPlaylist.getId(), testSong.getId()));
        assertTrue(ex.getMessage().contains("Song not in playlist"));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
        verify(songRepository, times(1)).findById(testSong.getId());
        verify(playlistRepository, never()).save(any(Playlist.class));
    }

    @Test
    public void testListSongsInPlaylist_Success() {
        // Setup playlist with one song
        testPlaylist.getSongs().add(testSong);
        when(playlistRepository.findById(testPlaylist.getId())).thenReturn(Optional.of(testPlaylist));

        List<Song> result = playlistService.listSongsInPlaylist(testPlaylist.getId());
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSong, result.get(0));

        verify(playlistRepository, times(1)).findById(testPlaylist.getId());
    }

    @Test
    public void testListSongsInPlaylist_PlaylistNotFound() {
        when(playlistRepository.findById(999L)).thenReturn(Optional.empty());
        PlaylistException ex = assertThrows(PlaylistException.class, () ->
                playlistService.listSongsInPlaylist(999L));
        assertTrue(ex.getMessage().contains("Playlist not found"));

        verify(playlistRepository, times(1)).findById(999L);
    }

    @Test
    public void testListAllPlaylists_Success() {
        List<Playlist> playlists = Collections.singletonList(testPlaylist);
        when(playlistRepository.findAll()).thenReturn(playlists);

        List<Playlist> result = playlistService.listAllPlaylists();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testPlaylist, result.get(0));

        verify(playlistRepository, times(1)).findAll();
    }
}
