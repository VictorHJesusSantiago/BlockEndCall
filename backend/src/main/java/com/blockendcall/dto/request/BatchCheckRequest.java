package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class BatchCheckRequest {

    @NotEmpty(message = "Phone number list must not be empty")
    @Size(max = 20, message = "Maximum 20 numbers per batch")
    private List<String> phoneNumbers;
}
