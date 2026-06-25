package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddPublicWhitelistRequest {

    @NotBlank
    private String phoneNumber;

    @NotBlank
    @Size(max = 200)
    private String organization;

    private String category;

    private boolean verified;
}
