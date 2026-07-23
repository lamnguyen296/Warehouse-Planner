package com.restapi.wmsservice.configuration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    Cloudinary cloudinary(CloudinaryProperties properties) {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", properties.getCloudName(),
                "api_key", properties.getApiKey(),
                "api_secret", properties.getApiSecret(),
                "secure", true));
    }
}
