package com.vidara.tradecenter.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileStorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageConfig.class);

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private Path uploadPath;
    private Path productImagePath;
    private Path profileImagePath;
    private Path reviewImagePath;


    // CREATE DIRECTORIES ON STARTUP
    // Runs automatically when app starts

    @PostConstruct
    public void init() {
        try {
            uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            productImagePath = uploadPath.resolve("products");
            profileImagePath = uploadPath.resolve("profiles");
            reviewImagePath = uploadPath.resolve("reviews");

            // Create directories if they don't exist
            Files.createDirectories(uploadPath);
            Files.createDirectories(productImagePath);
            Files.createDirectories(profileImagePath);
            Files.createDirectories(reviewImagePath);

            logger.info("Created upload directories at: {}", uploadPath);
            logger.info("  - Products: {}", productImagePath);
            logger.info("  - Profiles: {}", profileImagePath);
            logger.info("  - Reviews: {}", reviewImagePath);

        } catch (IOException e) {
            logger.error("Could not create upload directories", e);
            throw new RuntimeException("Could not create upload directories", e);
        }
    }


    // GETTERS - Used by FileStorageService
    public Path getUploadPath() {
        return uploadPath;
    }

    public Path getProductImagePath() {
        return productImagePath;
    }

    public Path getProfileImagePath() {
        return profileImagePath;
    }

    public Path getReviewImagePath() {
        return reviewImagePath;
    }

    public String getUploadDir() {
        return uploadDir;
    }
}