package com.hostelmanagement.controller.admin;

import com.hostelmanagement.dto.request.AssignRectorRequest;
import com.hostelmanagement.dto.request.RegisterRequest;
import com.hostelmanagement.dto.request.UpdateUserRequest;
import com.hostelmanagement.dto.response.ApiResponse;
import com.hostelmanagement.dto.response.AuditLogResponse;
import com.hostelmanagement.dto.response.ComplaintResponse;
import com.hostelmanagement.dto.response.DashboardAnalyticsResponse;
import com.hostelmanagement.dto.response.UserResponse;
import com.hostelmanagement.service.AnalyticsService;
import com.hostelmanagement.service.AuditLogService;
import com.hostelmanagement.service.ComplaintService;
import com.hostelmanagement.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Admin Operations", description = "Endpoints restricted to Admin users for system auditing and rector assignments")
public class AdminController {

    private final UserService userService;
    private final ComplaintService complaintService;
    private final AnalyticsService analyticsService;
    private final AuditLogService auditLogService;

    public AdminController(UserService userService,
                           ComplaintService complaintService,
                           AnalyticsService analyticsService,
                           AuditLogService auditLogService) {
        this.userService = userService;
        this.complaintService = complaintService;
        this.analyticsService = analyticsService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/api/admin/users")
    @Operation(summary = "View all users", description = "Retrieves a listing of all registered users (Students, Rectors, Admins).")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> response = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", response));
    }

    @PutMapping("/api/admin/users/{id}")
    @Operation(summary = "Edit User detail", description = "Allows editing role, block, room, name, and active status of any user.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        
        UserResponse response = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success("User updated successfully", response));
    }

    @DeleteMapping("/api/admin/users/{id}")
    @Operation(summary = "Delete a user account", description = "Permanently deletes a user's record from the system.")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }

    @PostMapping("/api/admin/create-rector")
    @Operation(summary = "Create Rector account", description = "Registers a new Rector with pre-defined block assignment.")
    public ResponseEntity<ApiResponse<UserResponse>> createRector(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.createRector(request);
        return ResponseEntity.ok(ApiResponse.success("Rector account created successfully", response));
    }

    @PostMapping("/api/admin/assign-rector")
    @Operation(summary = "Manually assign a Rector to a Complaint", description = "Assigns a rector to a complaint and automatically transitions it to IN_PROGRESS.")
    public ResponseEntity<ApiResponse<ComplaintResponse>> assignRector(@Valid @RequestBody AssignRectorRequest request) {
        ComplaintResponse response = complaintService.assignRector(request.getComplaintId(), request.getRectorId());
        return ResponseEntity.ok(ApiResponse.success("Rector assigned successfully", response));
    }

    @GetMapping("/api/admin/dashboard")
    @Operation(summary = "Retrieve Dashboard Analytics", description = "Compiles total complaints, open status counts, block aggregates, category stats, and average fix-times.")
    public ResponseEntity<ApiResponse<DashboardAnalyticsResponse>> getDashboardAnalytics() {
        DashboardAnalyticsResponse response = analyticsService.getDashboardAnalytics();
        return ResponseEntity.ok(ApiResponse.success("Analytics retrieved successfully", response));
    }

    @GetMapping("/api/admin/complaints")
    @Operation(summary = "View all complaints", description = "Retrieves a listing of all registered complaints across all hostel blocks.")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints() {
        List<ComplaintResponse> response = complaintService.getAllComplaints();
        return ResponseEntity.ok(ApiResponse.success("All complaints fetched successfully", response));
    }

    @GetMapping("/api/admin/audit-logs")
    @Operation(summary = "Retrieve System Audit Logs", description = "Returns a chronological timeline of all state transitions and system modifications.")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs() {
        List<AuditLogResponse> response = auditLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponse.success("Audit logs retrieved successfully", response));
    }
}
