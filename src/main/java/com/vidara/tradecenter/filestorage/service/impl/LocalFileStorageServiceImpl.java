package com.vidara.tradecenter.filestorage.service.impl;

import com.vidara.tradecenter.common.exception.BadRequestException;
import com.vidara.tradecenter.config.FileStorageConfig;
import com.vidara.tradecenter.filestorage.dto.response.FileResponse;
import com.vidara.tradecenter.filestorage.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageServiceImpl.class);

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp"
    );

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private final FileStorageConfig fileStorageConfig;

    public LocalFileStorageServiceImpl(FileStorageConfig fileStorageConfig) {
        this.fileStorageConfig = fileStorageConfig;
    }


    // UPLOAD SINGLE FILE

    @Override
    public FileResponse uploadFile(MultipartFile file, String subDir) {
        // Validate file is not empty
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BadRequestException("File type not allowed. Allowed types: JPEG, PNG, GIF, WebP");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        try {
            // Resolve target directory
            Path targetDir = fileStorageConfig.getUploadPath().resolve(subDir).normalize();

            // Ensure target directory is within upload path (prevent path traversal)
            if (!targetDir.startsWith(fileStorageConfig.getUploadPath())) {
                throw new BadRequestException("Invalid upload directory");
            }

            Files.createDirectories(targetDir);

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFilename = UUID.randomUUID().toString() + extension;

            // Save file to disk
            Path targetPath = targetDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Build URL path (relative to server root)
            String fileUrl = "/uploads/" + subDir + "/" + uniqueFilename;

            logger.info("File uploaded: {} → {}", originalFilename, fileUrl);

            return new FileResponse(uniqueFilename, fileUrl, contentType, file.getSize());

        } catch (IOException e) {
            logger.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }


    // UPLOAD MULTIPLE FILES

    @Override
    public List<FileResponse> uploadMultipleFiles(MultipartFile[] files, String subDir) {
        List<FileResponse> responses = new ArrayList<>();
        for (MultipartFile file : files) {
            responses.add(uploadFile(file, subDir));
        }
        return responses;
    }


    // DELETE FILE

    @Override
    public void deleteFile(String filePath) {
        try {
            // filePath is relative like "products/uuid.jpg"
            Path targetPath = fileStorageConfig.getUploadPath().resolve(filePath).normalize();

            // Ensure target path is within upload path (prevent path traversal)
            if (!targetPath.startsWith(fileStorageConfig.getUploadPath())) {
                throw new BadRequestException("Invalid file path");
            }

            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                logger.info("File deleted: {}", filePath);
            } else {
                logger.warn("File not found for deletion: {}", filePath);
            }

        } catch (IOException e) {
            logger.error("Failed to delete file: {}", filePath, e);
            throw new RuntimeException("Failed to delete file", e);
        }
    }
}
