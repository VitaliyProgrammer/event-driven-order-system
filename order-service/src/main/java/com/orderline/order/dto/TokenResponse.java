package com.orderline.order.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds
) {
}
