package com.example.demo.service;

import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.demo.dto.request.AuthenticationRequest;
import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.request.LogoutRequest;
import com.example.demo.dto.response.AuthenticationResponse;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.entity.InvalidatedToken;
import com.example.demo.entity.User;
import com.example.demo.exception.AppException;
import com.example.demo.exception.ErrorCode;
import com.example.demo.repository.InvalidatedTokenRepository;
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

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

@Service
@Data
@RequiredArgsConstructor
@Slf4j // của lombok tạo 1 logger
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @NonFinal // giúp spring ko tự động tiêm bean vào biến này
    @Value("${jwt.signer-key}") // để lấy dữ liệu từ application.yaml tiêm vào biến
    protected String signerKey; // chữ ký token

    @NonFinal
    @Value("${jwt.access-token-validity-in-seconds}") // lấy dữ liệu từ file application.yaml
    protected long accessTokenValidityInSeconds; // thời gian sống của token (tính bằng giây)

    // PasswordEncoder tự động được tiêm bởi ApplicationContext (Container) do bên PasswordConfig file có tạo bean
    private final PasswordEncoder passwordEncoder;

    // Hàm xác thực token
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken(); // lấy token

        boolean isValid = true;

        try {
            verifyToken(token);
        } catch (AppException e) { // bắt lỗi throw ra để trả về json, valid = false
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
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

    // đây là hàm logout
    public void logout(LogoutRequest request) throws JOSEException, ParseException {
        SignedJWT signedToken = verifyToken(request.getToken());

        String jti = signedToken.getJWTClaimsSet().getJWTID(); // lấy claim id ở trong token 
        Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn

        // tạo 1 bản ghi token đã logout
        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expirationTime(expirationTime.toInstant())
                .build();

        // lưu vào table trong db
        invalidatedTokenRepository.save(invalidatedToken);
    }

    // Hàm verify token, nếu invalid thì throw AppException, valid thì trả về signedJWT phục vụ cho hàm logout
    private SignedJWT verifyToken(String token) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes()); // tạo verifier

        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn token

        boolean verified = signedJWT.verify(verifier); // xác thực 2 chữ ký có khớp nhau?

        // Đùng dược cả cho introspect và logout vì đều phải xác thực token có chữ ký hợp lệ và
        // chưa hết hạn vì nếu hết hạn thì làm sao vẫn đang ở login mà logout
        if (!(verified && expirationTime.after(new Date()))) {
            log.error("Error verify token or expirated token!");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        // Nếu token này đã nằm trong bảng (tức là token này đã bị logout) thì cũng thow ra unauthenticated
        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            log.error("This token has been logout!");
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    /* 
        JWT (json web token) là chuỗi gồm header.payload.signature
            -  Header: chứa info về loại token, thuật toán mã hoá
            - Payload: chứa thông tin dữ liệu bạn muốn truyền đi (gọi là claims)
            - Signature: Bằng Header + Payload băm với 1 secret key
    */
    private String generateToken(User user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); // tạo header

        // thời gian hiện tại 
        Instant now = Instant.now();
        // thời gian token hết hạn = hiện tại + số giây token sống
        Instant expirationTime = now.plusSeconds(accessTokenValidityInSeconds);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() // tạo claims
                .subject(user.getUsername())
                .issuer("truong2k5.com")
                .issueTime(Date.from(now)) // thời gian issue token
                .expirationTime(Date.from(expirationTime)) // thời gian hết hạn token
                .jwtID(UUID.randomUUID().toString()) // thêm vào token cái id của token đó
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

            // Duyệt từng role của user
            user.getRoles().forEach(role -> {
                // rồi add từng role vào scope của token
                // ta cũng chủ động thêm prefix ROLE_ vào role rồi nên bên security config ở chỗ
                // converter không cần thêm prefix là role nữa
                // Làm việc này dể phân biệt trong scope đâu là role đâu là permission
                // role sẽ có prefix ROLE_ đằng trước, permission sẽ không có prefix chỉ có name thôi
                stringJoiner.add("ROLE_" + role.getName());

                // Duyệt từng permissions của mỗi role rồi cũng add vào scope của token 
                // Khi đó scope trong token có dạng: "scope" : "ADMIN CREATE_POST APPROVE_POST REJECT_POST"
                if (!CollectionUtils.isEmpty(role.getPermissions()))
                    role.getPermissions().forEach(permission -> stringJoiner.add(permission.getName()));
            });
        }

        return stringJoiner.toString();
    }

}
