package io.github.deeqma.music.repository;

import io.github.deeqma.music.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long>, JpaSpecificationExecutor<Playlist> {

    int countByOwnerId(UUID ownerId);
    boolean existsByShareToken(String shareToken);
    List<Playlist> findAllByOwnerId(UUID ownerId);
    boolean existsByNameAndOwnerId(String name, UUID ownerId);

}
