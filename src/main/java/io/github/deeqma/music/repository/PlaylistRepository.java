package io.github.deeqma.music.repository;

import io.github.deeqma.music.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long>, JpaSpecificationExecutor<Playlist> {

    int countByOwnerId(UUID ownerId);
    boolean existsByShareToken(String shareToken);
    boolean existsByNameAndOwnerId(String name, UUID ownerId);

    @Query("SELECT p FROM Playlist p WHERE p.visibility = 'PUBLIC' OR p.owner.id = :userId")
    List<Playlist> findAllPublicAndOwnedBy(@Param("userId") UUID userId);
    boolean existsByName(String playListName);
}
