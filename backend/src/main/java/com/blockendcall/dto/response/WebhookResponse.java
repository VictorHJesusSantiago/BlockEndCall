package com.blockendcall.dto.response;

import com.blockendcall.entity.Webhook;

import java.time.LocalDateTime;

public record WebhookResponse(
        Long id,
        String url,
        boolean active,
        LocalDateTime createdAt
) {
    public static WebhookResponse from(Webhook webhook) {
        return new WebhookResponse(
                webhook.getId(),
                webhook.getUrl(),
                webhook.isActive(),
                webhook.getCreatedAt()
        );
    }
}
