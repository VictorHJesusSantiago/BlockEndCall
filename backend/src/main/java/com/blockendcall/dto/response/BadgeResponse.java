package com.blockendcall.dto.response;

import com.blockendcall.entity.UserBadge;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BadgeResponse {

    private String badgeType;
    private String displayName;
    private LocalDateTime awardedAt;

    public static BadgeResponse from(UserBadge entity) {
        return BadgeResponse.builder()
                .badgeType(entity.getBadgeType().name())
                .displayName(entity.getBadgeType().getDisplayName())
                .awardedAt(entity.getAwardedAt())
                .build();
    }
}
