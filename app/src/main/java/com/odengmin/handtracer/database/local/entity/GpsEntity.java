package com.odengmin.handtracer.database.local.entity;

import android.icu.text.SimpleDateFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity
public class GpsEntity implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String latitude;
    public String longitude;
    public String created_at;
    @ColumnInfo(name = "da_te")
    public String date;
    public String time;

    @Ignore Boolean selected = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    public GpsEntity(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        created_at = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
        date = created_at.split("\\s")[0];
        time = created_at.split("\\s")[1];
    }

    public String getDate() {
        return date.replaceAll("-", "/");
    }

    public String getTime() {
        String[] before = time.split(":");
        return before[0] + ":" + before[1];
    }
}
