package com.example.demo.configuration;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

// import com.example.demo.enums.Role;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // bật authorize bằng method (PostAuthorize và PreAuthorize)
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
                //.requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_ADMIN") // endpoint với get method này chỉ cho admin truy cập, ban đầu là SCOPE_ADMIN, custome lại thành ROLE_ADMIN khi đó nên chuyển sang hasRole()
                // .requestMatchers(HttpMethod.GET, "/users").hasRole(Role.ADMIN.name())
                .anyRequest().authenticated()); // các endpoints khác phải được authenticate thì mới truy cập được

        /* - Kích hoạt BearerTokenAuthenticationFilter:
                + Lấy token ở header http request
                + gọi hàm jwtDecoder() định nghĩa ở dưới để check token
                + ok thì lấy dữ liệu trong payload từ token nhét vào SecurityContextHolder
           - Chốt cuối AuthorizationFilter vào SecurityConfig để check endpoints này cần quyền gì
             sau đó vào SecurityContextHolder xem có đủ quyền không (bằng cách xem có dữ liệu trong payload không)
        */
        httpSecurity.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwtConfigurer -> jwtConfigurer
                        .decoder(jwtDecoder())
                        .jwtAuthenticationConverter(jwtAuthenticationConverter())) // dùng hàm, custom lại prefix authority SCOPE_
                .authenticationEntryPoint(new JwtAuthenticationEntryPoint())); 
        // Dòng set authentication entry point bằng JwtAuthenticationEntryPoint là để báo tôi đang cấu hình ứng dụng này làm OAuth2 
        // Resource Server (xác thực bằng JWT). Nếu có bất kỳ thằng nào bị lỗi xác thực token (Token fake, Token hết hạn, không có 
        // Token...), ông đừng dùng cấu hình mặc định của ông nữa, mà hãy đá Request đó sang cho
        // class JwtAuthenticationEntryPoint của tôi xử lý!

        // Tắt chống csrf để chạy nhanh hơn vì dùng jwt lưu ở local storage nên không cần lo về bị tấn công csrf
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults()); // Cấu hình cors mặc định để frontend call api

        return httpSecurity.build();
    }

    // Hàm custom prefix
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

        // Do bên AuthenticationService đã chủ động thêm prefix ROLE_ ở ngay token nên ta không cần convert sang ROLE_ nữa
        // cứ để là string rỗng
        jwtGrantedAuthoritiesConverter.setAuthorityPrefix(""); // custome lại prefix của authority từ SCOPE_ thành ROLE_

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);

        return jwtAuthenticationConverter;
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

    // Tạo bean passwordEncoder để sử dụng ở nhiều nơi
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
