package com.odengmin.handtracer.database.local;

import android.content.Context;
import android.util.Log;

import androidx.room.*;

import com.odengmin.handtracer.database.local.dao.GpsDao;
import com.odengmin.handtracer.database.local.entity.GpsEntity;
import com.odengmin.handtracer.database.local.entity.WashCount;

@Database(entities = {GpsEntity.class, WashCount.class}, version = 4)
public abstract class GpsDataBase extends RoomDatabase {
    public abstract GpsDao GpsDao();
    private static GpsDataBase instance;
    private static final Object lock = new Object();

    // Room Database SingleTon
    public static GpsDataBase getInstance(Context context) {
        synchronized (lock) { // 비동기화
            if(instance == null) {
                instance = Room.databaseBuilder(context.getApplicationContext(),
                        GpsDataBase.class, "gps-db")
                        .fallbackToDestructiveMigration()
                        .build();
                Log.i("gpsDataBase", "inited");
            }
            return instance;
        }
    }
}
