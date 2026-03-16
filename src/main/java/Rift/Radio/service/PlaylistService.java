package Rift.Radio.service;

import Rift.Radio.dto.CreatePlaylistDto;
import Rift.Radio.dto.PlaylistDto;
import Rift.Radio.dto.SongDto;
import Rift.Radio.error.ErrorType;
import Rift.Radio.error.PlaylistException;
import Rift.Radio.model.Playlist;
import Rift.Radio.model.Song;
import Rift.Radio.repository.PlaylistRepository;
import Rift.Radio.repository.SongRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository, SongRepository songRepository) {
        this.playlistRepository = playlistRepository;
        this.songRepository = songRepository;
    }

    public CreatePlaylistDto createPlaylist(CreatePlaylistDto dto) {
        if (playlistRepository.existsByName(dto.getPlaylistName())) {
            throw new PlaylistException(ErrorType.PLAYLIST_ALREADY_EXISTS,
                    "Playlist name already exists");
        }
        Playlist playlist = new Playlist();
        playlist.setName(dto.getPlaylistName());
        playlist.setDescription(dto.getDescription());

        playlistRepository.save(playlist);

        return dto;
    }

    public Playlist addSongToPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new PlaylistException(ErrorType.SONG_NOT_FOUND, "Song not found"));
        if (playlist.getSongs().contains(song)) {
            throw new PlaylistException(ErrorType.SONG_ALREADY_IN_PLAYLIST, "Song already in playlist");
        }
        playlist.getSongs().add(song);
        return playlistRepository.save(playlist);
    }

    public Playlist deleteSongFromPlaylist(Long playlistId, Long songId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(ErrorType.PLAYLIST_NOT_FOUND, "Playlist not found"));
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new PlaylistException(ErrorType.SONG_NOT_FOUND, "Song not found"));
        if (!playlist.getSongs().contains(song)) {
            throw new PlaylistException(ErrorType.SONG_NOT_IN_PLAYLIST, "Song not in playlist");
        }
        playlist.getSongs().remove(song);
        return playlistRepository.save(playlist);
    }

    public PlaylistDto listSongsInPlaylist(Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistException(
                        ErrorType.PLAYLIST_NOT_FOUND,
                        "Playlist not found"
                ));

        PlaylistDto dto = new PlaylistDto();
        dto.setPlaylistId(playlist.getId());
        dto.setPlaylistName(playlist.getName());
        dto.setDescription(playlist.getDescription());

        List<SongDto> songDtos = new ArrayList<>();

        if (playlist.getSongs() != null) {
            for (Song song : playlist.getSongs()) {
                SongDto songDto = new SongDto();
                songDto.setId(song.getId());
                songDto.setSongName(song.getSongName());
                songDto.setArtistName(song.getArtistName());
                songDto.setGenre(song.getGenre());
                songDto.setLiked(song.isLiked());
                songDto.setReleaseYear(song.getReleaseYear());
                songDto.setAlbum(song.getAlbum());
                songDto.setFilePath(song.getFilePath());
                songDtos.add(songDto);
            }
        }

        dto.setSongDtos(songDtos);

        dto.setTotalSongs(String.valueOf(songDtos.size()));

        return dto;
    }

    public List<PlaylistDto> listAllPlaylists() {
        List<Playlist> playlists = playlistRepository.findAll();
        List<PlaylistDto> result = new ArrayList<>();

        for (Playlist playlist : playlists) {
            PlaylistDto dto = new PlaylistDto();
            dto.setPlaylistId(playlist.getId());
            dto.setPlaylistName(playlist.getName());
            dto.setDescription(playlist.getDescription());
            dto.setTotalSongs(playlist.getTotalSongs());

            List<SongDto> songDtos = new ArrayList<>();
            if (playlist.getSongs() != null) {
                for (Song song : playlist.getSongs()) {
                    SongDto songDto = new SongDto();
                    songDto.setId(song.getId());
                    songDto.setSongName(song.getSongName());
                    songDto.setArtistName(song.getArtistName());
                    songDto.setGenre(song.getGenre());
                    songDto.setLiked(song.isLiked());
                    songDto.setReleaseYear(song.getReleaseYear());
                    songDto.setAlbum(song.getAlbum());
                    songDto.setFilePath(song.getFilePath());
                    songDtos.add(songDto);
                }
            }

            dto.setSongDtos(songDtos);
            result.add(dto);
        }

        return result;
    }

}

