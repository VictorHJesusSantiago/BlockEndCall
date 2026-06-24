package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class UserReport {

    @SerializedName("reportId")
    private Long reportId;

    @SerializedName("numberId")
    private Long numberId;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("category")
    private String category;

    @SerializedName("totalReports")
    private int totalReports;

    @SerializedName("confirmed")
    private boolean confirmed;

    @SerializedName("description")
    private String description;

    @SerializedName("reportedAt")
    private String reportedAt;

    public Long getReportId() { return reportId; }
    public Long getNumberId() { return numberId; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCategory() { return category; }
    public int getTotalReports() { return totalReports; }
    public boolean isConfirmed() { return confirmed; }
    public String getDescription() { return description; }
    public String getReportedAt() { return reportedAt; }
}
