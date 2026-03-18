package io.github.deeqma.music.api;

import io.github.deeqma.music.dto.CreateOrUpdatePlaylistDto;
import io.github.deeqma.music.dto.PlaylistDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.service.PlaylistService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static io.github.deeqma.music.utils.JwtUtil.extractUserId;

@RestController
@RequestMapping("/api/v0/playlists")
@CrossOrigin(origins = "http://localhost:5173")
public class PlaylistController {

    private final PlaylistService playlistService;

    public PlaylistController(PlaylistService playlistService) {
        this.playlistService = playlistService;
    }

    @PostMapping
    public ResponseEntity<PlaylistDto> createPlaylist(
            @RequestBody @Valid CreateOrUpdatePlaylistDto dto,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.status(HttpStatus.CREATED).body(playlistService.createPlaylist(userId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PlaylistDto>> getAllPlaylists(@AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(playlistService.getAllPlaylists(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaylistDto> getPlaylistById(
            @PathVariable Long id,
            @ModelAttribute SongFilterDto filterDto,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int pageSize,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(playlistService.getPlaylistById(id, userId, filterDto, page, pageSize));
    }

    @GetMapping("/{id}/search")
    public ResponseEntity<List<SongDto>> searchSongsInPlaylist(
            @PathVariable Long id,
            @RequestParam String query,
            @RequestParam(required = false) String shareToken,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "15") @Min(1) int pageSize,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(playlistService.searchSongsInPlaylist(id, query, shareToken, userId, page, pageSize));
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<PlaylistDto> generateShareToken(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(playlistService.generateShareToken(id, userId));
    }

    @PatchMapping("/{id}/private")
    public ResponseEntity<PlaylistDto> toggleVisibility(
            @PathVariable Long id,
            @RequestParam boolean value,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = extractUserId(jwt);
        return ResponseEntity.ok(playlistService.toggleVisibility(id, value, userId));
    }

}
