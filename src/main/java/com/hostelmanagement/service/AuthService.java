package com.hostelmanagement.service;

import com.hostelmanagement.dto.request.LoginRequest;
import com.hostelmanagement.dto.request.RegisterRequest;
import com.hostelmanagement.dto.response.AuthResponse;
import com.hostelmanagement.dto.response.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse getMe(String email);
}
