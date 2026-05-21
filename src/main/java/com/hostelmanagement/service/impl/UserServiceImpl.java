package com.hostelmanagement.service.impl;

import com.hostelmanagement.dto.request.RegisterRequest;
import com.hostelmanagement.dto.request.UpdateProfileRequest;
import com.hostelmanagement.dto.request.UpdateUserRequest;
import com.hostelmanagement.dto.response.UserResponse;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.enums.Role;
import com.hostelmanagement.exception.DuplicateResourceException;
import com.hostelmanagement.exception.ResourceNotFoundException;
import com.hostelmanagement.exception.ValidationException;
import com.hostelmanagement.mapper.UserMapper;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.service.UserService;
import com.hostelmanagement.service.AuditLogService;
import com.hostelmanagement.util.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String oldValue = String.format("Block: %s, Room: %s", user.getHostelBlock(), user.getRoomNumber());
        
        user.setName(request.getName());
        user.setHostelBlock(request.getHostelBlock());
        user.setRoomNumber(request.getRoomNumber());
        user.setPhone(request.getPhone());
        if (request.getProfileImage() != null) {
            user.setProfileImage(request.getProfileImage());
        }

        User updatedUser = userRepository.save(user);
        String newValue = String.format("Block: %s, Room: %s", updatedUser.getHostelBlock(), updatedUser.getRoomNumber());

        auditLogService.logAction("UPDATE_PROFILE", oldValue, newValue, user.getEmail());

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        String oldValue = String.format("Name: %s, Email: %s, Active: %b, Role: %s",
                user.getName(), user.getEmail(), user.getIsActive(), user.getRole().name());

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setHostelBlock(request.getHostelBlock());
        user.setRoomNumber(request.getRoomNumber());
        user.setPhone(request.getPhone());
        user.setIsActive(request.getIsActive());

        if (request.getRole() != null) {
            try {
                user.setRole(Role.valueOf(request.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid role value: " + request.getRole());
            }
        }

        User updatedUser = userRepository.save(user);
        String newValue = String.format("Name: %s, Email: %s, Active: %b, Role: %s",
                updatedUser.getName(), updatedUser.getEmail(), updatedUser.getIsActive(), updatedUser.getRole().name());

        String adminEmail = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("ADMIN_UPDATE_USER", oldValue, newValue, adminEmail);

        return UserMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        userRepository.delete(user);

        String adminEmail = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("ADMIN_DELETE_USER", user.getEmail(), null, adminEmail);
    }

    @Override
    @Transactional
    public UserResponse createRector(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        User rector = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.RECTOR)
                .hostelBlock(request.getHostelBlock())
                .roomNumber(request.getRoomNumber()) // usually null for Rector or specific office
                .phone(request.getPhone())
                .isActive(true)
                .build();

        User savedRector = userRepository.save(rector);
        
        String adminEmail = SecurityUtils.getCurrentUserEmail();
        auditLogService.logAction("ADMIN_CREATE_RECTOR", null, savedRector.getEmail(), adminEmail);

        return UserMapper.toResponse(savedRector);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getRectors() {
        return userRepository.findByRole(Role.RECTOR).stream()
                .map(UserMapper::toResponse)
                .collect(Collectors.toList());
    }
}
