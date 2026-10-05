package com.orderline.notification.mapper;

import com.orderline.notification.dto.NotificationResponse;
import com.orderline.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
