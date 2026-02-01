package com.gallary.gallaryV1.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.gallary.gallaryV1.dto.AuthResponse;
import com.gallary.gallaryV1.dto.LoginRequest;
import com.gallary.gallaryV1.dto.RegisterRequest;
import com.gallary.gallaryV1.model.User;
import com.gallary.gallaryV1.repository.UserRepository;
import com.gallary.gallaryV1.security.JwtUtil;
import com.gallary.gallaryV1.dto.ForgotPasswordRequest;
import com.gallary.gallaryV1.dto.ResetPasswordRequest;
import com.gallary.gallaryV1.service.UserService;
import com.gallary.gallaryV1.dto.RefreshTokenRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService,
            JwtUtil jwtUtil, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {

        AuthResponse response = userService.register(
                request.getEmail(),
                request.getPassword());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String accessToken = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // Update user's refresh token
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(java.time.LocalDateTime.now().plusDays(7));
        userRepository.save(user);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(userService.refreshAccessToken(request.getRefreshToken()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.initiateForgotPassword(request.getEmail());
        return ResponseEntity.ok("If the email exists, a reset token has been sent.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully.");
    }
}
