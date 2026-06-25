package com.blockendcall.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class EnhancedStatsResponse {

    // Base stats
    private long totalConfirmed;
    private long totalPending;
    private long totalReports;
    private long totalUsers;
    private Map<String, Long> byCategory;
    private List<BlockedNumberResponse> trending;
    private List<BlockedNumberResponse> recentlyAdded;

    // Enhanced stats
    private Map<String, Long> byDdd;
    private List<DailyCount> dailyCounts;
    private Map<Integer, Long> peakHours;
    private double falsePositiveRate;
    private double accuracyRate;

    @Data
    @Builder
    public static class DailyCount {
        private String date;
        private long count;
    }
}
