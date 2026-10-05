package com.orderline.notification.exception;

import com.orderline.common.web.NotFoundException;

import java.util.UUID;

public class NotificationNotFoundException extends NotFoundException {

    public NotificationNotFoundException(UUID notificationId) {
        super("Notification %s not found".formatted(notificationId));
    }
}
