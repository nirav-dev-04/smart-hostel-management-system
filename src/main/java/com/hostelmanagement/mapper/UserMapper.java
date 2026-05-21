package com.hostelmanagement.mapper;

import com.hostelmanagement.dto.response.UserResponse;
import com.hostelmanagement.entity.User;

public class UserMapper {

    public static UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .hostelBlock(user.getHostelBlock())
                .roomNumber(user.getRoomNumber())
                .phone(user.getPhone())
                .profileImage(user.getProfileImage())
                .isActive(user.getIsActive())
                .build();
    }
}
