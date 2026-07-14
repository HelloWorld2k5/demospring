package com.example.demo.controller;

import java.time.LocalDate;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.example.demo.dto.request.UserCreationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers // kích hoạt tính năng quản lý containers của junit 5
class UserControllerIntegrationTest {

    @Container // đây là 1 container cần khởi tạo
    // phải có static vì Docker MySQL sẽ chỉ khởi động đúng 1 lần duy nhất cho toàn bộ các hàm test trong class này.
    // Các hàm test sẽ dùng chung cục database đó. Cách này giúp bài test chạy cực nhanh
    // nếu ko có static thì cứ mỗi hàm test là tạo 1 container mới gây lãng phí tài nguyên
    // Dùng @Transactional nếu test chạy chung 1 container và kq của hàm test này có thể gây ảnh hướng
    // đến kq của hàm test khác, Transactional sẽ rollback data mỗi khi 1 hàm test chạy xong
    static final MySQLContainer<?> MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0.46-debian");

    @DynamicPropertySource // cấu hình thay cho file application
    static void configureDatasource(DynamicPropertyRegistry registry) {

        // Cú pháp MY_SQL_CONTAINER::getJdbcUrl là () -> MY_SQL_CONTAINER.getJdbcUrl()
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("app.init.admin.username", () -> "admin");
        registry.add("app.init.admin.password", () -> "admin2k5");

        registry.add("jwt.signer-key", () -> "this_is_a_secret_key_for_signing_jwt_trg2k5");
        registry.add("jwt.access-token-validity-in-seconds", () -> 2400);
        registry.add("jwt.refreshable-duration-in-seconds", () -> 240000);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserCreationRequest userCreationRequest; // dữ liệu đầu vào

    private LocalDate dob;

    @BeforeEach
    void initData() {
        dob = LocalDate.of(2005, Month.AUGUST, 21);

        userCreationRequest = UserCreationRequest.builder()
                .username("trg25")
                .password("12345678")
                .fullName("Nguyễn Thế Trưởng")
                .dob(dob)
                .build();
    }

    @Test
    void createUser_validRequest_success() throws Exception {
        // GIVEN: nơi thiết lập bối cảnh ban đầu (Context) và chuẩn bị tất cả
        // các dữ liệu đầu vào cần thiết cho bài test (Khởi tạo các biến, dữ liệu giả,...)
        String content = objectMapper.writeValueAsString(userCreationRequest);

        // WHEN: nơi thực hiện hành động chính cần kiểm thử, thường chỉ gồm đúng 1 dòng code
        // (hoặc tối đa 2 dòng) để kích hoạt cái hàm/phương thức mà bạn đang viết test cho nó
        var response = mockMvc.perform(
                        MockMvcRequestBuilders // tạo mock request http
                                .post("/users") // method POST gọi đến endpoint /user
                                .contentType(MediaType.APPLICATION_JSON_VALUE) // type response là dạng application/json
                                .content(
                                        content != null
                                                ? content
                                                : "")) // content là userCreationRequest dưới dạng string json
                .andExpect(MockMvcResultMatchers.status().isOk()) // mong muốn trả về http status code là 200
                .andExpect(MockMvcResultMatchers.jsonPath("code")
                        .value(1000)) // mong đợi prop "code" trong json response là 1000
                .andExpect(MockMvcResultMatchers.jsonPath("result.username").value("trg25"))
                .andExpect(MockMvcResultMatchers.jsonPath("result.fullName").value("Nguyễn Thế Trưởng"));

        log.info("Result: " + response.andReturn().getResponse().getContentAsString());
    }
}
