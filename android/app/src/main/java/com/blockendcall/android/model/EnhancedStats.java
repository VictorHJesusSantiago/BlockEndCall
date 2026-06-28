package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class EnhancedStats {
    @SerializedName("totalConfirmed") private long totalConfirmed;
    @SerializedName("totalPending") private long totalPending;
    @SerializedName("totalReports") private long totalReports;
    @SerializedName("totalUsers") private long totalUsers;
    @SerializedName("byCategory") private Map<String, Long> byCategory;
    @SerializedName("trending") private List<BlockedNumber> trending;
    @SerializedName("dailyCounts") private List<DailyCount> dailyCounts;
    @SerializedName("peakHours") private Map<String, Long> peakHours;
    @SerializedName("falsePositiveRate") private double falsePositiveRate;
    @SerializedName("accuracyRate") private double accuracyRate;

    public long getTotalConfirmed() { return totalConfirmed; }
    public long getTotalPending() { return totalPending; }
    public long getTotalReports() { return totalReports; }
    public long getTotalUsers() { return totalUsers; }
    public Map<String, Long> getByCategory() { return byCategory; }
    public List<BlockedNumber> getTrending() { return trending; }
    public List<DailyCount> getDailyCounts() { return dailyCounts; }
    public Map<String, Long> getPeakHours() { return peakHours; }
    public double getFalsePositiveRate() { return falsePositiveRate; }
    public double getAccuracyRate() { return accuracyRate; }

    public static class DailyCount {
        @SerializedName("date") private String date;
        @SerializedName("count") private long count;
        public String getDate() { return date; }
        public long getCount() { return count; }
    }
}
