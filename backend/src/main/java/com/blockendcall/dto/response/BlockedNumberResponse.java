package com.blockendcall.dto.response;

import com.blockendcall.entity.BlockedNumber;
import com.blockendcall.enums.SpamCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BlockedNumberResponse {

    private Long id;
    private String phoneNumber;
    private SpamCategory category;
    private int reportCount;
    private int falsePositiveCount;
    private boolean confirmed;
    private boolean whitelisted;
    private int spamScore;
    private String riskLevel;
    private String description;
    private LocalDateTime createdAt;

    public static BlockedNumberResponse from(BlockedNumber entity) {
        int score = entity.getSpamScore();
        String risk = entity.isWhitelisted() ? "WHITELISTED"
                : entity.isConfirmed()       ? "HIGH"
                : score >= 50                ? "MEDIUM"
                : score > 0                  ? "LOW"
                :                             "NONE";

        return BlockedNumberResponse.builder()
                .id(entity.getId())
                .phoneNumber(entity.getPhoneNumber())
                .category(entity.getCategory())
                .reportCount(entity.getReportCount())
                .falsePositiveCount(entity.getFalsePositiveCount())
                .confirmed(entity.isConfirmed())
                .whitelisted(entity.isWhitelisted())
                .spamScore(score)
                .riskLevel(risk)
                .description(entity.getDescription())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
