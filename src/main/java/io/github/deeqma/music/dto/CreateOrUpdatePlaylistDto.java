package io.github.deeqma.music.dto;

import io.github.deeqma.music.model.PlaylistVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateOrUpdatePlaylistDto {

    @NotBlank(message = "Playlist name is required")
    @Size(max = 50)
    @Pattern(regexp = "^[\\p{L}\\p{N} ]+$", message = "Playlist name can only contain letters, numbers and spaces")
    private String playlistName;

    private String description;

    private PlaylistVisibility visibility;

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

    public PlaylistVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(PlaylistVisibility visibility) {
        this.visibility = visibility;
    }
}
