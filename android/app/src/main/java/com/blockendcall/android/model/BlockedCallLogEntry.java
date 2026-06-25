package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class BlockedCallLogEntry {
    @SerializedName("id") private Long id;
    @SerializedName("phoneNumber") private String phoneNumber;
    @SerializedName("blockedAt") private String blockedAt;
    @SerializedName("blockResult") private String blockResult;
    @SerializedName("matchedNumberId") private Long matchedNumberId;
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getBlockedAt() { return blockedAt; }
    public String getBlockResult() { return blockResult; }
    public Long getMatchedNumberId() { return matchedNumberId; }
}
