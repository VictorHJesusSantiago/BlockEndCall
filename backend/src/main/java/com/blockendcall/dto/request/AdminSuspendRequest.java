package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminSuspendRequest {

    @NotNull
    private Long userId;

    private boolean suspend;

    private String reason;
}
