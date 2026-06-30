package com.example.demo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.dto.response.ApiResponse;

/* 
Quản lý các exception một cách tập trung, giúp:
    - Controller không phải bắt các exception mà service throw ra
*/
@ControllerAdvice
public class GlobalException {

    // đây là handle excep ngoài ý muốn với errorCode ngoài
    @ExceptionHandler(value = Exception.class)
    private ResponseEntity<ApiResponse<String>> handlingRuntimeException(RuntimeException exception) {

        ApiResponse<String> apiResponse = new ApiResponse<>();

        apiResponse.setCode(ErrorCode.UNCATEGORIZED_ERROR.getCode());
        apiResponse.setMessage(ErrorCode.UNCATEGORIZED_ERROR.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Handle excep từ việc validate password
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    private ResponseEntity<ApiResponse<String>> handlingValidation(MethodArgumentNotValidException exception) {

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
        
        ApiResponse<String> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
        
        return ResponseEntity.badRequest().body(apiResponse);
    }

    // Handle app excep đây, lấy error code từ excep ra rồi đưa vào apiResponse
    @ExceptionHandler(value = AppException.class)
    private ResponseEntity<ApiResponse<String>> handlingAppException(AppException exception) {
    
        ErrorCode errorCode = exception.getErrorCode();
    
        ApiResponse<String> apiResponse = new ApiResponse<>();
    
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());
    
        return ResponseEntity.badRequest().body(apiResponse);
    }
    
}
