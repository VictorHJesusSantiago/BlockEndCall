package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class BlockedNumber {

    @SerializedName("id")
    private Long id;

    @SerializedName("phoneNumber")
    private String phoneNumber;

    @SerializedName("category")
    private String category;

    @SerializedName("reportCount")
    private int reportCount;

    @SerializedName("confirmed")
    private boolean confirmed;

    @SerializedName("description")
    private String description;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getCategory() { return category; }
    public int getReportCount() { return reportCount; }
    public boolean isConfirmed() { return confirmed; }
    public String getDescription() { return description; }
    public String getCreatedAt() { return createdAt; }
}
