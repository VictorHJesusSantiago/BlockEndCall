package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PersonalListRequest {

    @NotBlank
    @Size(max = 30)
    private String phoneNumber;

    @Size(max = 200)
    private String note;
}
