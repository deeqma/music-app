package io.github.deeqma.music.dto;

public class PlaylistDto {
    private Long playlistId;
    private String playlistName;
    private String slug;
    private boolean owner;

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

    public String getSlug() {
        return slug;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }
}
