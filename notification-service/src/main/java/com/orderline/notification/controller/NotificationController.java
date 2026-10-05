package com.orderline.notification.controller;

import com.orderline.common.security.CurrentUser;
import com.orderline.notification.dto.NotificationResponse;
import com.orderline.notification.service.NotificationService;
import com.orderline.notification.service.NotificationStream;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationStream notificationStream;

    @GetMapping
    public PagedModel<NotificationResponse> findMine(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(notificationService.findMine(CurrentUser.from(jwt).id(), pageable));
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return notificationService.markRead(CurrentUser.from(jwt), id);
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal Jwt jwt) {
        return notificationStream.subscribe(CurrentUser.from(jwt).id());
    }
}
