package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class Stats {

    @SerializedName("totalConfirmed")
    private long totalConfirmed;

    @SerializedName("totalPending")
    private long totalPending;

    @SerializedName("totalReports")
    private long totalReports;

    @SerializedName("totalUsers")
    private long totalUsers;

    @SerializedName("byCategory")
    private Map<String, Long> byCategory;

    @SerializedName("trending")
    private List<BlockedNumber> trending;

    @SerializedName("recentlyAdded")
    private List<BlockedNumber> recentlyAdded;

    public long getTotalConfirmed() { return totalConfirmed; }
    public long getTotalPending() { return totalPending; }
    public long getTotalReports() { return totalReports; }
    public long getTotalUsers() { return totalUsers; }
    public Map<String, Long> getByCategory() { return byCategory; }
    public List<BlockedNumber> getTrending() { return trending; }
    public List<BlockedNumber> getRecentlyAdded() { return recentlyAdded; }
}
