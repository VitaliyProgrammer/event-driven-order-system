package com.orderline.order.service;

import com.orderline.order.dto.LoginRequest;
import com.orderline.order.dto.RegistrationRequest;
import com.orderline.order.dto.TokenResponse;
import com.orderline.order.dto.UserResponse;
import com.orderline.order.entity.Role;
import com.orderline.order.entity.User;
import com.orderline.order.exception.EmailAlreadyUsedException;
import com.orderline.order.mapper.UserMapper;
import com.orderline.order.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private static final String GUEST_EMAIL_TEMPLATE = "guest-%s@orderline.local";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegistrationRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyUsedException(email);
        }
        User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), Role.USER));
        return userMapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(normalize(request.email()))
                .filter(found -> passwordEncoder.matches(request.password(), found.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        return tokenService.issue(user);
    }

    @Transactional
    public TokenResponse loginAsGuest() {
        String email = GUEST_EMAIL_TEMPLATE.formatted(UUID.randomUUID());
        String unusablePassword = passwordEncoder.encode(UUID.randomUUID().toString());
        User guest = userRepository.save(new User(email, unusablePassword, Role.USER));
        return tokenService.issue(guest);
    }

    private static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
