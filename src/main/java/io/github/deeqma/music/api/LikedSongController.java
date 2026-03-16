package io.github.deeqma.music.api;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v0/liked")
@CrossOrigin(origins = "http://localhost:5173")
public class LikedSongController {

}