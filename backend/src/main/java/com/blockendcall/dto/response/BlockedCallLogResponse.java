package com.blockendcall.dto.response;

import com.blockendcall.entity.ServerBlockedCallLog;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlockedCallLogResponse {

    private Long id;
    private String phoneNumber;
    private LocalDateTime blockedAt;
    private String blockResult;
    private Long matchedNumberId;

    public static BlockedCallLogResponse from(ServerBlockedCallLog entity) {
        return BlockedCallLogResponse.builder()
                .id(entity.getId())
                .phoneNumber(entity.getPhoneNumber())
                .blockedAt(entity.getBlockedAt())
                .blockResult(entity.getBlockResult() != null ? entity.getBlockResult().name() : null)
                .matchedNumberId(entity.getMatchedNumber() != null ? entity.getMatchedNumber().getId() : null)
                .build();
    }
}
