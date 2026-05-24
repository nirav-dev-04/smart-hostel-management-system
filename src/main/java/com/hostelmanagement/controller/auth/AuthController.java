package com.hostelmanagement.controller.auth;

import com.hostelmanagement.dto.request.LoginRequest;
import com.hostelmanagement.dto.request.RegisterRequest;
import com.hostelmanagement.dto.response.ApiResponse;
import com.hostelmanagement.dto.response.AuthResponse;
import com.hostelmanagement.dto.response.UserResponse;
import com.hostelmanagement.service.AuthService;
import com.hostelmanagement.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Authentication Operations", description = "Endpoints for user register, login, and profile fetching")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    @Operation(summary = "Register a new Student", description = "Standard student registration. Automatically grants student role.")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/api/auth/login")
    @Operation(summary = "Authenticate a User", description = "Accepts credentials and returns a Bearer JWT Token along with user role.")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/api/auth/google")
    @Operation(summary = "Authenticate a User via Google SSO", description = "Accepts Google email and signs in the user, returning a Bearer JWT Token.")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@RequestBody java.util.Map<String, String> request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }

    @GetMapping("/api/users/me")
    @Operation(summary = "Get current authenticated User profile", description = "Retrieves details of the currently logged-in student/rector/admin.")
    public ResponseEntity<ApiResponse<UserResponse>> getMe() {
        String email = SecurityUtils.getCurrentUserEmail();
        UserResponse response = authService.getMe(email);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }
}
