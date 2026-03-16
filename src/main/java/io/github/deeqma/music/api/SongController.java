package io.github.deeqma.music.api;


import io.github.deeqma.music.dto.CreateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SongDto> uploadSong(
            @RequestPart("file") MultipartFile file,
            @RequestPart("data") @Valid CreateSongDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(songService.uploadSong(file, dto));
    }

}
