package io.github.deeqma.music.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;


import java.util.HashSet;
import java.util.Set;


@Entity
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String songName;

    @Column(nullable = false)
    private String artistName;

    private String album;

    private String genre;

    @Column(nullable = false)
    private int releaseYear;

    @Column(nullable = false)
    private String filePath;

    private int durationSeconds;

    @Column(unique = true)
    private String fileHash;

    @OneToMany(mappedBy = "song", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @JsonManagedReference
    private Set<LikedSong> likedSongs = new HashSet<>();

    @ManyToMany(mappedBy = "songs")
    @JsonBackReference
    private Set<Playlist> playlists = new HashSet<>();


    public Song() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public Set<LikedSong> getLikedSongs() {
        return likedSongs;
    }

    public void setLikedSongs(Set<LikedSong> likedSongs) {
        this.likedSongs = likedSongs;
    }

    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
    }
}