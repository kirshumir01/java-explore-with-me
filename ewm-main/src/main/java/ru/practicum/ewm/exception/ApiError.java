package ru.practicum.ewm.exception;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ApiError {
    private List<String> errors;
    private String message;
    private String reason;
    private String status;
    private String timestamp;

    public ApiError(String status, String reason, String message, String timestamp) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.timestamp = timestamp;
    }

    public ApiError(String status, String reason, String message, List<String> errors, String timestamp) {
        this.status = status;
        this.reason = reason;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
    }
}