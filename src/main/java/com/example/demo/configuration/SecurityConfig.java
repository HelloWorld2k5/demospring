package com.example.demo.configuration;

import com.example.demo.controller.AuthenticationController;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Các endpoints mà ai cũng truy cập được
    private final String[] PUBLIC_ENDPOINTS = {
            "/users",
            "/auth/token",
            "/auth/introspect"
    };

    // Lấy signer key từ appication.yaml
    @Value("${jwt.signerKey}")
    private String signerKey;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        // cấp quyền cho endpoints post user, post token (login) và check token ai cũng
        // có thể truy cập được
        httpSecurity.authorizeHttpRequests(request -> request
                .requestMatchers(HttpMethod.POST, PUBLIC_ENDPOINTS).permitAll() // mở của cho 3 endpoints trên truy cập
                .anyRequest().authenticated()); // các endpoints khác phải được authenticate thì mới truy cập được

        /* - Kích hoạt BearerTokenAuthenticationFilter:
                + Lấy token ở header http request
                + gọi hàm jwtDecoder() định nghĩa ở dưới để check token
                + ok thì lấy dữ liệu trong payload từ token nhét vào SecurityContextHolder
           - Chốt cuối AuthorizationFilter vào SecurityConfig để check endpoints này cần quyền gì
             sau đó vào SecurityContextHolder xem có đủ quyền không (bằng cách xem có dữ liệu trong payload không)
        */
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwtConfigurer -> jwtConfigurer.decoder(jwtDecoder())));

        // Tắt chống csrf để chạy nhanh hơn vì dùng jwt lưu ở local storage nên không cần lo về bị tấn công csrf
        httpSecurity.csrf(csrf -> csrf.disable());

        return httpSecurity.build();
    }

    // Hàm check token
    @Bean
    JwtDecoder jwtDecoder() {
        SecretKeySpec secretKeySpec = new SecretKeySpec(signerKey.getBytes(), "HS512");

        return NimbusJwtDecoder
                .withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS512)
                .build();
    }
}
