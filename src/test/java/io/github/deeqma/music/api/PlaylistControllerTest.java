package io.github.deeqma.music.api;

import io.github.deeqma.music.config.SecurityConfig;
import io.github.deeqma.music.dto.CreateOrUpdatePlaylistDto;
import io.github.deeqma.music.dto.PlaylistDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.model.PlaylistVisibility;
import io.github.deeqma.music.service.PlaylistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@WebMvcTest(controllers = PlaylistController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class PlaylistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlaylistService playlistService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor mockJwt() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject("testuser")
                        .claim("userId", TEST_USER_ID.toString()));
    }

    private PlaylistDto mockPlaylistDto() {
        PlaylistDto dto = new PlaylistDto();
        dto.setPlaylistId(1L);
        dto.setPlaylistName("Rock");
        dto.setDescription("Rock songs");
        dto.setSlug("rock");
        dto.setVisibility(PlaylistVisibility.PRIVATE);
        dto.setTotalSongs(0);
        dto.setSongDtos(List.of());
        return dto;
    }

    @Test
    void returnsCreatedOnSuccessfulPlaylistCreation() throws Exception {
        when(playlistService.createPlaylist(any(UUID.class), any(CreateOrUpdatePlaylistDto.class)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(post("/api/v0/playlists")
                        .with(mockJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playlistName": "Rock",
                                  "description": "Rock songs",
                                  "visibility": "PRIVATE"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void returnsOkOnGetAllPlaylists() throws Exception {
        when(playlistService.getAllPlaylists(any(UUID.class)))
                .thenReturn(List.of(mockPlaylistDto()));

        mockMvc.perform(get("/api/v0/playlists")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnGetPlaylistById() throws Exception {
        when(playlistService.getPlaylistById(eq(1L), any(UUID.class), any(SongFilterDto.class), eq(0), eq(15)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(get("/api/v0/playlists/1")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnGetPlaylistByIdWithPagination() throws Exception {
        when(playlistService.getPlaylistById(eq(1L), any(UUID.class), any(SongFilterDto.class), eq(1), eq(20)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(get("/api/v0/playlists/1")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnAddSongToPlaylist() throws Exception {
        when(playlistService.addSongToPlaylist(eq(1L), eq(2L), any(UUID.class)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(post("/api/v0/playlists/1/songs/2")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnRemoveSongFromPlaylist() throws Exception {
        when(playlistService.removeSongFromPlaylist(eq(1L), eq(2L), any(UUID.class)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(delete("/api/v0/playlists/1/songs/2")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }


    @Test
    void returnsOkOnSearchSongsInPlaylist() throws Exception {
        when(playlistService.searchSongsInPlaylist(eq(1L), eq("highway"),
                isNull(), any(UUID.class), eq(0), eq(15)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v0/playlists/1/search")
                        .param("query", "highway")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnSearchSongsInPlaylistWithShareToken() throws Exception {
        when(playlistService.searchSongsInPlaylist(eq(1L), eq("highway"),
                eq("abc12345678"), any(UUID.class), eq(0), eq(15)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v0/playlists/1/search")
                        .param("query", "highway")
                        .param("shareToken", "abc12345678")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnGenerateShareToken() throws Exception {
        when(playlistService.generateShareToken(eq(1L), any(UUID.class)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(post("/api/v0/playlists/1/share")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnToggleVisibilityToPrivate() throws Exception {
        when(playlistService.toggleVisibility(eq(1L), eq(true), any(UUID.class)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(patch("/api/v0/playlists/1/private")
                        .param("value", "true")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnToggleVisibilityToPublic() throws Exception {
        when(playlistService.toggleVisibility(eq(1L), eq(false), any(UUID.class)))
                .thenReturn(mockPlaylistDto());

        mockMvc.perform(patch("/api/v0/playlists/1/private")
                        .param("value", "false")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returns401WhenNoJwt() throws Exception {
        mockMvc.perform(get("/api/v0/playlists"))
                .andExpect(status().isUnauthorized());
    }
}