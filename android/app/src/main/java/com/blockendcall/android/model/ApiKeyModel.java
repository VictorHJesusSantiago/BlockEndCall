package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class ApiKeyModel {
    @SerializedName("id") private Long id;
    @SerializedName("keyValue") private String keyValue;
    @SerializedName("label") private String label;
    @SerializedName("active") private boolean active;
    @SerializedName("createdAt") private String createdAt;
    @SerializedName("lastUsedAt") private String lastUsedAt;
    public Long getId() { return id; }
    public String getKeyValue() { return keyValue; }
    public String getLabel() { return label; }
    public boolean isActive() { return active; }
    public String getCreatedAt() { return createdAt; }
    public String getLastUsedAt() { return lastUsedAt; }
}
