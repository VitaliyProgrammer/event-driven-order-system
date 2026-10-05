package com.orderline.order.controller;

import com.orderline.common.security.CurrentUser;
import com.orderline.order.dto.UserResponse;
import com.orderline.order.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal Jwt jwt) {
        return userService.getById(CurrentUser.from(jwt).id());
    }
}
