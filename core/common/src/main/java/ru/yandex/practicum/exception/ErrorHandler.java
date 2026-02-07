package ru.yandex.practicum.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.exception.dto.ApiError;

import java.time.LocalDateTime;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return new ApiError(
                "The required object was not found.",
                e.getMessage(),
                "NOT_FOUND",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleValidation(final ValidationException e) {
        return new ApiError(
                "Integrity constraint has been violated.",
                e.getMessage(),
                "CONFLICT",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleExist(final ExistException e) {
        return new ApiError(
                "Integrity constraint has been violated.",
                e.getMessage(),
                "CONFLICT",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleExist(final ConflictException e) {
        return new ApiError(
                "Integrity constraint has been violated.",
                e.getMessage(),
                "CONFLICT",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequest(final MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String message = fieldError == null ? e.getMessage()
                : String.format("Field: %s. Error: %s. Value: %s",
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                Objects.toString(fieldError.getRejectedValue(), "null"));
        return new ApiError(
                "Incorrectly made request.",
                message,
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidDateRange(final InvalidDateRangeException e) {
        return new ApiError(
                "Incorrectly made request.",
                e.getMessage(),
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadJson(final HttpMessageNotReadableException e) {
        return new ApiError(
                "Incorrectly made request.",
                e.getMessage(),
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleOther(final Exception e) {
        return new ApiError(
                "Unexpected error occurred.",
                e.getMessage(),
                "INTERNAL_SERVER_ERROR",
                LocalDateTime.now()
        );
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParam(final MissingServletRequestParameterException e) {
        return new ApiError(
                "Incorrectly made request.",
                "Отсутствует обязательный параметр запроса: " + e.getParameterName(),
                "BAD_REQUEST",
                LocalDateTime.now()
        );
    }
}