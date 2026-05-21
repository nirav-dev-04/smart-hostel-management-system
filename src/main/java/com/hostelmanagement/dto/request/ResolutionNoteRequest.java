package com.hostelmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResolutionNoteRequest {

    @NotBlank(message = "Resolution note is required")
    private String resolutionNote;
}
