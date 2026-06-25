package com.blockendcall.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LeaderboardEntry {

    private Long userId;
    private String name;
    private int totalReports;
    private int reputationScore;
    private int rank;
}
