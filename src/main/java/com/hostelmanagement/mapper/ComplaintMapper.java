package com.hostelmanagement.mapper;

import com.hostelmanagement.dto.response.ComplaintResponse;
import com.hostelmanagement.entity.Complaint;

public class ComplaintMapper {

    public static ComplaintResponse toResponse(Complaint complaint) {
        if (complaint == null) {
            return null;
        }
        return ComplaintResponse.builder()
                .id(complaint.getId())
                .title(complaint.getTitle())
                .description(complaint.getDescription())
                .category(complaint.getCategory().name())
                .priority(complaint.getPriority().name())
                .status(complaint.getStatus().name())
                .studentId(complaint.getStudent() != null ? complaint.getStudent().getId() : null)
                .studentName(complaint.getStudent() != null ? complaint.getStudent().getName() : "Deleted Student")
                .rectorId(complaint.getRector() != null ? complaint.getRector().getId() : null)
                .rectorName(complaint.getRector() != null ? complaint.getRector().getName() : "Unassigned")
                .hostelBlock(complaint.getHostelBlock())
                .resolutionNote(complaint.getResolutionNote())
                .imageUrl(complaint.getImageUrl())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .build();
    }
}
