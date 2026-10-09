package com.quickbite.backend.service.impl;

import com.quickbite.backend.exception.BadRequestException;
import com.quickbite.backend.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);
    private static final String PUBLIC_PREFIX = "/uploads/";

    // Only these types are accepted. The extension comes from this map, never from the client's file name.
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif");

    private final Path root;

    public LocalFileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file, String subFolder) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please choose an image to upload");
        }
        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BadRequestException("Only JPG, PNG, WEBP or GIF images are allowed");
        }

        try {
            Path dir = root.resolve(subFolder).normalize();
            if (!dir.startsWith(root)) {
                throw new BadRequestException("Invalid upload location");
            }
            Files.createDirectories(dir);

            String filename = UUID.randomUUID() + extension;
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
            }
            log.info("Stored upload {}/{}", subFolder, filename);
            return PUBLIC_PREFIX + subFolder + "/" + filename;
        } catch (IOException e) {
            log.error("Failed to store uploaded file", e);
            throw new BadRequestException("Could not save the uploaded file");
        }
    }

    @Override
    public void delete(String publicPath) {
        if (publicPath == null || !publicPath.startsWith(PUBLIC_PREFIX)) {
            return;
        }
        try {
            Path target = root.resolve(publicPath.substring(PUBLIC_PREFIX.length())).normalize();
            if (target.startsWith(root)) {
                Files.deleteIfExists(target);
            }
        } catch (IOException e) {
            log.warn("Could not delete {}: {}", publicPath, e.getMessage());
        }
    }
}