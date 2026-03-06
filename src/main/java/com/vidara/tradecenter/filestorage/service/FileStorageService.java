package com.vidara.tradecenter.filestorage.service;

import com.vidara.tradecenter.filestorage.dto.response.FileResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface FileStorageService {

    // Upload a single file to the specified subdirectory
    FileResponse uploadFile(MultipartFile file, String subDir);

    // Upload multiple files to the specified subdirectory
    List<FileResponse> uploadMultipleFiles(MultipartFile[] files, String subDir);

    // Delete a file by its path (relative to uploads directory)
    void deleteFile(String filePath);
}
