package io.github.deeqma.music.api;


import io.github.deeqma.music.dto.CreateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.service.SongService;
import io.github.deeqma.music.service.UploadSongService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/songs")
public class SongController {

    private final UploadSongService uploadSongService;
    private final SongService  songService;

    public SongController(UploadSongService uploadSongService, SongService songService) {
        this.uploadSongService = uploadSongService;
        this.songService = songService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongDto> uploadSong(
            @RequestPart("file") MultipartFile file,
            @RequestParam("songName") String songName,
            @RequestParam("artistName") String artistName,
            @RequestParam(value = "album", required = false) String album,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam("releaseYear") int releaseYear) {

        CreateSongDto dto = new CreateSongDto();
        dto.setSongName(songName);
        dto.setArtistName(artistName);
        dto.setAlbum(album);
        dto.setGenre(genre);
        dto.setReleaseYear(releaseYear);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploadSongService.uploadSong(file, dto));
    }

    @GetMapping
    public ResponseEntity<List<SongDto>> getAllSongs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int pageSize) {
        return ResponseEntity.ok(songService.getAllSongs(page, pageSize));
    }

}
