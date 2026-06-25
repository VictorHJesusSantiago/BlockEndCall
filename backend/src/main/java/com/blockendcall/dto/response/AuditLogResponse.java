package com.blockendcall.dto.response;

import com.blockendcall.entity.AuditLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuditLogResponse {

    private Long id;
    private Long actorId;
    private String actorName;
    private String action;
    private String targetType;
    private Long targetId;
    private String details;
    private LocalDateTime createdAt;

    public static AuditLogResponse from(AuditLog entity) {
        return AuditLogResponse.builder()
                .id(entity.getId())
                .actorId(entity.getActor() != null ? entity.getActor().getId() : null)
                .actorName(entity.getActor() != null ? entity.getActor().getName() : null)
                .action(entity.getAction().name())
                .targetType(entity.getTargetType())
                .targetId(entity.getTargetId())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
