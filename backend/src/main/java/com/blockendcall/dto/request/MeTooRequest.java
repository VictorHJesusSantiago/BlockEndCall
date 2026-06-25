package com.blockendcall.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MeTooRequest {

    @Size(max = 200)
    private String note;
}
