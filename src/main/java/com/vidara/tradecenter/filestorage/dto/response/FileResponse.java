package com.vidara.tradecenter.filestorage.dto.response;

public class FileResponse {

    private String fileName;
    private String fileUrl;
    private String fileType;
    private long fileSize;


    // CONSTRUCTORS

    public FileResponse() {
    }

    public FileResponse(String fileName, String fileUrl, String fileType, long fileSize) {
        this.fileName = fileName;
        this.fileUrl = fileUrl;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }


    // GETTERS AND SETTERS

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
