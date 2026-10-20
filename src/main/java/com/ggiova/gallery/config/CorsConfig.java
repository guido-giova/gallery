package com.ggiova.gallery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for the cors where all origins listed in the environment variable {@code app.cors.allowed} are
 * given permission to do {@code GET}, {@code POST}, {@code PUT}, {@code DELETE}, {@code PATCH}, and {@code OPTIONS}.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    private static final String[] METHODS = new String[]{"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};

    @Value("${app.cors.allowed}")
    private String[] allowedOrigins;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(this.allowedOrigins)
                .allowedMethods(METHODS)
                .allowedHeaders("*");
    }
}
