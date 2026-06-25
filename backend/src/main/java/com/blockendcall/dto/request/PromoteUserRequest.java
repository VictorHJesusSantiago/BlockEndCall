package com.blockendcall.dto.request;

import com.blockendcall.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PromoteUserRequest {

    @NotNull
    private Long userId;

    @NotNull
    private UserRole newRole;
}
