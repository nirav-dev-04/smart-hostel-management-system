package com.hostelmanagement.controller.student;

import com.hostelmanagement.dto.request.ComplaintRequest;
import com.hostelmanagement.dto.request.UpdateProfileRequest;
import com.hostelmanagement.dto.response.ApiResponse;
import com.hostelmanagement.dto.response.ComplaintResponse;
import com.hostelmanagement.dto.response.UserResponse;
import com.hostelmanagement.service.ComplaintService;
import com.hostelmanagement.service.FileUploadService;
import com.hostelmanagement.service.UserService;
import com.hostelmanagement.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hostelmanagement.dto.request.UpdateComplaintStatusRequest;
import com.hostelmanagement.exception.UnauthorizedException;

import java.util.List;

@RestController
@Tag(name = "Student Operations", description = "Endpoints restricted to Student actions, complaint filings, and profile management")
public class StudentController {

    private final UserService userService;
    private final ComplaintService complaintService;
    private final FileUploadService fileUploadService;

    public StudentController(UserService userService,
                             ComplaintService complaintService,
                             FileUploadService fileUploadService) {
        this.userService = userService;
        this.complaintService = complaintService;
        this.fileUploadService = fileUploadService;
    }

    @PutMapping("/api/student/profile")
    @Operation(summary = "Update Student Profile", description = "Updates room number, phone, block, and profile image of the currently logged-in student.")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        UserResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping("/api/complaints")
    @Operation(summary = "Submit a new Complaint", description = "Registers a new hostel complaint. Auto-assigns the rector of the same block if registered.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> createComplaint(@Valid @RequestBody ComplaintRequest request) {
        Long studentId = SecurityUtils.getCurrentUserId();
        ComplaintResponse response = complaintService.createComplaint(studentId, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint filed successfully", response));
    }

    @GetMapping("/api/complaints/my")
    @Operation(summary = "Fetch own filed Complaints", description = "Retrieves all complaints filed by the currently logged-in student.")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getMyComplaints() {
        Long studentId = SecurityUtils.getCurrentUserId();
        List<ComplaintResponse> response = complaintService.getMyComplaints(studentId);
        return ResponseEntity.ok(ApiResponse.success("My complaints fetched successfully", response));
    }

    @GetMapping("/api/complaints/{id}")
    @Operation(summary = "Get Complaint details", description = "Retrieves complete detail of a complaint by its ID.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> getComplaintById(@PathVariable Long id) {
        ComplaintResponse response = complaintService.getComplaintById(id);
        return ResponseEntity.ok(ApiResponse.success("Complaint details fetched successfully", response));
    }

    @PutMapping("/api/complaints/{id}")
    @Operation(summary = "Edit a Complaint (Pending only)", description = "Allows editing a complaint's title, description, category, and priority if its status is still PENDING.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaint(@PathVariable Long id, @Valid @RequestBody ComplaintRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        ComplaintResponse response = complaintService.updateComplaint(id, userId, request);
        return ResponseEntity.ok(ApiResponse.success("Complaint updated successfully", response));
    }

    @PutMapping("/api/complaints/{id}/status")
    @Operation(summary = "Update Complaint Status (Student)", description = "Allows students to update (e.g. close) their own complaints.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        ComplaintResponse complaint = complaintService.getComplaintById(id);
        if (!complaint.getStudentId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to update the status of this complaint!");
        }
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @PostMapping(value = "/api/complaints/{id}/upload", consumes = "multipart/form-data")
    @Operation(summary = "Upload image proof for a Complaint", description = "Uploads a photo proof (JPEG, PNG, GIF, WEBP) and assigns its URL to the specified complaint.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> uploadComplaintImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        
        String imageUrl = fileUploadService.storeFile(file);
        ComplaintResponse response = complaintService.uploadComplaintImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Image uploaded and linked successfully", response));
    }
}
