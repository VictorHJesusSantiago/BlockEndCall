package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class LeaderboardEntry {
    @SerializedName("userId") private Long userId;
    @SerializedName("name") private String name;
    @SerializedName("totalReports") private int totalReports;
    @SerializedName("reputationScore") private int reputationScore;
    @SerializedName("rank") private int rank;
    public Long getUserId() { return userId; }
    public String getName() { return name; }
    public int getTotalReports() { return totalReports; }
    public int getReputationScore() { return reputationScore; }
    public int getRank() { return rank; }
}
