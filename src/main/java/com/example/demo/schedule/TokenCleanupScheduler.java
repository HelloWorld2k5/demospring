package com.example.demo.schedule;

// import java.time.Instant;

// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

// import com.example.demo.repository.InvalidatedTokenRepository;

// import lombok.AccessLevel;
// import lombok.RequiredArgsConstructor;
// import lombok.experimental.FieldDefaults;
// import lombok.extern.slf4j.Slf4j;

// @Component
// @RequiredArgsConstructor
// @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
// @Slf4j
// public class TokenCleanupScheduler {

//     InvalidatedTokenRepository invalidatedTokenRepository;

//     // Chạy mỗi 2 ngày một lần để xoá các token hết hạn
//     @Scheduled(fixedRate = 172800000) // 2 ngày = 2 * 24 * 60 * 60 * 1000 = 172800000 ms
//     public void cleanupExpiredTokens() {
//         log.info("Starting cleanup of expired tokens...");
//         invalidatedTokenRepository.deleteByExpirationTimeBefore(Instant.now());
//         log.info("Expired tokens cleanup completed.");
//     }
// }
