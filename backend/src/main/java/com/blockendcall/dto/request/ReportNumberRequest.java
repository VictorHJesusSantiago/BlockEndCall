package com.blockendcall.dto.request;

import com.blockendcall.enums.SpamCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportNumberRequest {

    @NotBlank
    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}$",
        message = "Phone number must be a valid E.164 format, e.g. +5511999999999"
    )
    private String phoneNumber;

    @NotNull
    private SpamCategory category;

    @Size(max = 500)
    private String description;
}
