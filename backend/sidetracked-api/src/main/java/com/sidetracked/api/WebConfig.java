package com.sidetracked.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * Which other websites may call the API from a browser (CORS).
 *
 * In development the Angular dev server proxies /api, so the
 * browser never needs this. It's here, in one place, for setups
 * where the frontend is served from a different address: set
 * sidetracked.cors.allowed-origins (comma-separated) to allow them.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;

    public WebConfig(
            @Value("${sidetracked.cors.allowed-origins:http://localhost:4200}")
            String[] allowedOrigins) {

        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }
}
