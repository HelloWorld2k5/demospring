package com.example.demo.exception;

import java.util.Map;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.dto.response.ApiResponse;

import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;

/* 
Xử lý các exception một cách tập trung, giúp:
    - Controller không phải bắt các exception mà service throw ra
*/
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Trong GlobalException không xử lý được lỗi 401,
    // vì lỗi này chưa vào được controller mà bị chặn ngay ở tầng filter
    // Để xử lý được 401 thì ta sẽ xử lý trong SecurityConfig

    private static final String MIN_ATTRIBUTE = "min";

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

    // Handle exception được ném ra khi validate các fields trong dto
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    private ResponseEntity<ApiResponse<?>> handlingValidation(MethodArgumentNotValidException exception) {

        // Nếu message key nhận được ko đúng thì sao? -> bắn ra IllegalArgumentException và vẫn trả về res mặc định của spring
        // Cách giải quyết là tạo thêm 1 error code cho excep trên, rồi gán vào biến errorCode ở dưới
        // Sau đó đưa lệnh valueOf vào try-catch
        ErrorCode errorCode = ErrorCode.KEY_INVALID;

        var fieldError = exception.getFieldError();

        Map<String, Object> attributes = null;

        if (fieldError != null) {
            String enumKey = fieldError.getDefaultMessage();
            try {
                errorCode = ErrorCode.valueOf(enumKey);
            } catch (IllegalArgumentException e) {
                log.error(e.getMessage());
            }

            // Đối tượng này chứa tất cả thông tin chi tiết về một ràng buộc cụ thể đã bị vi phạm
            var constraintViolation = exception
                    .getBindingResult()
                    .getAllErrors()
                    .getFirst()
                    .unwrap(ConstraintViolation.class);

            // Lấy ra các attribute tức là các tham số hoặc thuộc tính của annotation ràng buộc bị
            // vi phạm khiến exception này được ném ra
            // Ví dụ vi phạm annotation DobConstraint(min = 18, message = "INVALID_DOB")
            // thì attributes sẽ là min và message
            // Mục đích việc lấy min là để thêm min vào trong dòng message response bắn ra
            attributes = constraintViolation.getConstraintDescriptor().getAttributes();

            log.info(attributes.toString());
            
        } else {
            errorCode = ErrorCode.UNCATEGORIZED_ERROR;
        }
        
        ApiResponse<?> apiResponse = new ApiResponse<>();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes)
                ? mapAttribute(errorCode.getMessage(), attributes) // nếu có attributes thì message là message đã được thay thế min được lấy từ min của annotation
                : errorCode.getMessage()); // ko có thì cứ set message mặc định của erroCode
        
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

    // Hàm này lấy message mặc định, rồi thay thế min value vào trong message đó
    private String mapAttribute(String message, Map<String, Object> attributes) {
        String minValue = attributes.get(MIN_ATTRIBUTE).toString();

        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
    
}
