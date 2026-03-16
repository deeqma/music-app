package io.github.deeqma.music.error;

public class UserException extends RuntimeException {

    private final ErrorType errorType;

    public UserException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}
