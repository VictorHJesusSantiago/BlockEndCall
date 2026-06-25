package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class Badge {
    @SerializedName("badgeType") private String badgeType;
    @SerializedName("displayName") private String displayName;
    @SerializedName("awardedAt") private String awardedAt;
    public String getBadgeType() { return badgeType; }
    public String getDisplayName() { return displayName; }
    public String getAwardedAt() { return awardedAt; }
}
