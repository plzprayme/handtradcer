package com.odengmin.handtracer.fragment;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.kakao.usermgmt.response.model.Profile;
import com.odengmin.handtracer.R;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.databinding.ActivityMainBinding;
import com.odengmin.handtracer.global.manager.SessionManager;

public class FragmentController {

    public static final int LOGIN_FRAGMENT = 1;
    public static final int MAPS_FRAGMENT = 2;

    public static void initFragment(int fragmentNumber, AppCompatActivity activity, String email) {
        Fragment fragment = null;
        switch (fragmentNumber) {
            case LOGIN_FRAGMENT:
                fragment = new LoginFragment();
                break;
            case MAPS_FRAGMENT:
                fragment = new MapsFragment(email);
                break;
        }
        assert fragment != null;
        activity.getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, fragment, "" + fragment.getClass())
                .commit();
    }

    public static void changeFragment(AppCompatActivity activity, Fragment fragment) {
        if (activity.getSupportFragmentManager().findFragmentByTag(fragment.getClass().toString()) == null) {
            FragmentManager fragmentManager = activity.getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_right, R.anim.enter_from_right, R.anim.exit_to_right);
            //fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.fragment_container, fragment, "" + fragment.getClass());
            fragmentTransaction.commitAllowingStateLoss();
        }
    }

    public static boolean isActivityAvailable(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return !activity.isFinishing() && !activity.isDestroyed();
        } else {
            return !activity.isFinishing();
        }
    }
}
