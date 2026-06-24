package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class NumberCheckResult {

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("blocked")
    private boolean blocked;

    @SerializedName("confirmed")
    private boolean confirmed;

    @SerializedName("category")
    private String category;

    @SerializedName("reportCount")
    private int reportCount;

    @SerializedName("spamScore")
    private int spamScore;

    @SerializedName("riskLevel")
    private String riskLevel;

    @SerializedName("description")
    private String description;

    public String getPhoneNumber() { return phoneNumber; }
    public boolean isBlocked() { return blocked; }
    public boolean isConfirmed() { return confirmed; }
    public String getCategory() { return category; }
    public int getReportCount() { return reportCount; }
    public int getSpamScore() { return spamScore; }
    public String getRiskLevel() { return riskLevel != null ? riskLevel : "SAFE"; }
    public String getDescription() { return description; }
}
