package io.github.deeqma.music.service;

import io.github.deeqma.music.dto.CreateOrUpdateSongDto;
import io.github.deeqma.music.dto.SongDto;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import io.github.deeqma.music.model.Song;
import io.github.deeqma.music.repository.SongRepository;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
@Service
public class UploadSongService {

    private static final Logger log = LoggerFactory.getLogger(UploadSongService.class);

    private static final long MAX_FILE_SIZE = 31457280;

    private final SongRepository songRepository;
    private final SongService songService;

    @Value("${storage.mp3.path}")
    private String storagePath;

    public UploadSongService(SongRepository songRepository, SongService songService) {
        this.songRepository = songRepository;
        this.songService = songService;
    }

    public SongDto uploadSong(MultipartFile file, CreateOrUpdateSongDto dto) {

        log.info("uploadSong: uploading '{}' by '{}'", dto.getSongName(), dto.getArtistName());

        try {
            String fileHash = generateFileHash(file);
            validateFile(file, dto, fileHash);

            String filePath = saveFile(file);
            log.info("uploadSong: file saved to '{}'", filePath);

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
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Failed to save file: " + e.getMessage(), e);
        }

    }

    private void validateFile(MultipartFile file, CreateOrUpdateSongDto dto, String fileHash) {

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

        if (songRepository.existsByFileHash(fileHash)) {
            throw new SongException(ErrorType.MP3_ALREADY_EXIST, "This MP3 has already been uploaded");
        }
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path storageDir = resolveStorageDirectory();
        Path filePath = storageDir.resolve(resolveFileName()).normalize();

        if (!filePath.toAbsolutePath().startsWith(storageDir.toAbsolutePath())) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Invalid file path detected");
        }

        log.info("saveFile: saving to '{}'", filePath.toAbsolutePath());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        return filePath.toString();
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
        } catch (Exception e) {
            log.warn("extractDuration: could not read duration for '{}', reason: {}", filePath, e.getMessage());
            return 0;
        }
    }

    private Path resolveStorageDirectory() {
        try {
            Path dir = Paths.get(storagePath).toRealPath();
            log.info("resolveStorageDirectory: using path '{}'", dir);
            File storageDir = dir.toFile();
            if (!storageDir.exists() && !storageDir.mkdirs()) {
                throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Could not create storage directory: " + dir);
            }
            return dir;
        } catch (IOException e) {
            throw new SongException(ErrorType.FILE_STORAGE_ERROR, "Could not resolve storage directory", e);
        }
    }

    private String resolveFileName() {
        return UUID.randomUUID().toString().replace("-", "") + ".mp3";
    }

}