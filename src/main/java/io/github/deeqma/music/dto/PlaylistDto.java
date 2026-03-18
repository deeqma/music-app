package io.github.deeqma.music.dto;

import io.github.deeqma.music.model.PlaylistVisibility;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDto {
    private Long playlistId;
    private String playlistName;
    private String description;
    private String slug;
    private PlaylistVisibility visibility;
    private int totalSongs;
    private String shareToken;
    private List<SongDto> songDtos = new ArrayList<>();

    public Long getPlaylistId() {
        return playlistId;
    }

    public void setPlaylistId(Long playlistId) {
        this.playlistId = playlistId;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public PlaylistVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PlaylistVisibility visibility) {
        this.visibility = visibility;
    }

    public int getTotalSongs() {
        return totalSongs;
    }

    public void setTotalSongs(int totalSongs) {
        this.totalSongs = totalSongs;
    }

    public String getShareToken() {
        return shareToken;
    }

    public void setShareToken(String shareToken) {
        this.shareToken = shareToken;
    }

    public List<SongDto> getSongDtos() {
        return songDtos;
    }

    public void setSongDtos(List<SongDto> songDtos) {
        this.songDtos = songDtos;
    }
}
