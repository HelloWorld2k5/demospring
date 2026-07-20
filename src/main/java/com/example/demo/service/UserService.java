package com.example.demo.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.dto.request.UserCreationRequest;
import com.example.demo.dto.request.UserUpdateRequest;
import com.example.demo.dto.response.UserResponse;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.mapper.UserMappper;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor // tạo constructor có tham số với các field final để tiêm bean
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMappper userMapper;

    // Mã hoá mật khẩu bằng BCrypt của dependency spring security
    private final PasswordEncoder passwordEncoder;

    public UserResponse createUser(UserCreationRequest request) {
        log.info("UserService: create user");

        // Khi column username đã có unique thì không cần phải check trùng username ở đây nữa

        // Dùng mapper ta không cần set liên tục như này nữa
        // User user = new User();
        // user.setUsername(request.getUsername());
        // user.setFullName(request.getFullName());
        // user.setPassword(request.getPassword());
        // user.setDob(request.getDob());

        User user = userMapper.toUser(request); // duy nhất 1 dòng

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // HashSet<String> roles = new HashSet<>();
        // roles.add(Role.USER.name()); // tạo roles mặc định cho user mới tạo

        // Role mặc định là USER, ko có role user vứt exception
        Role defaultRole = roleRepository.findById("USER")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.setRoles(Set.of(defaultRole)); // set roles

        return userMapper.toUserResponse(userRepository.save(user));
    }

    // Khi truy cập đến hàm này, thì PreAuthorize sẽ lấy biểu thức tính toán check
    // role
    // Nếu ok thì mới chạy logic trong Hàm
    // Nếu ko ok thì ném 403 (AccessDeniedException) và code trong hàm ko chạy
    // @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("hasRole('admin')") // có quyền
    public List<UserResponse> getAllUsers() {

        // Log sẽ hiện (hàm chạy) sau khi PreAuthorize check role thành công
        log.info("In getAllUsers method!");
        return userRepository.findAll().stream()
                .map(user -> userMapper.toUserResponse(user))
                .toList();
    }

    // PostAuthorize vẫn cho hàm chạy bình thường
    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUserById(String userId) {

        log.info("In getUserById method!");

        // Ngay trước khi return về kết quả, spring gói data lại và check biểu thức
        // trong PostAuthorize
        // Nếu đúng thì mới trả data
        // Nếu ko ok thì ném 403 (AccessDeniedException) và giấu data đã lưu

        if (userId != null)
            return userMapper.toUserResponse(
                    userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
        return null;
    }

    // Hàm này giúp khi đang đăng nhập ta có thể lấy info của chính mình mà ko cần
    // id (endpoint: "/users/myInfo")
    public UserResponse getMyInfo() {

        // Khi đang đăng nhập tức là trong SecurityContextHolder có dữ liệu username và
        // role
        // Lấy context trong securitycontextholder
        var context = SecurityContextHolder.getContext();
        var authentication = context.getAuthentication();

        // trong context lấy username
        String username = authentication.getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
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

        user.setPassword(passwordEncoder.encode(request.getPassword()));

        List<Role> roles = roleRepository.findAllById(request.getRoles());
        user.setRoles(new HashSet<>(roles));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    public void deleteUserById(String userId) {
        if (userId != null) {
            userRepository.deleteById(userId);
        }
    }
}
