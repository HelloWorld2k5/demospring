package com.example.demo.controller;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.response.PermissionResponse;
import com.example.demo.dto.response.RoleResponse;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
// Test thì không tự động lấy biến môi trg từ file env được nên phải khai báo ở đây
@SpringBootTest
@ActiveProfiles("test") // đọc file application-test.yaml để cấu hình db, signer key,...
@TestPropertySource(locations = "file:.env") // Ép Spring nhặt file .env ngoài cùng nạp vào Test Context
@AutoConfigureMockMvc // tự động cấu hình và khởi tạo đối tượng MockMvc trong các bài kiểm thử
// JUNit5 có thể lỏng lẻo access modifier hơn Junit4 (bắt buộc phải public)
// giở Junit5 không cần viết access modifier để default
class UserControllerTest {

    @Autowired // Nên dùng Autowired trong môi trg chạy test
    // công cụ giả lập các request http gửi lên controller (được cấu hình đầy đủ filter, security để bắn request đi
    // test)
    private MockMvc mockMvc;

    @Autowired
    // spring boot đã cấu hình sẵn objectMapper hỗ trợ JavaTimeModule (hỗ trợ LocalDate)
    // để chuyển object thành string, chỉ việc inject vào dùng
    private ObjectMapper objectMapper;

    @MockitoBean
    // giả lập bean UserService để chạy vì hàm này chỉ chạy code của nó chứ không chạy đến tầng service nên ta mock nó
    private UserService userService;

    private UserCreationRequest userCreationRequest; // dữ liệu đầu vào
    private UserResponse userResponse; // dữ liệu đầu ra
    private RoleResponse roleResponse; // giả lập thêm dữ liệu đầu ra là các roles mặc định khi createUser
    private PermissionResponse permissionResponse; // các permissions của role

    private LocalDate dob;

    // annotation BeforeEach này sẽ giúp hàm initData chạy trước khi các test case chạy
    // mục đích là mỗi test case sẽ cần dữ liệu đầu vào và đầu ra nên ta sẽ set cho chúng bằng hàm initData
    // nên cứ mỗi khi có test case chạy thì hàm này sẽ chạy trc
    @BeforeEach
    void initData() {
        dob = LocalDate.of(2005, Month.AUGUST, 21);

        permissionResponse = PermissionResponse.builder()
                .name("CREATE_POST")
                .description("Create a new post")
                .build();

        roleResponse = RoleResponse.builder()
                .name("USER")
                .description("Role user")
                .permissions(Set.of(permissionResponse))
                .build();

        userCreationRequest = UserCreationRequest.builder()
                .username("trg25")
                .password("12345678")
                .fullName("Nguyễn Thế Trưởng")
                .dob(dob)
                .build();

        userResponse = UserResponse.builder()
                .userId("d309f29v02940vjew")
                .username("trg25")
                .fullName("Nguyễn Thế Trưởng")
                .dob(dob)
                .roles(Set.of(roleResponse))
                .build();
    }

    @Test
    void createUser_validRequest_success() throws Exception {
        // GIVEN: nơi thiết lập bối cảnh ban đầu (Context) và chuẩn bị tất cả
        // các dữ liệu đầu vào cần thiết cho bài test (Khởi tạo các biến, dữ liệu giả,...)
        String content = objectMapper.writeValueAsString(userCreationRequest);

        // Khi userService gọi đến method createUser thay vì hàm đó chạy thì ta sẽ trả luôn về response
        when(userService.createUser(ArgumentMatchers.any())).thenReturn(userResponse);

        // WHEN: nơi thực hiện hành động chính cần kiểm thử, thường chỉ gồm đúng 1 dòng code
        // (hoặc tối đa 2 dòng) để kích hoạt cái hàm/phương thức mà bạn đang viết test cho nó
        mockMvc.perform(
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
                .andExpect(MockMvcResultMatchers.jsonPath("result.userId").value("d309f29v02940vjew"));

        // THEN: nơi đối chiếu và kiểm tra kết quả xem hệ thống có chạy đúng như kỳ vọng của bạn hay không
        // Trong hàm này thì THEN chính là các method andExpect()
    }

    @Test
    void createUser_invalidUsername_fail() throws Exception {
        // GIVEN
        userCreationRequest.setUsername("trg"); // set cho username invalid
        String content = objectMapper.writeValueAsString(userCreationRequest);

        // ở đây không phải when khi userService gọi đến hàm createUser nữa
        // vì nếu username được validate ngay tầng trên vậy khi
        // username invalid thì sẽ throw ngay ra exception chứ không đi vào code bên trong

        // WHEN, THEN
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content != null ? content : ""))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("code").value(1007))
                .andExpect(MockMvcResultMatchers.jsonPath("message").value("Username must be at least 5 characters!"));
    }
}
