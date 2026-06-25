package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitReportedNameRequest {

    @NotBlank
    @Size(max = 200)
    private String reportedName;
}
