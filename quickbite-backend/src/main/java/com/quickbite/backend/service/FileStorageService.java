package com.quickbite.backend.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    // Saves an image and returns its public path, e.g. /uploads/foods/<uuid>.jpg
    String store(MultipartFile file, String subFolder);

    // Best-effort removal of a previously stored file (ignores null / foreign paths)
    void delete(String publicPath);
}