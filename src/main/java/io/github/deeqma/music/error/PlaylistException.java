package io.github.deeqma.music.error;


public class PlaylistException extends RuntimeException {

    private final ErrorType errorType;

    public PlaylistException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public PlaylistException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
