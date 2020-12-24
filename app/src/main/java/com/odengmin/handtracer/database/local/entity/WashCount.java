package com.odengmin.handtracer.database.local.entity;

import android.icu.text.SimpleDateFormat;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.Locale;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Entity(indices = {@Index(value = {"da_te"})})
public class WashCount {

    public WashCount() {
        this.date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "da_te")
    public String date;

    @ColumnInfo(defaultValue = "0")
    public int washCount;

    public void increaseCount() {
        washCount += 1;
    }
}
