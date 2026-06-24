package com.blockendcall.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class StatsResponse {

    private long totalConfirmed;
    private long totalPending;
    private long totalReports;
    private long totalUsers;
    private Map<String, Long> byCategory;
    private List<BlockedNumberResponse> trending;
    private List<BlockedNumberResponse> recentlyAdded;
}
