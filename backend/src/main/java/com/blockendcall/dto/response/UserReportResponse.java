package com.blockendcall.dto.response;

import com.blockendcall.entity.Report;
import com.blockendcall.enums.CallFrequency;
import com.blockendcall.enums.SpamCategory;
import com.blockendcall.enums.SpamSubcategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserReportResponse {

    private Long reportId;
    private Long numberId;
    private String phoneNumber;
    private SpamCategory category;
    private SpamSubcategory subcategory;
    private String callerName;
    private CallFrequency callFrequency;
    private int totalReports;
    private boolean confirmed;
    private String description;
    private LocalDateTime reportedAt;

    public static UserReportResponse from(Report report) {
        return UserReportResponse.builder()
                .reportId(report.getId())
                .numberId(report.getBlockedNumber().getId())
                .phoneNumber(report.getBlockedNumber().getPhoneNumber())
                .category(report.getBlockedNumber().getCategory())
                .subcategory(report.getSubcategory())
                .callerName(report.getCallerName())
                .callFrequency(report.getCallFrequency())
                .totalReports(report.getBlockedNumber().getReportCount())
                .confirmed(report.getBlockedNumber().isConfirmed())
                .description(report.getDescription())
                .reportedAt(report.getCreatedAt())
                .build();
    }
}
