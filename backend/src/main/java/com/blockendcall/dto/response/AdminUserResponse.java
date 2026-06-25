package com.blockendcall.dto.response;

import com.blockendcall.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AdminUserResponse {

    private Long id;
    private String name;
    private String email;
    private String role;
    private int reputationScore;
    private boolean suspended;
    private boolean active;
    private long totalReports;
    private LocalDateTime createdAt;

    public static AdminUserResponse from(User user, long totalReports) {
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .reputationScore(user.getReputationScore())
                .suspended(user.isSuspended())
                .active(user.isActive())
                .totalReports(totalReports)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
