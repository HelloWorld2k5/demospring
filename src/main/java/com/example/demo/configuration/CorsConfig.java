package com.example.demo.configuration;

import org.springframework.web.filter.CorsFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Cors config cho frontend truy cập
@Configuration
public class CorsConfig {
    
    @Bean
    CorsFilter corsFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        corsConfiguration.addAllowedOrigin("http://localhost:5173"); // cho phép trang web này truy cập api
        corsConfiguration.addAllowedHeader("*"); // cho phép header nào cũng được
        corsConfiguration.addAllowedMethod("*"); // cho phép trang web gọi api với http method nào cx đc

        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();

        // gắn cors config cho tất cả các endpoints của hệ thống
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(urlBasedCorsConfigurationSource);
    }

}
