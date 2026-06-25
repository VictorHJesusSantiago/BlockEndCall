package com.blockendcall.dto.response;

import com.blockendcall.entity.PublicWhitelist;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PublicWhitelistResponse {

    private Long id;
    private String phoneNumber;
    private String organization;
    private String category;
    private boolean verified;
    private LocalDateTime createdAt;

    public static PublicWhitelistResponse from(PublicWhitelist entity) {
        return PublicWhitelistResponse.builder()
                .id(entity.getId())
                .phoneNumber(entity.getPhoneNumber())
                .organization(entity.getOrganization())
                .category(entity.getCategory())
                .verified(entity.isVerified())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
