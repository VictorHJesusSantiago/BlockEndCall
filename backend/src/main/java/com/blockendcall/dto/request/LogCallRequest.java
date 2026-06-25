package com.blockendcall.dto.request;

import com.blockendcall.enums.BlockedCallResult;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LogCallRequest {

    @NotBlank
    private String phoneNumber;

    private BlockedCallResult blockResult;

    private Long matchedNumberId;
}
