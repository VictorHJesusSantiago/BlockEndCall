package com.blockendcall.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class BulkActionRequest {

    @NotEmpty
    private List<Long> ids;

    private String action;
}
