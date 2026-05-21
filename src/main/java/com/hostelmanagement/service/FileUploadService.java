package com.hostelmanagement.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    String storeFile(MultipartFile file);
}
