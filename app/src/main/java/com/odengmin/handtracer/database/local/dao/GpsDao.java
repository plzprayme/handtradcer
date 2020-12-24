package com.odengmin.handtracer.database.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.odengmin.handtracer.database.local.entity.GpsEntity;
import com.odengmin.handtracer.database.local.entity.WashCount;

import java.util.List;

@Dao
public interface GpsDao {
    @Query("SELECT * FROM GpsEntity ORDER BY id DESC")
    LiveData<List<GpsEntity>> getAllGps();

    //@Query("SELECT *  FROM GpsEntity WHERE created_at LIKE '%' || :date || '%'")
    @Query("SELECT * FROM GpsEntity WHERE da_te = :date ORDER BY da_te ASC")
    List<GpsEntity> getDateGps(String date);

    @Query("SELECT * FROM GpsEntity ORDER BY id DESC LIMIT 1")
    GpsEntity getLastGps();

    @Insert
    void insertUsers(GpsEntity users);

    @Update
    void updateUsers(GpsEntity users);

    @Delete
    void deleteUsers(GpsEntity users);


//    wash count dao
    @Query("SELECT * From washcount WHERE da_te=:date")
    WashCount getTodayWashCount(String date);

    @Insert
    void insertWashCount(WashCount washCount);

    @Update
    void updateWashCount(WashCount washCount);

}
