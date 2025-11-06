package org.fsm.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.avatar-dir}")
    private String avatarDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // MUST end with "/" and use "file:"
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations("file:" + avatarDir + "/");
    }

    @PostConstruct
    public void init() throws IOException {
        Path dir = Paths.get(avatarDir);
        System.out.println("Creating avatar directory: " + dir.toAbsolutePath());
        Files.createDirectories(dir);
    }
}
