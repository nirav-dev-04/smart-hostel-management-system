package com.hostelmanagement.service;

import com.hostelmanagement.dto.request.RegisterRequest;
import com.hostelmanagement.dto.request.UpdateProfileRequest;
import com.hostelmanagement.dto.request.UpdateUserRequest;
import com.hostelmanagement.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse updateProfile(Long userId, UpdateProfileRequest request);
    List<UserResponse> getAllUsers();
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    void deleteUser(Long userId);
    UserResponse createRector(RegisterRequest request);
    List<UserResponse> getRectors();
}
