package com.edusmart.manager.common;

@Deprecated
public class ApiResponse<T> extends Result<T> {

    public ApiResponse() {
    }

    public ApiResponse(int code, String message, T data) {
        super(code, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "ok", data);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}

