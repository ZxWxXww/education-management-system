package com.edusmart.manager.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public Result<Object> handleStatus(ResponseStatusException ex) {
        log.warn("Request failed with status exception: {}", ex.getReason(), ex);
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        int code = mapStatusCode(status);
        return Result.error(code, ex.getReason() == null ? "\u8bf7\u6c42\u5931\u8d25" : ex.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Object> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Request validation failed: {}", msg);
        return Result.error(40001, msg.isBlank() ? "\u53c2\u6570\u6821\u9a8c\u5931\u8d25" : msg);
    }

    @ExceptionHandler(Exception.class)
    public Result<Object> handleOther(Exception ex) {
        log.error("Unhandled server exception", ex);
        return Result.error(50000, "\u670d\u52a1\u5668\u5185\u90e8\u9519\u8bef");
    }

    private int mapStatusCode(HttpStatus status) {
        if (status == null) {
            return 50000;
        }
        return switch (status) {
            case BAD_REQUEST -> 40001;
            case UNAUTHORIZED -> 40101;
            case FORBIDDEN -> 40301;
            case NOT_FOUND -> 40404;
            default -> status.is4xxClientError() ? 40000 + status.value() : 50000;
        };
    }
}
