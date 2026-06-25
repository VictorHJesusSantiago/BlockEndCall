package com.blockendcall.dto.response;

import com.blockendcall.entity.NumberTimelineEvent;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NumberTimelineResponse {

    private Long id;
    private String eventType;
    private String details;
    private LocalDateTime createdAt;

    public static NumberTimelineResponse from(NumberTimelineEvent entity) {
        return NumberTimelineResponse.builder()
                .id(entity.getId())
                .eventType(entity.getEventType())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
