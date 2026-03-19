package io.github.deeqma.music.controller;

import io.github.deeqma.music.SongTestData;
import io.github.deeqma.music.api.SongController;
import io.github.deeqma.music.config.SecurityConfig;
import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.service.SongService;
import io.github.deeqma.music.service.UploadSongService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.support.ResourceRegion;

import java.util.List;
import java.util.UUID;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SongController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SongControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UploadSongService uploadSongService;

    @MockitoBean
    private SongService songService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static final UUID TEST_USER_ID = UUID.randomUUID();

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor mockJwt() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject("testuser")
                        .claim("userId", TEST_USER_ID.toString()));
    }

    @Test
    void returnsCreatedOnSuccessfulUpload() throws Exception {
        when(uploadSongService.uploadSong(any(MultipartFile.class), any(CreateOrUpdateSongDto.class)))
                .thenReturn(SongTestData.highwayStarDto());

        mockMvc.perform(multipart("/api/v1/songs")
                        .file("file", "fake-mp3".getBytes())
                        .param("songName", "Highway Star")
                        .param("artistName", "Deep Purple")
                        .param("album", "Machine Head")
                        .param("genre", "Hard Rock")
                        .param("releaseYear", "1972")
                        .with(mockJwt()))
                .andExpect(status().isCreated());
    }

    @Test
    void returnsOkWithDefaultPagination() throws Exception {
        when(songService.getAllSongs(any(SongFilterDto.class), any(UUID.class), eq(0), eq(15)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/songs")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkWithCustomPagination() throws Exception {
        when(songService.getAllSongs(any(SongFilterDto.class), any(UUID.class), eq(1), eq(30)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/songs")
                        .param("page", "1")
                        .param("pageSize", "30")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkWithArtistFilter() throws Exception {
        when(songService.getAllSongs(any(SongFilterDto.class), any(UUID.class), eq(0), eq(15)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/songs")
                        .param("artistName", "Deep Purple")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnSearch() throws Exception {
        when(songService.searchSongs(eq("Highway"), any(UUID.class), eq(0), eq(15)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/songs/search")
                        .param("query", "Highway")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnGetLikedSongs() throws Exception {
        when(songService.getLikedSongs(any(SongFilterDto.class), any(UUID.class), eq(0), eq(15)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/songs/liked")
                        .with(mockJwt()))
                .andExpect(status().isOk());
    }

    @Test
    void returnsOkOnSuccessfulUpdate() throws Exception {
        when(songService.updateSong(eq(1L), any(CreateOrUpdateSongDto.class)))
                .thenReturn(SongTestData.highwayStarDto());

        mockMvc.perform(put("/api/v1/songs/1")
                        .with(mockJwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "songName": "Highway Star",
                          "artistName": "Deep Purple",
                          "releaseYear": 1972
                        }
                        """))
                .andExpect(status().isOk());
    }

    @Test
    void returnsNoContentOnSuccessfulDelete() throws Exception {
        doNothing().when(songService).deleteSong(1L);

        mockMvc.perform(delete("/api/v1/songs/1")
                        .with(mockJwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void returnsPartialContentOnSuccessfulStream() throws Exception {
        Resource resource = new ByteArrayResource("fake-mp3-content".getBytes());
        ResourceRegion region = new ResourceRegion(resource, 0, resource.contentLength());
        when(songService.StreamSong(eq(1L), any(HttpHeaders.class))).thenReturn(region);

        mockMvc.perform(get("/api/v1/songs/1/stream")
                        .with(mockJwt()))
                .andExpect(status().isPartialContent());
    }
}