package io.github.deeqma.music.repository;

import io.github.deeqma.music.model.LikedSong;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LikedSongRepository extends JpaRepository<LikedSong, Long> {

    @Query("SELECT ls.song.id FROM LikedSong ls WHERE ls.user.id = :userId")
    Set<Long> findSongIdsByUserId(@Param("userId") UUID userId);

    boolean existsBySongIdAndUserId(Long songId, UUID userId);
    void deleteBySongIdAndUserId(Long songId, UUID userId);
    List<LikedSong> findAllByUserId(UUID userId, Pageable pageable);
}
