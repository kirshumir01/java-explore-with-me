package ru.practicum.ewm.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.MissingFormatArgumentException;

@Slf4j
@RestControllerAdvice("ru.practicum.ewm")
public class ErrorHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingFormatArgumentException.class,
            MissingRequestHeaderException.class,
            ValidationException.class
    })


    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleMethodArgumentNotValidException(final Exception e) {
        log.error(e.getMessage());
        return new ErrorResponse("Data is not valid", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleBadRequest(final BadRequestException e) {
        log.error(e.getMessage());
        return new ErrorResponse("Bad request error", e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ErrorResponse handleThrowable(final Exception e) {
        log.error(e.getMessage());
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        e.printStackTrace(new PrintWriter(out, true, StandardCharsets.UTF_8));
        return new ErrorResponse("Internal server error", e.getMessage());
    }
}