package com.blockendcall.android.model;

import com.google.gson.annotations.SerializedName;

public class UserProfile {

    @SerializedName("id")
    private Long id;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("role")
    private String role;

    @SerializedName("totalReports")
    private long totalReports;

    @SerializedName("createdAt")
    private String createdAt;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone != null ? phone : ""; }
    public String getRole() { return role != null ? role : "USER"; }
    public long getTotalReports() { return totalReports; }
    public String getCreatedAt() { return createdAt != null ? createdAt : ""; }
}
