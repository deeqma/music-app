package io.github.deeqma.music.error;

public class SongException extends RuntimeException {

    private final ErrorType errorType;

    public SongException(ErrorType errorType) {
        this.errorType = errorType;
    }

    public SongException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public SongException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}