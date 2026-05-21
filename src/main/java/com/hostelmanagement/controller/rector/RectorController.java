package com.hostelmanagement.controller.rector;

import com.hostelmanagement.dto.request.ResolutionNoteRequest;
import com.hostelmanagement.dto.request.UpdateComplaintStatusRequest;
import com.hostelmanagement.dto.response.ApiResponse;
import com.hostelmanagement.dto.response.ComplaintResponse;
import com.hostelmanagement.service.ComplaintService;
import com.hostelmanagement.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Rector Operations", description = "Endpoints restricted to Rector users for resolving block level complaints")
public class RectorController {

    private final ComplaintService complaintService;

    public RectorController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping("/api/rector/complaints")
    @Operation(summary = "View assigned Block Complaints", description = "Retrieves all complaints associated with the currently logged-in rector's block.")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAssignedComplaints() {
        Long rectorId = SecurityUtils.getCurrentUserId();
        List<ComplaintResponse> response = complaintService.getRectorComplaints(rectorId);
        return ResponseEntity.ok(ApiResponse.success("Block complaints retrieved successfully", response));
    }

    @PutMapping("/api/rector/complaints/{id}/status")
    @Operation(summary = "Update Complaint Status", description = "Transitions a complaint to IN_PROGRESS, RESOLVED, CLOSED, ESCALATED, or REJECTED state.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateComplaintStatusRequest request) {
        
        ComplaintResponse response = complaintService.updateComplaintStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Complaint status updated successfully", response));
    }

    @PostMapping("/api/rector/complaints/{id}/note")
    @Operation(summary = "Add Resolution Note", description = "Files a resolution note explaining the fix. Auto-resolves the complaint status.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> addNote(
            @PathVariable Long id,
            @Valid @RequestBody ResolutionNoteRequest request) {
        
        ComplaintResponse response = complaintService.addResolutionNote(id, request.getResolutionNote());
        return ResponseEntity.ok(ApiResponse.success("Resolution note logged successfully", response));
    }
}
