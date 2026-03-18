package io.github.deeqma.music.repository;

import io.github.deeqma.music.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    int countByOwnerId(UUID ownerId);
    boolean existsByName(String name);

    boolean existsByNameAndOwnerId(String name, UUID ownerId);
}
