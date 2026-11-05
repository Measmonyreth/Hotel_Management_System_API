package com.veth007.hotelmanagement.config.cloudinary;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;
    @Value("${cloudinary.api-key}")
    private String apiKey;
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true));
    }

    // @Bean
    // public Cloudinary cloudinary() {
    // System.out.println(">>> cloud_name: [" + cloudName + "]");
    // System.out.println(">>> api_key: [" + apiKey + "]");
    // System.out.println(">>> api_secret: [" + apiSecret + "]");
    // return new Cloudinary(ObjectUtils.asMap(
    // "cloud_name", "Test_Hotel_Project",
    // "api_key", "835898444686674",
    // "api_secret", "dJ-SHfTEF9h4yEIy8M2yhXE-aek",
    // "secure", true));
    // }

}
