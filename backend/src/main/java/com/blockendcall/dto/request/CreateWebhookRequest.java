package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateWebhookRequest(

        @NotBlank(message = "Webhook URL is required")
        @Pattern(
                regexp = "^https://.*",
                message = "Webhook URL must use HTTPS"
        )
        @Size(max = 500, message = "Webhook URL must not exceed 500 characters")
        String url,

        @Size(max = 256, message = "Secret must not exceed 256 characters")
        String secret
) {}
