package io.github.deeqma.music.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;


@Entity
@SuppressWarnings("unused")
public class LikedSong {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @ManyToOne
    @JoinColumn(name = "song_id", nullable = false)
    @JsonBackReference
    private Song song;

    public LikedSong() {}

}
