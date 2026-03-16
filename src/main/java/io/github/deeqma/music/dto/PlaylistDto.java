package io.github.deeqma.music.dto;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDto {
    private Long playlistId;
    private String playlistName;
    private String description;
    private String totalSongs;
    List<SongDto> songDtos = new ArrayList<>();


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

    public String getTotalSongs() {
        return totalSongs;
    }

    public void setTotalSongs(String totalSongs) {
        this.totalSongs = totalSongs;
    }

    public List<SongDto> getSongDtos() {
        return songDtos;
    }

    public void setSongDtos(List<SongDto> songDtos) {
        this.songDtos = songDtos;
    }
}
