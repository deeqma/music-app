package io.github.deeqma.music.error;

public class LikedException extends RuntimeException {

    private final ErrorType errorType;

    public LikedException(ErrorType errorType) {
        this.errorType = errorType;
    }

    public LikedException(ErrorType errorType, String message) {
        super(message); this.errorType = errorType;
    }

    public LikedException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause); this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

}
