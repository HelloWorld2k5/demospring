package com.example.demo.exception;


// Error code chứa mã code và message của lỗi
public enum ErrorCode {

    INVALID_KEY(1001, "Invalid key message!"),
    USER_EXISTED(1002, "User existed!"),
    USER_NOT_EXISTED(1003, "User not existed!"),
    USER_NOT_FOUND(1004, "User not found!"),
    PASSWORD_INVALID(1005, "Password must be at least 8 characters!"),
    UNAUTHENTICATED(1006, "Unauthenticated!"),
    UNCATEGORIZED_ERROR(9999, "Uncategorized error!");

    private ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}