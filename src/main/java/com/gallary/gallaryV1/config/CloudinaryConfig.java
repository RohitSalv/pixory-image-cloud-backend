package com.gallary.gallaryV1.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig {
	
	@Bean
	public Cloudinary cloudinary() {
		Map<String, String> config = new HashMap<>();
		config.put("cloud_name", "daehhtffp");
		config.put("api_key", "413187787635319");
		config.put("api_secret", "ef6FhqBD2ygJ4EFGX4qdpnOt7RA");
		
		return new Cloudinary(config);
	}

}
