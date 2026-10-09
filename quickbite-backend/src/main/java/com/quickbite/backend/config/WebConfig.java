package com.quickbite.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebConfig.class);

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Exposes the upload folder at /uploads/** so saved image paths become real URLs
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            log.warn("Could not create upload directory {}: {}", root, e.getMessage());
        }
        String location = "file:" + root.toString().replace("\\", "/") + "/";

        // File names are random UUIDs, so long caching is safe
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
    }
}