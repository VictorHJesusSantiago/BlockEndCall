package com.blockendcall.dto.response;

import com.blockendcall.entity.PersonalBlacklist;
import com.blockendcall.entity.PersonalWhitelist;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PersonalListResponse {

    private Long id;
    private String phoneNumber;
    private String note;
    private LocalDateTime createdAt;

    public static PersonalListResponse from(PersonalWhitelist entity) {
        return PersonalListResponse.builder()
                .id(entity.getId())
                .phoneNumber(entity.getPhoneNumber())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public static PersonalListResponse from(PersonalBlacklist entity) {
        return PersonalListResponse.builder()
                .id(entity.getId())
                .phoneNumber(entity.getPhoneNumber())
                .note(entity.getNote())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
