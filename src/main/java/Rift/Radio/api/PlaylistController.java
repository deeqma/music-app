package Rift.Radio.api;

import Rift.Radio.dto.CreatePlaylistDto;
import Rift.Radio.dto.PlaylistDto;
import Rift.Radio.error.PlaylistException;
import Rift.Radio.model.Playlist;
import Rift.Radio.service.PlaylistService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v0/playlists")
@CrossOrigin(origins = "http://localhost:5173")
public class PlaylistController {

    private final PlaylistService playlistService;

    @Autowired
    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @GetMapping
    public ResponseEntity<List<PlaylistDto>> listAllPlaylists() {
        List<PlaylistDto> playlists = playlistService.listAllPlaylists();
        return ResponseEntity.ok(playlists);
    }

    @PostMapping
    public ResponseEntity<?> createPlaylist(@RequestBody @Valid CreatePlaylistDto dto) {
        try {
            CreatePlaylistDto created = playlistService.createPlaylist(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (PlaylistException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Playlist> addSongToPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        Playlist updated = playlistService.addSongToPlaylist(playlistId, songId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Playlist> deleteSongFromPlaylist(@PathVariable Long playlistId, @PathVariable Long songId) {
        Playlist updated = playlistService.deleteSongFromPlaylist(playlistId, songId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<PlaylistDto> listSongsInPlaylist(@PathVariable Long playlistId) {
        PlaylistDto songs = playlistService.listSongsInPlaylist(playlistId);
        return ResponseEntity.ok(songs);
    }

}
