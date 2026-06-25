package com.blockendcall.controller;

import com.blockendcall.dto.response.ApiResponse;
import com.blockendcall.dto.response.EnhancedStatsResponse;
import com.blockendcall.dto.response.LeaderboardEntry;
import com.blockendcall.dto.response.StatsResponse;
import com.blockendcall.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/enhanced")
    @Operation(summary = "Enhanced statistics with DDD breakdown, daily counts, and peak hours (no auth)")
    public ResponseEntity<ApiResponse<EnhancedStatsResponse>> getEnhancedStats() {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getEnhancedStats()));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Top reporters leaderboard (no auth)")
    public ResponseEntity<ApiResponse<List<LeaderboardEntry>>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getLeaderboard(limit)));
    }

    @GetMapping("/by-ddd")
    @Operation(summary = "Spam counts grouped by DDD area code (no auth)")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStatsByDdd() {
        return ResponseEntity.ok(ApiResponse.ok(statsService.getStatsByDdd()));
    }
}
