package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class AdminUser {
    @SerializedName("id") private Long id;
    @SerializedName("name") private String name;
    @SerializedName("email") private String email;
    @SerializedName("role") private String role;
    @SerializedName("reputationScore") private int reputationScore;
    @SerializedName("suspended") private boolean suspended;
    @SerializedName("active") private boolean active;
    @SerializedName("totalReports") private long totalReports;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public int getReputationScore() { return reputationScore; }
    public boolean isSuspended() { return suspended; }
    public boolean isActive() { return active; }
    public long getTotalReports() { return totalReports; }
    public String getCreatedAt() { return createdAt; }
}
