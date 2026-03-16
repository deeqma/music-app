package Rift.Radio.service;

import Rift.Radio.error.ErrorType;
import Rift.Radio.error.LikedException;
import Rift.Radio.model.LikedSong;
import Rift.Radio.model.Song;
import Rift.Radio.repository.LikedSongRepository;
import Rift.Radio.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LikedSongService {

    private final LikedSongRepository likedRepository;
    private final SongRepository songRepository;

    @Autowired
    public LikedSongService(LikedSongRepository likedRepository, SongRepository songRepository) {
        this.likedRepository = likedRepository;
        this.songRepository = songRepository;
    }

    @Transactional
    public synchronized LikedSong likeSong(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new LikedException(ErrorType.SONG_NOT_FOUND, "Song not found"));

        if (likedRepository.findBySong(song).isPresent()) {
            throw new LikedException(ErrorType.LIKED_SONG_ALREADY_EXISTS, "Song already liked");
        }

        LikedSong likedSong = new LikedSong();
        likedSong.setSong(song);
        song.setLiked(true);
        songRepository.save(song);
        return likedRepository.save(likedSong);
    }


    public List<Song> getAllLikedSongs() {
        return likedRepository.findAll().stream()
                .map(LikedSong::getSong)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeLikedSong(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new LikedException(ErrorType.SONG_NOT_FOUND, "Song not found"));
        LikedSong likedSong = likedRepository.findBySong(song)
                .orElseThrow(() -> new LikedException(ErrorType.LIKED_SONG_NOT_FOUND, "Liked song not found"));
        likedRepository.delete(likedSong);
        song.setLiked(false);
        songRepository.save(song);
    }

}
