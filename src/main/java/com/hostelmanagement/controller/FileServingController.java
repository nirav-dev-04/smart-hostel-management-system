package com.hostelmanagement.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class FileServingController {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() || resource.isReadable()) {
                String contentType = "application/octet-stream";
                String lowercaseFilename = filename.toLowerCase();
                
                if (lowercaseFilename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (lowercaseFilename.endsWith(".jpg") || lowercaseFilename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (lowercaseFilename.endsWith(".gif")) {
                    contentType = "image/gif";
                } else if (lowercaseFilename.endsWith(".webp")) {
                    contentType = "image/webp";
                } else if (lowercaseFilename.endsWith(".mp4")) {
                    contentType = "video/mp4";
                } else if (lowercaseFilename.endsWith(".webm")) {
                    contentType = "video/webm";
                } else if (lowercaseFilename.endsWith(".ogg")) {
                    contentType = "video/ogg";
                } else if (lowercaseFilename.endsWith(".mov")) {
                    contentType = "video/quicktime";
                } else if (lowercaseFilename.endsWith(".m4v")) {
                    contentType = "video/x-m4v";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
