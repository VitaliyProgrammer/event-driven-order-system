package com.orderline.order.service;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.orderline.common.security.JwtConfig;
import com.orderline.order.dto.TokenResponse;
import com.orderline.order.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;

@Service
public class TokenService {

    private static final String ISSUER = "orderline";
    private static final String TOKEN_TYPE = "Bearer";

    private final JwtEncoder jwtEncoder;
    private final Duration tokenTtl;

    public TokenService(SecretKey jwtSecretKey, @Value("${orderline.jwt.ttl}") Duration tokenTtl) {
        this.jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
        this.tokenTtl = tokenTtl;
    }

    public TokenResponse issue(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(tokenTtl))
                .claim(JwtConfig.ROLE_CLAIM, user.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new TokenResponse(token, TOKEN_TYPE, tokenTtl.toSeconds());
    }
}
