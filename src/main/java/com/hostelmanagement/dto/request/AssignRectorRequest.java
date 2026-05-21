package com.hostelmanagement.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignRectorRequest {

    @NotNull(message = "Complaint ID is required")
    private Long complaintId;

    @NotNull(message = "Rector ID is required")
    private Long rectorId;
}
