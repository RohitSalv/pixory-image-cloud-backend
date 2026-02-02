package com.gallary.gallaryV1.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${app.cors.allowed-origins:http://localhost:4200}")
	private String[] allowedOrigins = new String[0];

	@Override
	public void addCorsMappings(@NonNull CorsRegistry registry) {
		if (allowedOrigins.length > 0) {
			registry.addMapping("/api/**")
					.allowedOrigins(allowedOrigins)
					.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
					.allowedHeaders("*")
					.allowCredentials(true);
		}
	}

}
