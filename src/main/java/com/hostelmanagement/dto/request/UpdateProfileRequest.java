package com.hostelmanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Hostel block is required")
    @Size(max = 50, message = "Hostel block must not exceed 50 characters")
    private String hostelBlock;

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @NotBlank(message = "Phone number is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    private String phone;

    private String profileImage;
}
