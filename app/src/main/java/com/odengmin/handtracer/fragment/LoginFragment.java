package com.odengmin.handtracer.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.MapFragment;
import com.kakao.auth.AuthType;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.response.model.Profile;
import com.odengmin.handtracer.R;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.databinding.FragmentLoginBinding;
import com.odengmin.handtracer.global.manager.PreferenceManager;
import com.odengmin.handtracer.global.manager.SessionManager;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private MainActivity mainActivity;
    private Animation animationFadeIn;
    private Animation animationFadeOut;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        View root = binding.getRoot();

        animationFadeIn = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in);
        animationFadeOut = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_out);
        binding.btnLoginImage.startAnimation(animationFadeIn);
        binding.textLogo.startAnimation(animationFadeIn);
        binding.handtracerLogo.startAnimation(animationFadeIn);


        return root;
    }
}