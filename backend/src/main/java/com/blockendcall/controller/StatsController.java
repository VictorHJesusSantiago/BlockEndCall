package com.blockendcall.controller;

import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.StatsResponse;
import com.blockendcall.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Community spam statistics")
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    @Operation(summary = "Global spam statistics — total blocked, by category, trending (no auth)")
    public ResponseEntity<ApiResponse<StatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getGlobalStats()));
    }
}
