package com.hostelmanagement.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {
    private Long id;
    private String title;
    private String description;
    private String category;
    private String priority;
    private String status;
    private Long studentId;
    private String studentName;
    private Long rectorId;
    private String rectorName;
    private String hostelBlock;
    private String resolutionNote;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
