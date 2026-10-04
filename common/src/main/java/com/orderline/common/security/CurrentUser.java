package com.orderline.common.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public record CurrentUser(UUID id, boolean admin) {

    private static final String ADMIN_ROLE = "ADMIN";

    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                ADMIN_ROLE.equals(jwt.getClaimAsString(JwtConfig.ROLE_CLAIM)));
    }

    public boolean canAccess(UUID ownerId) {
        return admin || id.equals(ownerId);
    }
}
