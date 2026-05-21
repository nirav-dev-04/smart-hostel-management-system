package com.hostelmanagement.service.impl;

import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.service.FileUploadService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Path fileStorageLocation;

    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Upload directory initialized at: {}", this.fileStorageLocation);
        } catch (Exception ex) {
            log.error("Could not create the directory where the uploaded files will be stored.", ex);
            throw new RuntimeException("Could not create the upload directory!", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("Cannot upload empty file!");
        }

        // Validate image content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ValidationException("Only image uploads are allowed!");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";
        
        int i = originalFilename.lastIndexOf('.');
        if (i > 0) {
            fileExtension = originalFilename.substring(i);
        }

        // Validate standard image extensions
        List<String> allowedExtensions = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".webp");
        if (!allowedExtensions.contains(fileExtension.toLowerCase())) {
            throw new ValidationException("Unsupported image format! Allowed formats: JPG, JPEG, PNG, GIF, WEBP.");
        }

        try {
            // Check for invalid path characters
            if (originalFilename.contains("..")) {
                throw new ValidationException("Filename contains invalid path sequence: " + originalFilename);
            }

            // Generate unique name
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);

            // Copy file to target path
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("File successfully saved: {}", targetLocation);
            
            // Return relative serving path
            return "/uploads/" + newFilename;
        } catch (IOException ex) {
            log.error("Failed to store file {}", originalFilename, ex);
            throw new RuntimeException("Could not store file " + originalFilename + ". Please try again!", ex);
        }
    }
}
