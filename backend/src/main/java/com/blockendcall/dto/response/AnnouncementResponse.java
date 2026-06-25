package com.blockendcall.dto.response;

import com.blockendcall.entity.Announcement;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AnnouncementResponse {

    private Long id;
    private String title;
    private String body;
    private String authorName;
    private LocalDateTime createdAt;

    public static AnnouncementResponse from(Announcement entity) {
        return AnnouncementResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .body(entity.getBody())
                .authorName(entity.getAuthor() != null ? entity.getAuthor().getName() : null)
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
