package com.orderline.order.exception;

import com.orderline.common.web.ConflictException;

public class EmailAlreadyUsedException extends ConflictException {

    public EmailAlreadyUsedException(String email) {
        super("Email %s is already registered".formatted(email));
    }
}
