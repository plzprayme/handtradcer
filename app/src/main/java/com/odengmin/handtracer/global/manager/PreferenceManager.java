package com.odengmin.handtracer.global.manager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceManager {
    private SharedPreferences preference;
    private SharedPreferences.Editor preferenceEditor;

    @SuppressLint("CommitPrefEdits")
    public PreferenceManager(Context context) {
        preference = context.getSharedPreferences("com.odengmin.handtracer", MODE_PRIVATE);
        preferenceEditor = context.getSharedPreferences("com.odengmin.handtracer", MODE_PRIVATE).edit();
    }

    public void putString(String key, String value) {
        preferenceEditor.putString(key, value);
        preferenceEditor.commit();
    }

    public void putBoolean(String key, Boolean value) {
        preferenceEditor.putBoolean(key, value);
        preferenceEditor.commit();
    }

    public String getString(String key, String defaultValue) {
        return preference.getString(key, defaultValue);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        return preference.getBoolean(key, defaultValue);
    }

    public boolean checkExist(String key) {
        return preference.contains(key);
    }

    public void remove(String key) {
        preferenceEditor.remove(key);
        preferenceEditor.commit();
    }
}
