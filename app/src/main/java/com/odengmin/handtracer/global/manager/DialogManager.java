package com.odengmin.handtracer.global.manager;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.odengmin.handtracer.R;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.databinding.ActivityMainBinding;
import com.odengmin.handtracer.fragment.FragmentController;
import com.odengmin.handtracer.fragment.LoginFragment;
import com.valdesekamdem.library.mdtoast.MDToast;

import static android.content.Context.MODE_PRIVATE;

public class DialogManager {
    private MaterialDialog dialogBuilder = null;
    private PreferenceManager preferenceManager;

    @SuppressLint("CommitPrefEdits")
    public DialogManager(AppCompatActivity activity) {
        preferenceManager = new PreferenceManager(activity);
    }

    public MaterialDialog getDialogBuilder() {
        return dialogBuilder;
    }

    public void setDialogBuilder(MaterialDialog dialogBuilder) {
        this.dialogBuilder = dialogBuilder;
    }

    public void openLogoutDialog(AppCompatActivity activity) {
        if(dialogBuilder == null) {
            dialogBuilder = new MaterialDialog.Builder(activity)
                    .title("로그아웃")
                    .content("로그아웃 하시겠습니까?")
                    .positiveText("취소")
                    .negativeText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.NEGATIVE) {
                                MDToast mdToast = MDToast.makeText(activity, "로그아웃 되었습니다.", MDToast.LENGTH_SHORT, MDToast.TYPE_INFO);
                                UserManagement.getInstance()
                                        .requestLogout(new LogoutResponseCallback() {
                                            @Override
                                            public void onCompleteLogout() {
                                                mdToast.show();
                                                FragmentController.changeFragment(activity, new LoginFragment());
                                                ((MainActivity) activity).getBinding().layoutDrawer.closeDrawers();
                                                ((MainActivity) activity).getBinding().appbar.getLayoutParams().height = 0;
                                                ((MainActivity) activity).getBinding().appbar.setVisibility(View.GONE);
                                                String[] userDataArray = new String[] { "email", "profile", "nickname" };
                                                for(String userData : userDataArray) {
                                                    preferenceManager.remove(userData);
                                                }
                                            }
                                        });
                            }
                            dialogBuilder = null;
                        }
                    })
                    .show();
        }
    }

    public void openLocationDialog(AppCompatActivity activity, CompoundButton compoundButton, boolean b) {
        if(dialogBuilder == null) {
            dialogBuilder = new MaterialDialog.Builder(activity)
                    .title("위치정보 저장 " + (b ? "허용" : "거부"))
                    .content("위치정보 저장 제공을 " + (b ? "허용" : "거부") + "하시겠습니까?")
                    .positiveText("취소")
                    .negativeText("확인")
                    .onAny(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            if (which == DialogAction.NEGATIVE) {
                                MDToast mdToast = MDToast.makeText(activity
                                        , "위치정보 저장 제공을 " + (b ? "허용" : "거부") + "하였습니다."
                                        , MDToast.LENGTH_SHORT, b ? MDToast.TYPE_SUCCESS : MDToast.TYPE_WARNING);
                                mdToast.show();
                            } else {
                                compoundButton.setChecked(!b);
                            }
                            dialogBuilder = null;
                            preferenceManager.putBoolean("locationSwitch", compoundButton.isChecked());
                        }
                    })
                    .show();
        }
    }
}
