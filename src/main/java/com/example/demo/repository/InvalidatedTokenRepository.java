package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.InvalidatedToken;

// import java.time.Instant;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
    // void deleteByExpirationTimeBefore(Instant now);
}
