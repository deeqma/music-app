package io.github.deeqma.music.error;

import java.time.Instant;


public class ErrorResponse {

    private final ErrorType errorType;
    private final int status;
    private final Instant timestamp;
    private final String message;

    public ErrorResponse(ErrorType errorType, int status, Instant timestamp, String message) {
        this.errorType = errorType;
        this.status = status;
        this.timestamp = timestamp;
        this.message = message;
    }

    public ErrorType getErrorType() { return errorType; }
    public int getStatus() { return status; }
    public Instant getTimestamp() { return timestamp; }
    public String getMessage() { return message; }

}