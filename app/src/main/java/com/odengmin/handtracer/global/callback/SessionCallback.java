package com.odengmin.handtracer.global.callback;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.kakao.auth.ISessionCallback;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeV2ResponseCallback;
import com.kakao.usermgmt.response.MeV2Response;
import com.kakao.usermgmt.response.model.Profile;
import com.kakao.usermgmt.response.model.UserAccount;
import com.kakao.util.OptionalBoolean;
import com.kakao.util.exception.KakaoException;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.databinding.NavHeaderBinding;
import com.odengmin.handtracer.fragment.FragmentController;
import com.odengmin.handtracer.fragment.LoginFragment;
import com.odengmin.handtracer.fragment.MapsFragment;
import com.odengmin.handtracer.global.manager.PreferenceManager;
import com.odengmin.handtracer.global.manager.SessionManager;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class SessionCallback implements ISessionCallback {
    private AppCompatActivity activity;
    private String email;
    private Profile profile;

    public SessionCallback(AppCompatActivity activity) {
        this.activity = activity;
    }

    // 로그인에 성공한 상태
    @Override
    public void onSessionOpened() {
        requestMe();
    }

    // 로그인에 실패한 상태
    @Override
    public void onSessionOpenFailed(KakaoException exception) {
        Log.e("SessionCallback :: ", "onSessionOpenFailed : " + exception.getMessage());
    }

    // 사용자 정보 요청
    public void requestMe() {
        UserManagement.getInstance()
                .me(new MeV2ResponseCallback() {
                    @Override
                    public void onSessionClosed(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "세션이 닫혀 있음: " + errorResult);
                    }

                    @Override
                    public void onFailure(ErrorResult errorResult) {
                        Log.e("KAKAO_API", "사용자 정보 요청 실패: " + errorResult);
                    }

                    @Override
                    public void onSuccess(MeV2Response result) {
                        Log.i("KAKAO_API", "사용자 아이디: " + result.getId());

                        UserAccount kakaoAccount = result.getKakaoAccount();
                        if (kakaoAccount != null) {

                            // 이메일
                            email = kakaoAccount.getEmail();

                            if (email != null) {
                                Log.i("KAKAO_API", "email: " + email);

                            } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 이메일 획득 가능
                                // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.

                            } else {
                                // 이메일 획득 불가
                            }

                            // 프로필
                            profile = kakaoAccount.getProfile();

                            if (profile != null) {
                                Log.d("KAKAO_API", "nickname: " + profile.getNickname());
                                Log.d("KAKAO_API", "profile image: " + profile.getProfileImageUrl());
                                Log.d("KAKAO_API", "thumbnail image: " + profile.getThumbnailImageUrl());
                                PreferenceManager preferenceManager = new PreferenceManager(activity);
                                List<Pair<String, String>> userDataList = new ArrayList<Pair<String, String>>(
                                        Arrays.asList(Pair.create("nickname",  profile.getNickname())
                                                , Pair.create("profile",  profile.getProfileImageUrl())
                                                , Pair.create("email",  email))
                                );
                                for(Pair<String, String> userData : userDataList) {
                                    preferenceManager.putString(userData.first, userData.second);
                                    System.out.println("data : " + userData.first + " " + userData.second);
                                }
                                SessionManager sessionManager = new SessionManager(activity);
                                sessionManager.putSessionPreference();
                                FragmentController.changeFragment(activity, new MapsFragment(preferenceManager.getString("email", null)));

                                /*
                                if(!activity.isDestroyed() || !activity.isFinishing()) {
                                    if (activity.getSupportFragmentManager().findFragmentByTag(MapsFragment.class.toString()) == null) {
                                        FragmentController.changeFragment(activity, new MapsFragment(preferenceManager.getString("email", null)));
                                    }
                                }
                                */
                                //} else {
                                //    FragmentController.initFragment(FragmentController.MAPS_FRAGMENT
                                //            , activity
                                //            , email
                                //            , profile);
                                //}
                            } else if (kakaoAccount.profileNeedsAgreement() == OptionalBoolean.TRUE) {
                                // 동의 요청 후 프로필 정보 획득 가능

                            } else {
                                // 프로필 획득 불가
                            }
                        }
                    }
                });
    }

}