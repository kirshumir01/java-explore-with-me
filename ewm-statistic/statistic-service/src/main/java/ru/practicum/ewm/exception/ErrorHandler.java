package ru.practicum.ewm.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.MissingFormatArgumentException;

@Slf4j
@RestControllerAdvice("ru.practicum.ewm")
public class ErrorHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingFormatArgumentException.class,
            MissingRequestHeaderException.class,
            ValidationException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ApiError handleMethodArgumentNotValidException(final Exception e) {
        log.error("Validation exception occurred - invalid arguments: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Incorrectly made request.",
                String.format("Validation exception occurred - invalid arguments: %s", e.getMessage()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ApiError handleBadRequest(final BadRequestException e) {
        log.error("Bad request exception occurred: {}", e.getMessage(), e);
        return new ApiError(
                String.valueOf(HttpStatus.BAD_REQUEST),
                "Incorrectly made request.",
                String.format("Bad request exception occurred: %s", e.getMessage()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ApiError handleThrowable(final Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log.error(sw.toString());

        List<String> errors = Collections.singletonList(e.getMessage());

        return new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.name(),
                "Internal server error occurred.",
                e.getMessage(),
                errors,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}