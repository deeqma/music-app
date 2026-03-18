package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@SuppressWarnings("unused")
@Service
public class UploadSongService {

    private static final Logger log = LoggerFactory.getLogger(UploadSongService.class);

    private static final long MAX_FILE_SIZE = 31457280; // 30MB
    private final SongRepository songRepository;
    private final SongService songService;

    @Autowired
    public UploadSongService(SongRepository songRepository, SongService songService) {
        this.songRepository = songRepository;
        this.songService = songService;
    }

    public SongDto uploadSong(MultipartFile file, CreateOrUpdateSongDto dto) {

        log.info("uploadSong: uploading '{}' by '{}'", dto.getSongName(), dto.getArtistName());

        validate(file, dto);

        try {
            String fileHash = generateFileHash(file);
            String filePath = saveFile(file);

            Song song = new Song();
            song.setSongName(dto.getSongName());
            song.setArtistName(dto.getArtistName());
            song.setAlbum(dto.getAlbum());
            song.setGenre(dto.getGenre());
            song.setReleaseYear(dto.getReleaseYear());
            song.setFilePath(filePath);
            song.setDurationSeconds(extractDuration(filePath));
            song.setFileHash(fileHash);

            Song saved = songRepository.save(song);
            log.info("uploadSong: saved song ID {}", saved.getId());
            return songService.toDto(saved);

        } catch (IOException e) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Failed to upload the song", e);
        }
    }


    private void validate(MultipartFile file, CreateOrUpdateSongDto dto) {

        if (file.isEmpty()) {
            throw new SongException(ErrorType.FILE_NOT_FOUND, "File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "File size exceeds the 30MB limit");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!"mp3".equalsIgnoreCase(extension)) {
            throw new SongException(ErrorType.FILE_NOT_FOUND, "Only MP3 files are allowed");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("audio/mpeg")) {
            throw new SongException(ErrorType.FILE_NOT_FOUND, "Invalid content type. Only audio/mpeg is allowed");
        }

        if (songRepository.existsBySongNameAndArtistName(dto.getSongName(), dto.getArtistName())) {
            throw new SongException(ErrorType.DUPLICATED_SONG, "Song with this name and artist already exists");
        }

        try {
            String fileHash = generateFileHash(file);
            if (songRepository.existsByFileHash(fileHash)) {
                throw new SongException(ErrorType.MP3_ALREADY_EXIST, "This MP3 has already been uploaded");
            }
        } catch (IOException e) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Could not read file for validation", e);
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path storageDir = resolveStorageDirectory();
        String fileName = resolveFileName(file.getOriginalFilename());
        String filePath = storageDir.resolve(fileName).toString();
        file.transferTo(new File(filePath));
        return filePath;
    }

    private String generateFileHash(MultipartFile file) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(file.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Could not generate file hash", e);
        }
    }

    private int extractDuration(String filePath) {
        try {
            AudioFile audioFile = AudioFileIO.read(new File(filePath));
            return audioFile.getAudioHeader().getTrackLength();
        } catch (Exception _) {
            log.warn("extractDuration: could not read duration for '{}', defaulting to 0", filePath);
            return 0;
        }
    }

    private Path resolveStorageDirectory() {
        Path dir = Paths.get(System.getProperty("user.dir"),
                "src", "main", "resources", "localstorage", "mp3");
        File storageDir = dir.toFile();
        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Could not create storage directory");
        }
        return dir;
    }

    private String resolveFileName(String originalFilename) {
        String name = StringUtils.hasText(originalFilename) ? originalFilename : "untitled.mp3";
        return StringUtils.cleanPath(name);
    }

}