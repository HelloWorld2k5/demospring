package com.example.demo.configuration;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.example.demo.dto.response.ApiResponse;
import com.example.demo.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

// Bắt quả tang và xử lý những Request không có Token (Anonymous) hoặc
// Token bị sai/hết hạn khi cố tình truy cập vào các API cần bảo mật
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // Hàm commence sẽ tự động kích hoạt ngay khi có lỗi phân quyền xảy ra ở tầng Filter
    @Override
    public void commence(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;

        response.setStatus(errorCode.getStatusCode().value()); // set http status code cho response
        response.setContentType(MediaType.APPLICATION_JSON_VALUE); // định dạng response là dạng "application/json"

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        // objectMapper để convert object apiResponse thành string
        ObjectMapper objectMapper = new ObjectMapper();

        // Vì đang ở tầng Filter thấp (chưa vào Controller để Spring tự dùng @RestController convert JSON hộ),
        // bạn phải dùng hàm này để gõ thủ công chuỗi JSON đó thẳng vào luồng phản hồi (Response Body) của HTTP
        // gửi trả về cho Client.
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        // Ra lệnh cho hệ thống đóng gói và đẩy gói tin Response này đi ngay lập tức về phía Frontend,
        // không giữ lại trong bộ nhớ đệm (Buffer) nữa, đồng thời kết thúc Request tại đây luôn.
        response.flushBuffer();
    }
}
