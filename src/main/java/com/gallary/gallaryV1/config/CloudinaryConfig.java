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
		config.put("cloud_name", "cloudname");
		config.put("api_key", "11111");
		config.put("api_secret", "xxxxx");
		
		return new Cloudinary(config);
	}

}
