package com.odengmin.handtracer.global.manager;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kakao.auth.Session;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.database.local.GpsDataBase;
import com.odengmin.handtracer.databinding.NavHeaderBinding;
import com.odengmin.handtracer.global.callback.SessionCallback;

public class SessionManager {
    private SessionCallback sessionCallback;
    private Session session;
    private AppCompatActivity activity;

    public SessionManager(AppCompatActivity activity) {
        this.activity = activity;
        sessionCallback = new SessionCallback(activity);
        session = Session.getCurrentSession();
        session.addCallback(sessionCallback);
    }

    public Session getSession() {
        return session;
    }

    public boolean checkSession() {
        // 세션 유지 확인
        return Session.getCurrentSession().checkAndImplicitOpen();
    }

    public SessionCallback getSessionCallback() {
        return sessionCallback;
    }

    public void putSessionPreference() {
        if(!activity.isFinishing()) {
            MainActivity MainActivity = ((MainActivity) this.activity);
            NavHeaderBinding navHeaderBinding = MainActivity.getNavHeaderBinding();
            navHeaderBinding.navUserText.setText(MainActivity.getPreferenceManager().getString("nickname", null));
            navHeaderBinding.navUserEmail.setText(MainActivity.getPreferenceManager().getString("email", null));
            Glide.with(MainActivity)
                    .load(MainActivity.getPreferenceManager().getString("profile", null))
                    .override(200, 180)
                    .into(navHeaderBinding.navProfileImage);
        }
    }
}
