package com.gallary.gallaryV1.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@Configuration
public class CloudinaryConfig{
	
	private CloudinaryProperties props;
	
	  public CloudinaryConfig(CloudinaryProperties props) {
	        this.props = props;
	    }
	
	@Bean
	public Cloudinary cloudinary() {
		
		Map<String, String> config = new HashMap<>();
		
		config.put("cloud_name", props.getCloudName());
        config.put("api_key", props.getApiKey());
        config.put("api_secret", props.getApiSecret());
        
		return new Cloudinary(config);
	}

}
