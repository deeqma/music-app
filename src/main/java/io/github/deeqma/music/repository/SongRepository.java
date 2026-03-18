package io.github.deeqma.music.repository;

import io.github.deeqma.music.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long>, JpaSpecificationExecutor<Song> {

    boolean existsBySongNameAndArtistName(String songName, String artistName);
    boolean existsByFileHash(String fileHash);
    boolean existsBySongNameAndArtistNameAndIdNot(String songName, String artistName, Long id);

}

