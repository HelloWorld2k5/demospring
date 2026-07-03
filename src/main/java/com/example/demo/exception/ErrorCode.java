package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import jakarta.annotation.Nonnull;
import lombok.Getter;

// Error code chứa mã code và message của lỗi
@Getter
public enum ErrorCode {

    INVALID_KEY(1001, "Invalid key message!", HttpStatus.BAD_REQUEST), // trả về lỗi 400
    USER_EXISTED(1002, "User existed!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003, "User not existed!", HttpStatus.NOT_FOUND), // trả về 404 vì ko tìm thấy
    USER_NOT_FOUND(1004, "User not found!", HttpStatus.NOT_FOUND),
    PASSWORD_INVALID(1005, "Password must be at least 8 characters!", HttpStatus.BAD_REQUEST),

    // trả về 401 vì chưa được xác thực
    // lỗi 401 này GlobalException ko giải quyết được do nó bị chặn ngay ở tầng filter chưa vào đc Controller
    // ta sẽ config nó trong SecurityConfig
    UNAUTHENTICATED(1006, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "Not have permission!", HttpStatus.FORBIDDEN), // trả về 403 vì ko có quyền
    UNCATEGORIZED_ERROR(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR); // trả về lỗi 500

    private ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    @Nonnull
    private HttpStatusCode statusCode;

}