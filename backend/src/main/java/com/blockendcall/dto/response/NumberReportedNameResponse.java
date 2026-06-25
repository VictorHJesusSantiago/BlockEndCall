package com.blockendcall.dto.response;

import com.blockendcall.entity.NumberReportedName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NumberReportedNameResponse {

    private Long id;
    private String reportedName;
    private int reportCount;
    private LocalDateTime createdAt;

    public static NumberReportedNameResponse from(NumberReportedName entity) {
        return NumberReportedNameResponse.builder()
                .id(entity.getId())
                .reportedName(entity.getReportedName())
                .reportCount(entity.getReportCount())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
