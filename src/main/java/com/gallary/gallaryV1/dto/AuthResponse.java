package com.gallary.gallaryV1.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
	private String accessToken;
	private String refreshToken;

}
