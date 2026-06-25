package com.blockendcall.dto.response;

import com.blockendcall.enums.SpamCategory;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NumberCheckResponse {

    private String phoneNumber;
    private boolean blocked;
    private boolean confirmed;
    private SpamCategory category;
    private int reportCount;
    private int spamScore;
    private String description;
    private String riskLevel;
    private boolean inPublicWhitelist;
    private boolean inPersonalWhitelist;

    public static NumberCheckResponse safe(String phoneNumber) {
        return NumberCheckResponse.builder()
                .phoneNumber(phoneNumber)
                .blocked(false)
                .confirmed(false)
                .spamScore(0)
                .riskLevel("SAFE")
                .build();
    }

    public static NumberCheckResponse from(BlockedNumberResponse r) {
        int score = Math.min(100, r.getReportCount() * 10);
        String risk = r.isConfirmed() ? "HIGH" : (r.getReportCount() >= 3 ? "MEDIUM" : "LOW");

        return NumberCheckResponse.builder()
                .phoneNumber(r.getPhoneNumber())
                .blocked(true)
                .confirmed(r.isConfirmed())
                .category(r.getCategory())
                .reportCount(r.getReportCount())
                .spamScore(score)
                .description(r.getDescription())
                .riskLevel(risk)
                .build();
    }
}
