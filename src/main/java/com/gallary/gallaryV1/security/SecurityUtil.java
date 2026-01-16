package com.gallary.gallaryV1.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {

    public static CustomUserDetails getCurrentUser() {
        Object principal = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails userDetails) {
            return userDetails;
        }

        throw new RuntimeException("Unauthenticated access");
    }
}
