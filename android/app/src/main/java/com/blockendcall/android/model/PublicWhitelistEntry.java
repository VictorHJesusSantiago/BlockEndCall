package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class PublicWhitelistEntry {
    @SerializedName("id") private Long id;
    @SerializedName("phoneNumber") private String phoneNumber;
    @SerializedName("organization") private String organization;
    @SerializedName("category") private String category;
    @SerializedName("verified") private boolean verified;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getOrganization() { return organization; }
    public String getCategory() { return category; }
    public boolean isVerified() { return verified; }
    public String getCreatedAt() { return createdAt; }
}
