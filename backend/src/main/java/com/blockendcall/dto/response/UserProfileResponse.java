package com.blockendcall.dto.response;

import com.blockendcall.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private long totalReports;
    private LocalDateTime createdAt;

    public static UserProfileResponse from(User user, long totalReports) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .totalReports(totalReports)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
