package com.vidara.tradecenter.filestorage.controller;

import com.vidara.tradecenter.common.dto.ApiResponse;
import com.vidara.tradecenter.filestorage.dto.response.FileResponse;
import com.vidara.tradecenter.filestorage.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }


    // UPLOAD SINGLE FILE (authenticated)
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<FileResponse>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "subDir", defaultValue = "products") String subDir) {
        FileResponse fileResponse = fileStorageService.uploadFile(file, subDir);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("File uploaded successfully", fileResponse));
    }


    // UPLOAD MULTIPLE FILES (authenticated)
    @PostMapping("/upload-multiple")
    public ResponseEntity<ApiResponse<List<FileResponse>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "subDir", defaultValue = "products") String subDir) {
        List<FileResponse> fileResponses = fileStorageService.uploadMultipleFiles(files, subDir);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Files uploaded successfully", fileResponses));
    }


    // DELETE FILE (ADMIN only)
    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @RequestParam("filePath") String filePath) {
        fileStorageService.deleteFile(filePath);
        return ResponseEntity
                .ok(ApiResponse.success("File deleted successfully"));
    }
}
