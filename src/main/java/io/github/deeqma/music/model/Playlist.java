package io.github.deeqma.music.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlaylistVisibility visibility;

    @Column(unique = true)
    private String shareToken;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    @JsonBackReference
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "playlist_song",
            joinColumns = @JoinColumn(name = "playlist_id"),
            inverseJoinColumns = @JoinColumn(name = "song_id")
    )
    @JsonManagedReference
    private Set<Song> songs = new HashSet<>();

    public Playlist() {}
}
