package com.orderline.order.exception;

import com.orderline.common.web.NotFoundException;

import java.util.UUID;

public class UserNotFoundException extends NotFoundException {

    public UserNotFoundException(UUID userId) {
        super("User %s not found".formatted(userId));
    }
}
