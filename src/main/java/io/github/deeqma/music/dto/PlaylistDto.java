package io.github.deeqma.music.dto;

import io.github.deeqma.music.model.PlaylistVisibility;

public class PlaylistDto {
    private Long playlistId;
    private String playlistName;
    private String description;
    private String slug;
    private PlaylistVisibility visibility;
    private int totalSongs;

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
}
