package com.example.demo.configuration;

import java.text.ParseException;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.example.demo.dto.request.IntrospectRequest;
import com.example.demo.dto.response.IntrospectResponse;
import com.example.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component // để spring biết để tạo bean cho class này
@RequiredArgsConstructor
@Slf4j
public class CustomJwtDecoder implements JwtDecoder {

    private final AuthenticationService authenticationService;

    private NimbusJwtDecoder nimbusJwtDecoder = null;

    @Value("${jwt.signer-key}")
    protected String SIGNER_KEY;

    @Override
    public Jwt decode(String token) throws JwtException {

        if (Objects.isNull(nimbusJwtDecoder)) {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SIGNER_KEY.getBytes(), "HS512");
            nimbusJwtDecoder = NimbusJwtDecoder.
                    withSecretKey(secretKeySpec)
                    .macAlgorithm(MacAlgorithm.HS512)
                    .build();
        }

        // Cứ decode token trước
        Jwt jwt = nimbusJwtDecoder.decode(token);

        try {
            IntrospectResponse introspectResponse = authenticationService
                    .introspect(IntrospectRequest.builder().token(token).build());

            // Nếu introspectResponse mà introspect trả về valid != true thì throw ra JwtException để JwtAuthenticationEntryPoint bắt
            if (!introspectResponse.isValid()) {
                throw new JwtException("This token has been logout!"); // Những exceptions này sẽ được JwtAuthenticationEntryPoint tự động bắt và trả về response authenticated
            }
        } catch(JOSEException | ParseException e) {
            throw new JwtException(e.getMessage()); // Những exceptions này sẽ được JwtAuthenticationEntryPoint tự động bắt và trả về response authenticated
        }

        return jwt;
    }
    
}
