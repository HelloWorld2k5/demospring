package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

// Error code chứa mã code và message của lỗi
@Getter
public enum ErrorCode {
    
    UNCATEGORIZED_ERROR(9999, "Uncategorized error!", HttpStatus.INTERNAL_SERVER_ERROR), // trả về lỗi 500

    // trả về 401 vì chưa được xác thực
    // lỗi 401 này GlobalException ko giải quyết được do nó bị chặn ngay ở tầng filter chưa vào đc Controller
    // ta sẽ config nó trong SecurityConfig
    UNAUTHENTICATED(1001, "Unauthenticated!", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1002, "Not have permission!", HttpStatus.FORBIDDEN), // trả về 403 vì ko có quyền
    KEY_INVALID(1003, "Invalid key message!", HttpStatus.BAD_REQUEST), // trả về lỗi 400
    USER_EXISTED(1004, "User existed!", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed!", HttpStatus.NOT_FOUND), // trả về 404 vì ko tìm thấy
    USER_NOT_FOUND(1006, "User not found!", HttpStatus.NOT_FOUND),
    USERNAME_INVALID(1007, "Username must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    PASSWORD_INVALID(1008, "Password must be at least {min} characters!", HttpStatus.BAD_REQUEST),
    DOB_INVALID(1009, "Age must be at least {min}", HttpStatus.BAD_REQUEST), // message trả về của trường hợp này sẽ linh động theo tham số min được truyền vào của annotation ràng buộc
    ROLE_NOT_FOUND(1010, "Role not found!", HttpStatus.NOT_FOUND);

    private ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private int code;
    private String message;
    private HttpStatusCode statusCode;

}