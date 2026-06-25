package com.blockendcall.dto.response;

import com.blockendcall.entity.ApiKey;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiKeyResponse {

    private Long id;
    private String keyValue;
    private String label;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastUsedAt;

    public static ApiKeyResponse from(ApiKey entity) {
        return ApiKeyResponse.builder()
                .id(entity.getId())
                .keyValue(entity.getKeyValue())
                .label(entity.getLabel())
                .active(entity.isActive())
                .createdAt(entity.getCreatedAt())
                .lastUsedAt(entity.getLastUsedAt())
                .build();
    }
}
