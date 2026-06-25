package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class NumberTimeline {
    @SerializedName("id") private Long id;
    @SerializedName("eventType") private String eventType;
    @SerializedName("details") private String details;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public String getEventType() { return eventType; }
    public String getDetails() { return details; }
    public String getCreatedAt() { return createdAt; }
}
