package io.github.deeqma.music.dto;

import java.util.UUID;

public class UserProfileDto {

    private UUID userId;
    private String username;
    private int totalLikedSongs;
    private int totalPlaylists;
    private int playlistLimit;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getTotalLikedSongs() {
        return totalLikedSongs;
    }

    public void setTotalLikedSongs(int totalLikedSongs) {
        this.totalLikedSongs = totalLikedSongs;
    }

    public int getTotalPlaylists() {
        return totalPlaylists;
    }

    public void setTotalPlaylists(int totalPlaylists) {
        this.totalPlaylists = totalPlaylists;
    }

    public int getPlaylistLimit() {
        return playlistLimit;
    }

    public void setPlaylistLimit(int playlistLimit) {
        this.playlistLimit = playlistLimit;
    }
}