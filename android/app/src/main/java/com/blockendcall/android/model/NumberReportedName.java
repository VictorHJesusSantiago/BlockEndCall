package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class NumberReportedName {
    @SerializedName("id") private Long id;
    @SerializedName("reportedName") private String reportedName;
    @SerializedName("reportCount") private int reportCount;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public String getReportedName() { return reportedName; }
    public int getReportCount() { return reportCount; }
    public String getCreatedAt() { return createdAt; }
}
