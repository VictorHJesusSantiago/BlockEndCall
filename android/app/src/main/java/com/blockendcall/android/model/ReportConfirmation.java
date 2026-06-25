package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class ReportConfirmation {
    @SerializedName("id") private Long id;
    @SerializedName("numberId") private Long numberId;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public Long getNumberId() { return numberId; }
    public String getCreatedAt() { return createdAt; }
}
