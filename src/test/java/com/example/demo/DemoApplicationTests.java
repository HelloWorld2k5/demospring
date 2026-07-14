package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test") // đọc file application-test.yaml để cấu hình db, signer key,...
@TestPropertySource(locations = "file:.env") // Ép Spring nhặt file .env ngoài cùng nạp vào Test Context
class DemoApplicationTests {

	@Test
	void contextLoads() {
	}

}
