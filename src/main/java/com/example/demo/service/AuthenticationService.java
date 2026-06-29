package com.example.demo.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;

// import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
// import lombok.experimental.FieldDefaults;

@Service
@Data
@RequiredArgsConstructor
// @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
public class AuthenticationService {
    
    private final UserRepository userRepository;

    public boolean authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

        return passwordEncoder.matches(request.getPassword(), user.getPassword());
    }

}
