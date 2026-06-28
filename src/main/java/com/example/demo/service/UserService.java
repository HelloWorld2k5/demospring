package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor // tạo constructor có tham số với các field final để tiêm bean
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // tạo fields private và final
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    public User createUser(UserCreationRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        // Dùng mapper ta không cần set liên tục như này nữa
        // User user = new User();
        // user.setUsername(request.getUsername());
        // user.setFullName(request.getFullName());
        // user.setPassword(request.getPassword());
        // user.setDob(request.getDob());

        User user = userMapper.toUser(request); // duy nhất 1 dòng

        if (user != null) {
            return userRepository.save(user);
        }

        throw new AppException(ErrorCode.UNCATEGORIZED_ERROR); // user null
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserResponse getUserById(String userId) {
        if (userId != null)
            return userMapper.toUserResponse(
                    userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
        return null;
    }

    public UserResponse updateUserById(String userId, UserUpdateRequest request) {

        if (userId == null) {
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.UNCATEGORIZED_ERROR));

        // Dùng mapper, mapping target ta không set từng fields nữa
        // user.setFullName(request.getFullName());
        // user.setPassword(request.getPassword());
        // user.setDob(request.getDob());

        userMapper.updateUser(user, request); // chỉ cần 1 dòng, tự động map từ request sang user

        if (user == null)
            throw new AppException(ErrorCode.UNCATEGORIZED_ERROR);

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUserById(String userId) {
        if (userId != null) {
            userRepository.deleteById(userId);
        }
    }
}
