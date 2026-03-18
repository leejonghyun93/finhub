package com.finhub.user.dto.response;

import com.finhub.user.domain.User;

import java.time.LocalDateTime;

public record UserInfoResponse(
        Long id,
        String email,
        String name,
        String phoneNumber,
        String role,
        LocalDateTime createdAt
) {
    public static UserInfoResponse from(User user) {
        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
