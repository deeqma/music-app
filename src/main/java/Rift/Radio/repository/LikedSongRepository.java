package Rift.Radio.repository;

import Rift.Radio.model.LikedSong;
import Rift.Radio.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikedSongRepository extends JpaRepository<LikedSong, Long> {
    Optional<LikedSong> findBySong(Song song);
}
