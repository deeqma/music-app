package io.github.deeqma.music.error;

public class JwtException extends RuntimeException {

    private final ErrorType errorType;

    public JwtException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public JwtException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}