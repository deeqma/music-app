package io.github.deeqma.music.controller;

import io.github.deeqma.music.SongTestData;
import io.github.deeqma.music.api.SongController;
import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongFilterDto;
import io.github.deeqma.music.service.SongService;
import io.github.deeqma.music.service.UploadSongService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.support.ResourceRegion;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = SongController.class)
@ActiveProfiles("test")
class SongControllerTest {

    @Autowired
    private MockMvcTester mockMvcTester;

    @MockitoBean
    private UploadSongService uploadSongService;

    @MockitoBean
    private SongService songService;

    @Test
    void returnsCreatedOnSuccessfulUpload() {
        when(uploadSongService.uploadSong(any(MultipartFile.class), any(CreateOrUpdateSongDto.class)))
                .thenReturn(SongTestData.highwayStarDto());

        assertThat(mockMvcTester.post()
                .uri("/api/v1/songs")
                .multipart()
                .file("file", "fake-mp3".getBytes())
                .param("songName", "Highway Star")
                .param("artistName", "Deep Purple")
                .param("album", "Machine Head")
                .param("genre", "Hard Rock")
                .param("releaseYear", "1972")
                .exchange())
                .hasStatus(HttpStatus.CREATED);
    }

    @Test
    void returnsOkWithDefaultPagination() {
        when(songService.getAllSongs(0, 15)).thenReturn(List.of());

        assertThat(mockMvcTester.get()
                .uri("/api/v1/songs")
                .exchange())
                .hasStatusOk();
    }

    @Test
    void returnsOkWithCustomPagination() {
        when(songService.getAllSongs(1, 30)).thenReturn(List.of());

        assertThat(mockMvcTester.get()
                .uri("/api/v1/songs")
                .param("page", "1")
                .param("pageSize", "30")
                .exchange())
                .hasStatusOk();
    }

    @Test
    void returnsOkOnSuccessfulUpdate() {
        when(songService.updateSong(eq(1L), any(CreateOrUpdateSongDto.class)))
                .thenReturn(SongTestData.highwayStarDto());

        assertThat(mockMvcTester.put()
                .uri("/api/v1/songs/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "songName": "Highway Star",
                          "artistName": "Deep Purple",
                          "releaseYear": 1972
                        }
                        """)
                .exchange())
                .hasStatusOk();
    }

    @Test
    void returnsOkWithNoFilters() {
        when(songService.filterSongs(any(SongFilterDto.class), eq(0), eq(15)))
                .thenReturn(List.of());

        assertThat(mockMvcTester.get()
                .uri("/api/v1/songs/filter")
                .exchange())
                .hasStatusOk();
    }

    @Test
    void returnsOkWithArtistFilter() {
        when(songService.filterSongs(any(SongFilterDto.class), eq(0), eq(15)))
                .thenReturn(List.of());

        assertThat(mockMvcTester.get()
                .uri("/api/v1/songs/filter")
                .param("artistName", "Deep Purple")
                .exchange())
                .hasStatusOk();
    }

    @Test
    void returnsNoContentOnSuccessfulDelete() {
        doNothing().when(songService).deleteSong(1L);

        assertThat(mockMvcTester.delete()
                .uri("/api/v1/songs/1")
                .exchange())
                .hasStatus(HttpStatus.NO_CONTENT);
    }

    @Test
    void returnsPartialContentOnSuccessfulStream() throws IOException {
        Resource resource = new ByteArrayResource("fake-mp3-content".getBytes());
        ResourceRegion region = new ResourceRegion(resource, 0, resource.contentLength());
        when(songService.StreamSong(eq(1L), any(HttpHeaders.class))).thenReturn(region);

        assertThat(mockMvcTester.get()
                .uri("/api/v1/songs/1/stream")
                .exchange())
                .hasStatus(HttpStatus.PARTIAL_CONTENT);
    }
}
