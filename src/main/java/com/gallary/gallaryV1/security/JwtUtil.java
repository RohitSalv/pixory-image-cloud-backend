package com.gallary.gallaryV1.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	private static final String SECRET = "OX4OxLBo8c7DGjnsUpuHyRVCLTfEWTXxmAxc7TTNV3B";
	private static final long EXPIRATION = 1000 * 60 * 60 * 24;
	
	private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
	
	public String generateToken(String email) {
		return Jwts.builder()
				.subject(email)
				.issuedAt(new Date())
				.expiration(new Date(System.currentTimeMillis() + EXPIRATION))
				.signWith(key)
				.compact();
	}
	
	public String extractEmail(String token) {
		return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
	}
}
