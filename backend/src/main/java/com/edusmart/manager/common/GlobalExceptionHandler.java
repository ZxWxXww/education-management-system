package com.edusmart.manager.common;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ApiResponse<Object> handleStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        int code = status != null && status.is4xxClientError() ? 40000 + ex.getStatusCode().value() : 50000;
        return ApiResponse.error(code, ex.getReason() == null ? "请求失败" : ex.getReason());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Object> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ApiResponse.error(40001, msg.isBlank() ? "参数校验失败" : msg);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Object> handleOther(Exception ex) {
        return ApiResponse.error(50000, "服务器内部错误");
    }
}

