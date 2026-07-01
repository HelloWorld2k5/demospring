package com.example.demo.service;

import com.example.demo.configuration.ApplicationInitConfig;
import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.UserRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

// import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
// import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@RequiredArgsConstructor
// @FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
@Slf4j // của lombok tạo 1 logger
public class AuthenticationService {
    
    private final UserRepository userRepository;

    @NonFinal // giúp spring ko tự động tiêm bean vào biến này
    @Value("${jwt.signerKey}") // để lấy dữ liệu từ application.yaml tiêm vào biến
    protected String signerKey;

    // PasswordEncoder tự động được tiêm bởi ApplicationContext (Container) do bên SecurityConfig file có tạo bean
    private final PasswordEncoder passwordEncoder;

    // Hàm xác thực token
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken(); // lấy token

        JWSVerifier verifier = new MACVerifier(signerKey.getBytes()); // tạo verifier

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian token hết hạn

        boolean verified = signedJWT.verify(verifier); // xác thực 2 chữ ký có khớp nhau?

        // Kết quả trả về là token đã được xác minh chưa, và token đã hết hạn chưa
        return IntrospectResponse.builder()
            .valid(verified && expirationTime.after(new Date()))
            .build();
    }


    // đây là hàm login
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean authenticated = passwordEncoder.matches(request.getPassword(), user.getPassword());

        if (!authenticated)
            throw new AppException(ErrorCode.UNAUTHENTICATED);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
            .token(token)
            .authenticated(authenticated)
            .build();
    }

    /* 
        JWT (json web token) là chuỗi gồm header.payload.signature
            - Header: chứa info về loại token, thuật toán mã hoá
            - Payload: chứa thông tin dữ liệu bạn muốn truyền đi (gọi là claims)
            - Signature: Bằng Header + Payload băm với 1 secret key
    */
    private String generateToken(User user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); // tạo header

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() // tạo claims
            .subject(user.getUsername())
            .issuer("truong2k5.com")
            .issueTime(new Date())
            .expirationTime(new Date(Instant.now().plus(1, ChronoUnit.HOURS).toEpochMilli()))
            .claim("scope", buildScope(user)) // Muốn chỉ admin mới có thể truy cập endpoin get /users ta tạo thêm claim scope gồm các roles của user
            .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject()); // tạo payload

        JWSObject jwsObject = new JWSObject(header, payload); // nhét header và payload vào jwt

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes())); // ký xác nhận, tức là tạo signature rồi nhét vào jwt
            return jwsObject.serialize(); // return jwt dưới dạng string
        } catch (JOSEException e) {
            log.error("Cannot create token!", e);
            throw new RuntimeException(e);
        }
    }

    // Hàm tạo value cho scope gồm các roles
    // VD: "scope" : "USER ADMIN"
    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" "); // mỗi roles cách nhau bởi 1 space

        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(s -> stringJoiner.add(s));
        }

        return stringJoiner.toString();
    }

}
