package com.gallary.gallaryV1.service;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.gallary.gallaryV1.model.User;
import com.gallary.gallaryV1.repository.UserRepository;

import com.gallary.gallaryV1.dto.AuthResponse;
import com.gallary.gallaryV1.security.JwtUtil;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    public UserService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(String email, String password) {

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");

        // Generate tokens
        String accessToken = jwtUtil.generateToken(email);
        String refreshToken = jwtUtil.generateRefreshToken(email);

        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(java.time.LocalDateTime.now().plusDays(7));

        User savedUser = userRepository.save(user);

        // Send welcome email
        String subject = "Welcome to Pixory Image Cloud!";
        String body = "Hello,\n\nWelcome to Pixory! We are excited to have you on board.\n\nEnjoy uploading and analyzing your images with AI superpowers.\n\nBest,\nThe Pixory Team";
        emailService.sendEmail(savedUser.getEmail(), subject, body);

        return new AuthResponse(accessToken, refreshToken);
    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtUtil.extractEmail(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRefreshToken() == null || !user.getRefreshToken().equals(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        if (user.getRefreshTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = jwtUtil.generateToken(email);
        return new AuthResponse(newAccessToken, refreshToken);
    }

    public void initiateForgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = java.util.UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(java.time.LocalDateTime.now().plusMinutes(15));
            userRepository.save(user);

            // Send email
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            String subject = "Password Reset Request";
            String body = "To reset your password, please click the link below:\n\n" + resetLink
                    + "\n\nThis link will expire in 15 minutes.";

            emailService.sendEmail(user.getEmail(), subject, body);
        });
    }

    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getResetTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }

}
