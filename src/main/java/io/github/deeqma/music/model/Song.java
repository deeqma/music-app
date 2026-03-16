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


}