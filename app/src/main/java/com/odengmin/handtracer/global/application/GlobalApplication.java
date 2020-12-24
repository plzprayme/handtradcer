package com.odengmin.handtracer.global.application;

import android.app.Application;
import android.content.Context;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;

public class GlobalApplication extends Application {
    private static GlobalApplication instance;

    public static GlobalApplication getGlobalApplicationContext() {
        if (instance == null) {
            throw new IllegalStateException("This Application does not inherit com.kakao.GlobalApplication");
        }

        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Kakao Sdk 초기화
        KakaoSDK.init(new KakaoSDKAdapter());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    public class KakaoSDKAdapter extends KakaoAdapter {

        @Override
        public ISessionConfig getSessionConfig() {
            return new ISessionConfig() {
                @Override
                public AuthType[] getAuthTypes() {
                    return new AuthType[] {AuthType.KAKAO_LOGIN_ALL};
                    /* KAKAO_TALK	                        0	kakaotalk으로 login을 하고 싶을때 지정.
                       KAKAO_STORY	                        1	kakaostory으로 login을 하고 싶을때 지정.
                       KAKAO_ACCOUNT	                    2	웹뷰 Dialog를 통해 카카오 계정연결을 제공하고 싶을경우 지정.
                       KAKAO_TALK_EXCLUDE_NATIVE_LOGIN	    3	카카오톡으로만 로그인을 유도하고 싶으면서 계정이 없을때 계정생성을 위한
                                                                버튼도 같이 제공을 하고 싶다면 지정.
                                                                KAKAO_TALK과 중복 지정불가.
                       KAKAO_LOGIN_ALL	                    4	모든 로그인 방식을 사용하고 싶을 때 지정. */
                }

                @Override
                public boolean isUsingWebviewTimer() {
                    return false;
                }

                @Override
                public boolean isSecureMode() {
                    return false;
                }

                @Override
                public ApprovalType getApprovalType() {
                    return ApprovalType.INDIVIDUAL;
                }

                @Override
                public boolean isSaveFormData() {
                    return true;
                }
            };
        }

        // Application이 가지고 있는 정보를 얻기 위한 인터페이스
        @Override
        public IApplicationConfig getApplicationConfig() {
            return new IApplicationConfig() {
                @Override
                public Context getApplicationContext() {
                    return GlobalApplication.getGlobalApplicationContext();
                }
            };
        }
    }
}