package io.github.deeqma.music.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateOrUpdateSongDto {

    @NotBlank(message = "Song name is required")
    private String songName;

    @NotBlank(message = "Artist name is required")
    private String artistName;

    private String album;

    private String genre;

    @Min(value = 1800, message = "Release year must be after 1800")
    @Max(value = 2035, message = "Release year must be before 2035")
    private int releaseYear;

    public String getSongName() { return songName; }
    public void setSongName(String songName) { this.songName = songName; }

    public String getArtistName() { return artistName; }
    public void setArtistName(String artistName) { this.artistName = artistName; }

    public String getAlbum() { return album; }
    public void setAlbum(String album) { this.album = album; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public int getReleaseYear() { return releaseYear; }
    public void setReleaseYear(int releaseYear) { this.releaseYear = releaseYear; }
}

