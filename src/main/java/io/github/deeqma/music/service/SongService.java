package io.github.deeqma.music.service;

import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.SongException;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@SuppressWarnings("unused")
@Service
public class SongService {

    private static final long MAX_FILE_SIZE = 31457280; // 30MB

    private final Logger log = LoggerFactory.getLogger(SongService.class);

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
        String name = StringUtils.hasText(originalFilename) ? originalFilename : "UNTITLED_MP3";
        return StringUtils.cleanPath(name);
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

    private void validateYear(int year) {
        if (year < 1800 || year > 2035) {
            throw new SongException(ErrorType.INVALID_DATE, "Year must be between 1800 and 2035");
        }
    }

}