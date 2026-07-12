package com.example.demo.service;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import com.example.demo.dto.request.RefreshRequest;
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
    protected long accessTokenValidityInSeconds; // thời gian sống của access token (tính bằng giây)

    @NonFinal
    @Value("${jwt.refreshable-duration-in-seconds}")
    protected long refreshableDurationInSeconds; //

    // PasswordEncoder tự động được tiêm bởi ApplicationContext (Container) do bên PasswordConfig file có tạo bean
    private final PasswordEncoder passwordEncoder;

    // Hàm xác thực token
    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken(); // lấy token

        boolean isValid = true;

        try {
            verifyToken(token, false);
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

        /*
            Giải thích tại sao hàm logout lại verifytoken có isRefresh = true:
            Nếu có 1 trường hợp là token hết hạn nhưng người dùng vẫn đang ở trên web chưa thao tác
            gì để refresh token cả. Sau đó user logout, frontend vẫn gửi cái token hết hạn đó lên
            nhưng chết ngay ở dòng xác thực hết hạn (vì throw ra excep) và hàm logout này sẽ không đưa token đó vào table invalidated
            token được .Nếu hacker có được token hết hạn này chỉ cần gọi refresh token, và vì token này ko có trong bảng
            nên vẫn ok và vẫn trong thời gian max có thể refresh nên nó vẫn cấp cho hacker 1 token mới, quá nguy hiểm!

            => phải coi cơ chế logout như refresh, tức là vẫn chấp nhận token hết hạn, đúng chữ ký và trong tg max refresh
        */

        try {
            SignedJWT signedToken = verifyToken(request.getToken(), true);

            String jti = signedToken.getJWTClaimsSet().getJWTID(); // lấy claim id ở trong token 
            Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime(); // Lấy thời gian hết hạn

            // tạo 1 bản ghi token đã logout
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(jti)
                    .expirationTime(expirationTime.toInstant())
                    .build();

            // lưu vào table trong db
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("This token has been expired!");
        }
        
    }

    // Đây là hàm refresh token cũ và nhận về 1 token mới
    public AuthenticationResponse refreshToken(RefreshRequest request) throws JOSEException, ParseException {

        // Vẫn phải xác thực token cũ xem ổn không
        SignedJWT signedToken = verifyToken(request.getToken(), true);

        // Nếu token cũ ok thì ta sẽ đưa token này vào bảng InvalidatedToken trong db 
        String jti = signedToken.getJWTClaimsSet().getJWTID(); // lấy id token
        Date expirationTime = signedToken.getJWTClaimsSet().getExpirationTime(); // lấy thời gian hết hạn

        InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                .id(jti)
                .expirationTime(expirationTime.toInstant())
                .build();

        // đưa token cũ vào InvalidatedToken trong db để không cho lấy token này refresh lần 2
        invalidatedTokenRepository.save(invalidatedToken);

        String username = signedToken.getJWTClaimsSet().getSubject(); // lấy username từ token

        // Lấy user trong db ra dùng username
        User user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .token(token)
                .authenticated(true)
                .build();
    }

    // Hàm verify token, nếu invalid thì throw AppException, valid thì trả về signedJWT phục vụ cho hàm logout
    // Tham số isRefresh là để báo hàm này là hàm verify token cho các hành động bình thường hay là hàm refresh token
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes()); // tạo verifier

        SignedJWT signedJWT = SignedJWT.parse(token);

        // 2 trường hợp: nếu chỉ là hàm verify token bình thường thì isRefresh là true và vẫn hoạt động như cũ 
        // Nếu là trường hợp refresh token thì chắc chắn token đó hết hạn rồi nhưng ta cộng thêm refreshableDurationInSeconds
        // tức là thời gian tính từ lúc token được sinh ra đến thời gian max được refresh token thì token cũ đó vẫn đc refresh
        Date expirationTime = (isRefresh)
                ? new Date(signedJWT
                        .getJWTClaimsSet()
                        .getIssueTime() // lấy thời điểm bắt đầu đăng nhập (lần đầu refresh token được sinh ra)
                        .toInstant()
                        .plus(refreshableDurationInSeconds, ChronoUnit.SECONDS) // 
                        .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        boolean verified = signedJWT.verify(verifier); // xác thực 2 chữ ký có khớp nhau?

        boolean isAlive = expirationTime.after(new Date()); // token còn sống hay hết hạn

        // Đùng dược cả cho introspect và logout vì đều phải xác thực token có chữ ký hợp lệ và
        // chưa hết hạn vì nếu hết hạn thì làm sao vẫn đang ở login mà logout
        if (!(verified && isAlive)) {
            log.error("Error verify token or expired token!");
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
