package io.github.deeqma.music.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.github.deeqma.music.error.ErrorType;
import io.github.deeqma.music.error.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private final byte[] secret;
    private final long expirationMs;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long expirationMs
    ) {
        this.secret = secret.getBytes();
        this.expirationMs = expirationMs;
    }

    public String generateToken(UUID userId, String username) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("userId", userId.toString())
                    .claim("scope", List.of("read", "write", "delete"))
                    .issuer("bologverket-pokeapi-app")
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expirationMs))
                    .build();

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            signedJWT.sign(new MACSigner(secret));
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new JwtException(ErrorType.TOKEN_GENERATION_FAILED, "Failed to generate token", e);
        }
    }

    public static UUID extractUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }

}