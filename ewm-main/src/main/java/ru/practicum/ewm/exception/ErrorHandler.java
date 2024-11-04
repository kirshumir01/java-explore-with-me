package ru.practicum.ewm.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateError;
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
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            MissingFormatArgumentException.class,
            MissingRequestHeaderException.class,
            NumberFormatException.class,
            IllegalArgumentException.class,
            ValidationException.class,
            MissingServletRequestParameterException.class,
            URIFormatException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ApiError handleMethodArgumentNotValidException(final Exception e) {
        log.info("Validation exception occurred - invalid arguments: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.BAD_REQUEST.name(),
                "Incorrectly made request.",
                String.format("Validation exception occurred - invalid arguments: %s", e.getMessage()),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ApiError handleConstraintViolentException(final ConstraintViolationException e) {
        return e.getConstraintViolations().stream()
                .map((constraintViolation -> {
                    log.info("Constraint violent exception occurred. {}: {}",
                            constraintViolation.getPropertyPath(), constraintViolation.getMessage());
                    return new ApiError(
                            HttpStatus.BAD_REQUEST.name(),
                            String.format("Invalid value of the %s parameter: %s",
                                    constraintViolation.getPropertyPath().toString(),
                                    constraintViolation.getMessage()),
                            e.getMessage(),
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }))
                .findFirst()
                .get();
    }

    @ExceptionHandler(EventDateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ApiError handleEventDateExceptionException(final EventDateException e) {
        log.info("Event date exception occurred: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "The event time does not meet the established criteria.",
                e.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public ApiError handleConflictException(final ConflictException e) {
        log.info("Conflict data exception occurred: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "Conflict data exception occurred.",
                e.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @ExceptionHandler(HibernateError.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public ApiError handleSQLException(final HibernateError e) {
        log.info("SQL exception occurred: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.CONFLICT.name(),
                "SQL exception occurred.",
                e.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ApiError handleNotFoundException(final NotFoundException e) {
        log.info("The required object was not found: {}", e.getMessage(), e);
        return new ApiError(
                HttpStatus.NOT_FOUND.name(),
                "The required object was not found.",
                e.getMessage(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
