package Rift.Radio.repository;

import Rift.Radio.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongRepository extends JpaRepository<Song, Long> {
    boolean existsBySongName(String songName);
    boolean existsByFilePath(String filePath);

    boolean existsBySongNameAndIdNot(String songName, Long id);

}

