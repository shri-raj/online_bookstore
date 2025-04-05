package com.example.online_bookstore.config;

import lombok.Getter;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class JwtConfig {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    public String getEncodedSecret() {
        return Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
}