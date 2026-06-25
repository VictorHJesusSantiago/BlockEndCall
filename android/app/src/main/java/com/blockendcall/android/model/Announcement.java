package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class Announcement {
    @SerializedName("id") private Long id;
    @SerializedName("title") private String title;
    @SerializedName("body") private String body;
    @SerializedName("authorName") private String authorName;
    @SerializedName("createdAt") private String createdAt;
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public String getAuthorName() { return authorName; }
    public String getCreatedAt() { return createdAt; }
}
