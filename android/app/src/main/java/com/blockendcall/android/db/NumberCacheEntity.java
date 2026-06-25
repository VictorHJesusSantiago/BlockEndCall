package com.blockendcall.android.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "number_cache")
public class NumberCacheEntity {
    @PrimaryKey
    @NonNull
    public String phoneNumber = "";
    public boolean blocked;
    public boolean confirmed;
    public String category;
    public int reportCount;
    public int spamScore;
    public String riskLevel;
    public long cachedAt;
}
