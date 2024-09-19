package ru.practicum.ewm.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    String error;

    String description;

    public ErrorResponse(String error, String description) {
        this.error = error;
        this.description = description;
    }
}
