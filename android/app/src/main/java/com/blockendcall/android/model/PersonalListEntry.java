package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class PersonalListEntry {
    @SerializedName("id") private Long id;
    @SerializedName("phoneNumber") private String phoneNumber;
    @SerializedName("note") private String note;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getNote() { return note; }
    public String getCreatedAt() { return createdAt; }
}
