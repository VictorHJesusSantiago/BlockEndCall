package com.blockendcall.service;

import com.blockendcall.dto.request.LoginRequest;
import com.blockendcall.dto.request.RegisterRequest;
import com.blockendcall.dto.response.AuthResponse;
import com.blockendcall.entity.User;
import com.blockendcall.repository.UserRepository;
import com.blockendcall.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);

        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("User not found after authentication"));

        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(user, token);
    }

    public void verifyEmail(String token) {
        // TODO: implement actual email verification via Spring Mail + token DB lookup
        log.info("Email verified for token: {}", token);
    }

    public void sendPasswordResetEmail(String email) {
        // TODO: send actual password reset email via Spring Mail
        log.info("Password reset email would be sent to: {}", email);
    }

    public void resetPassword(String token, String newPassword) {
        // TODO: validate token from DB, update user password
        log.info("Password reset for token: {}", token);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
