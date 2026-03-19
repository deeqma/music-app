package io.github.deeqma.music.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SongException.class)
    public ResponseEntity<ErrorResponse> handleSongException(SongException ex) {
        HttpStatus status = resolveStatus(ex.getErrorType());
        return ResponseEntity.status(status).body(
                new ErrorResponse(ex.getErrorType(), status.value(), Instant.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(PlaylistException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistException(PlaylistException ex) {
        HttpStatus status = resolveStatus(ex.getErrorType());
        return ResponseEntity.status(status).body(
                new ErrorResponse(ex.getErrorType(), status.value(), Instant.now(), ex.getMessage())
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(
                new ErrorResponse(null, HttpStatus.BAD_REQUEST.value(), Instant.now(), message)
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).body(
                new ErrorResponse(null, HttpStatus.CONTENT_TOO_LARGE.value(), Instant.now(),
                        "File size exceeds the allowed limit")
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("handleGeneric: unhandled exception", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ErrorResponse(null, HttpStatus.INTERNAL_SERVER_ERROR.value(), Instant.now(),
                        "An unexpected error occurred")
        );
    }

    private HttpStatus resolveStatus(ErrorType errorType) {
        return switch (errorType) {
            case SONG_NOT_FOUND,
                 PLAYLIST_NOT_FOUND,
                 FILE_NOT_FOUND,
                 USER_NOT_FOUND,
                 NOT_FOUND -> HttpStatus.NOT_FOUND;
            case DUPLICATED_SONG,
                 SONG_ALREADY_IN_PLAYLIST,
                 PLAYLIST_ALREADY_EXISTS,
                 MP3_ALREADY_EXIST,
                 PLAYLIST_SHARE_ALREADY_EXISTS,
                 ALREADY_EXISTS -> HttpStatus.CONFLICT;
            case SONG_NOT_IN_PLAYLIST,
                 PLAYLIST_SHARE_NOT_ALLOWED -> HttpStatus.BAD_REQUEST;
            case FILE_STORAGE_ERROR,
                 TOKEN_GENERATION_FAILED -> HttpStatus.INTERNAL_SERVER_ERROR;
            case PLAYLIST_LIMIT_REACHED -> HttpStatus.FORBIDDEN;
            case BAD_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
        };
    }

}