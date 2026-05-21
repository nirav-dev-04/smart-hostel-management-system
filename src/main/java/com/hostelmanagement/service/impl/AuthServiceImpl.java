package com.hostelmanagement.service.impl;

import com.hostelmanagement.dto.request.LoginRequest;
import com.hostelmanagement.dto.request.RegisterRequest;
import com.hostelmanagement.dto.response.AuthResponse;
import com.hostelmanagement.dto.response.UserResponse;
import com.hostelmanagement.entity.User;
import com.hostelmanagement.enums.Role;
import com.hostelmanagement.exception.DuplicateResourceException;
import com.hostelmanagement.exception.ResourceNotFoundException;
import com.hostelmanagement.mapper.UserMapper;
import com.hostelmanagement.repository.UserRepository;
import com.hostelmanagement.security.jwt.JwtUtils;
import com.hostelmanagement.security.service.UserDetailsImpl;
import com.hostelmanagement.service.AuthService;
import com.hostelmanagement.service.AuditLogService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuditLogService auditLogService;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils,
                           AuditLogService auditLogService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        // Resolve role
        Role role = Role.STUDENT;
        if (request.getRole() != null && !request.getRole().trim().isEmpty()) {
            try {
                role = Role.valueOf(request.getRole().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new com.hostelmanagement.exception.ValidationException("Invalid role specified: " + request.getRole());
            }
        }

        // Perform role-based validation
        java.util.Map<String, String> errors = new java.util.HashMap<>();
        if (role == Role.STUDENT) {
            if (request.getHostelBlock() == null || request.getHostelBlock().trim().isEmpty()) {
                errors.put("hostelBlock", "Hostel block is required for students");
            }
            if (request.getRoomNumber() == null || request.getRoomNumber().trim().isEmpty()) {
                errors.put("roomNumber", "Room number is required for students");
            }
        } else if (role == Role.RECTOR) {
            if (request.getHostelBlock() == null || request.getHostelBlock().trim().isEmpty()) {
                errors.put("hostelBlock", "Hostel block is required for rectors");
            }
        }

        if (!errors.isEmpty()) {
            throw new com.hostelmanagement.exception.ValidationException("Validation failed", errors);
        }

        // Create user
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .hostelBlock(request.getHostelBlock())
                .roomNumber(role == Role.STUDENT ? request.getRoomNumber() : null)
                .phone(request.getPhone())
                .isActive(true)
                .build();

        userRepository.save(user);
        auditLogService.logAction("USER_REGISTER", null, user.getEmail(), user.getEmail());

        // Perform auto-login to return token immediately upon registration
        return authenticateAndBuildResponse(request.getEmail(), request.getPassword());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        AuthResponse response = authenticateAndBuildResponse(request.getEmail(), request.getPassword());
        auditLogService.logAction("USER_LOGIN", null, request.getEmail(), request.getEmail());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return UserMapper.toResponse(user);
    }

    private AuthResponse authenticateAndBuildResponse(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        String roleName = userPrincipal.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        return AuthResponse.builder()
                .token(jwt)
                .role(roleName)
                .userId(userPrincipal.getId())
                .name(userPrincipal.getName())
                .build();
    }
}
