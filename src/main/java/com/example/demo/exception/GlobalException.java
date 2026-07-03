package com.example.demo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

/* 
Quản lý các exception một cách tập trung, giúp:
    - Controller không phải bắt các exception mà service throw ra
*/
@ControllerAdvice
@Slf4j
public class GlobalException {

    // Trong GlobalException không xử lý được lỗi 401, vì lỗi này chưa vào được controller mà bị chặn ngay ở tầng filter
    // Để xử lý được 401 thì ta sẽ xử lý trong SecurityConfig


    // đây là handle excep ngoài ý muốn với errorCode ngoài
    @ExceptionHandler(value = Exception.class)
    private ResponseEntity<ApiResponse<?>> handlingException(Exception exception) {

        log.error("Critical error!", exception);

        ErrorCode errorCode = ErrorCode.UNCATEGORIZED_ERROR;

        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Handle excep từ việc validate password
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    private ResponseEntity<ApiResponse<?>> handlingValidation(MethodArgumentNotValidException exception) {

        // Nếu message key nhận được ko đúng thì sao? -> bắn ra IllegalArgumentException và vẫn trả về res mặc định của spring
        // Cách giải quyết là tạo thêm 1 error code cho excep trên, rồi gán vào biến errorCode ở dưới
        // Sau đó đưa lệnh valueOf vào try-catch
        ErrorCode errorCode = ErrorCode.INVALID_KEY;

        var fieldError = exception.getFieldError();

        if (fieldError != null) {
            String enumKey = fieldError.getDefaultMessage();
            try {
                errorCode = ErrorCode.valueOf(enumKey);
            } catch (IllegalArgumentException e) {
                
            }
        } else {
            errorCode = ErrorCode.UNCATEGORIZED_ERROR;
        }
        
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Handle app excep đây, lấy error code từ excep ra rồi đưa vào apiResponse
    @ExceptionHandler(value = AppException.class)
    private ResponseEntity<ApiResponse<?>> handlingAppException(AppException exception) {
    
        ErrorCode errorCode = exception.getErrorCode();
    
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
    
        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    }

    // Xử lý ngoại lệ bắn ra khi không có quyền truy cập vào endpoint
    @ExceptionHandler(value = AuthorizationDeniedException.class)
    private ResponseEntity<ApiResponse<?>> handlingAccessDeniedException(AuthorizationDeniedException exception) {

        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        return ResponseEntity.status(errorCode.getStatusCode()).body(apiResponse);
    } 
    
}
