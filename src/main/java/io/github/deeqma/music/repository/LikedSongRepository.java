package io.github.deeqma.music.repository;

import io.github.deeqma.music.model.LikedSong;
import io.github.deeqma.music.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikedSongRepository extends JpaRepository<LikedSong, Long> {
    Optional<LikedSong> findBySong(Song song);
}
