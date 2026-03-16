package io.github.deeqma.music.api;

import io.github.deeqma.music.model.LikedSong;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.service.LikedSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v0/liked")
@CrossOrigin(origins = "http://localhost:5173")
public class LikedSongController {

    private final LikedSongService likedService;

    @Autowired
    public LikedSongController(LikedSongService likedService) {
        this.likedService = likedService;
    }

    @PostMapping("/{songId}")
    public ResponseEntity<LikedSong> likeSong(@PathVariable Long songId) {
        LikedSong likedSong = likedService.likeSong(songId);
        return ResponseEntity.status(HttpStatus.CREATED).body(likedSong);
    }

    @GetMapping
    public ResponseEntity<List<Song>> getAllLikedSongs() {
        List<Song> likedSongs = likedService.getAllLikedSongs();
        return ResponseEntity.ok(likedSongs);
    }

    @DeleteMapping("/{songId}")
    public ResponseEntity<Void> removeLikedSong(@PathVariable Long songId) {
        likedService.removeLikedSong(songId);
        return ResponseEntity.ok().build();
    }

}