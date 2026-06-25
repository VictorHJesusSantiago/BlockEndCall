package com.blockendcall.android.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface NumberCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(NumberCacheEntity entity);

    @Query("SELECT * FROM number_cache WHERE phoneNumber = :phone")
    NumberCacheEntity findByPhone(String phone);

    @Query("DELETE FROM number_cache WHERE cachedAt < :threshold")
    void deleteExpired(long threshold);

    @Query("DELETE FROM number_cache")
    void deleteAll();
}
