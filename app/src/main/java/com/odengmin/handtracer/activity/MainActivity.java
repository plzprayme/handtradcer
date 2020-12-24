package com.odengmin.handtracer.activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewbinding.BuildConfig;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.response.model.Profile;
import com.odengmin.handtracer.R;
import com.odengmin.handtracer.databinding.ActivityMainBinding;
import com.odengmin.handtracer.databinding.NavHeaderBinding;
import com.odengmin.handtracer.fragment.FragmentController;
import com.odengmin.handtracer.fragment.MapsFragment;
import com.odengmin.handtracer.global.animation.SlideAnimation;
import com.odengmin.handtracer.global.manager.DialogManager;
import com.odengmin.handtracer.global.manager.PreferenceManager;
import com.odengmin.handtracer.global.manager.SessionManager;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
        , SwitchMaterial.OnCheckedChangeListener {

    private ActivityMainBinding binding;
    private boolean calendarOpened = false;
    private int toolbarHeight = 0;
    private int calendarHeight = 0;
    private NavHeaderBinding navHeaderBinding;
    private AppCompatActivity mainActivity;
    private DialogManager dialogManager;
    private SwitchCompat locationSwitchCompat;
    private MenuItem locationMenuItem;
    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    private PreferenceManager preferenceManager;
    private SlideAnimation slideAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = this;
        preferenceManager = new PreferenceManager(this);


        // 키 해시 가져오기
//        String keyHash = com.kakao.util.helper.Utility.getKeyHash(this /* context */);
//        Log.i("Key Hash", keyHash);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        navHeaderBinding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.nav_header, binding
                .navigation, true);

        if(navHeaderBinding != null) {
            dialogManager = new DialogManager(this);
            binding.navigation.setNavigationItemSelectedListener(this);
            locationMenuItem = binding.navigation.getMenu().findItem(R.id.setting);
            locationSwitchCompat = locationMenuItem.getActionView().findViewById(R.id.drawer_switch);
            locationSwitchCompat.setChecked(preferenceManager.getBoolean("locationSwitch", true));
            locationSwitchCompat.setOnCheckedChangeListener(this);
        }

        setSupportActionBar(binding.toolbar);

        // 앱 상단 툴바
        binding.calendarView.setSelectionMode(MaterialCalendarView.SELECTION_MODE_RANGE);
        binding.calendarView.setSelectedDate(CalendarDay.today());

        // 툴바의 사이드뷰
        ActionBarDrawerToggle dt = new ActionBarDrawerToggle(this, binding.layoutDrawer, binding.toolbar, R.string.app_name, R.string.app_name) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                binding.layoutMain.setTranslationX(slideOffset * drawerView.getWidth());
                binding.layoutMain.getLayoutParams().height = binding.layoutDrawer.getHeight() -
                        (int) (binding.layoutDrawer.getHeight() * slideOffset * 0.3f);
                ((DrawerLayout.LayoutParams) binding.layoutMain.getLayoutParams()).topMargin = (binding.layoutDrawer.getHeight() - binding.layoutMain.getLayoutParams().height) / 3;
                ((DrawerLayout.LayoutParams) binding.layoutMain.getLayoutParams()).bottomMargin = (binding.layoutDrawer.getHeight() - binding.layoutMain.getLayoutParams().height) / 3;
                binding.layoutDrawer.bringChildToFront(drawerView);
                binding.layoutDrawer.setScrimColor(Color.TRANSPARENT);
                binding.layoutDrawer.requestLayout();
            }
        };
        binding.layoutDrawer.addDrawerListener(dt);
        dt.syncState();

        slideAnimation = new SlideAnimation();

        /* 캘린터 높이 가져오기 위한 부분 */
        toolbarHeight = getToolbarHeight(getApplicationContext());
        calendarHeight = getViewHeight(binding.appbar) - toolbarHeight;

        //처음 로그인 이후 세션 열려있는경우 로그인 버튼을 누르지 않아도 다음 화면으로 이동
        if(Session.getCurrentSession().isOpened())
        {
            //Session.getCurrentSession().checkAndImplicitOpen();
            SessionManager sessionManger = new SessionManager(this);
            sessionManger.putSessionPreference();
            FragmentController.changeFragment(this
                    , new MapsFragment( preferenceManager.getString("email", null)));
        } else {
            FragmentController.initFragment(FragmentController.LOGIN_FRAGMENT
                    , MainActivity.this
                    , preferenceManager.getString("email", null));
        }
        binding.layoutDrawer.setDrawerListener(dt);
    }

    public static int getViewHeight(View view) {
        WindowManager wm =
                (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int deviceWidth;

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
            Point size = new Point();
            display.getSize(size);
            deviceWidth = size.x;
        } else {
            deviceWidth = display.getWidth();
        }

        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(deviceWidth, View.MeasureSpec.AT_MOST);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);
        return view.getMeasuredHeight();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.calendarItem:
                try {
                    visibleCalendar(binding.appbar);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                break;
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void visibleCalendar(View view) throws ExecutionException, InterruptedException, ParseException {
        if (calendarOpened) {
            calendarOpened = false;
            slideAnimation.slideView(view, calendarHeight + toolbarHeight, toolbarHeight);
            ((MapsFragment) getSupportFragmentManager()
                    .findFragmentByTag(MapsFragment.class.toString()))
                    .initMapInfo(false);
        } else {
            calendarOpened = true;
            slideAnimation.slideView(view, toolbarHeight, calendarHeight + toolbarHeight);
            ((MapsFragment) getSupportFragmentManager()
                    .findFragmentByTag(MapsFragment.class.toString())).clearMap();
        }
    }

    public ActivityMainBinding getBinding() {
        return binding;
    }

    public NavHeaderBinding getNavHeaderBinding() {
        return navHeaderBinding;
    }

    public int getToolbarHeight(Context context) {
        if(toolbarHeight == 0) {
            TypedValue tv = new TypedValue();
            if (context.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
                return TypedValue.complexToDimensionPixelSize(tv.data, context.getResources().getDisplayMetrics());
            }
        } else {
            return toolbarHeight;
        }
        return 0;
    }

    public void setCalendarOpened(boolean calendarOpened) {
        this.calendarOpened = calendarOpened;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        MDToast mdToast;
        // Handle navigation view item clicks here.
        switch (item.getItemId()) {
            case R.id.account:
                // DO your stuff
                mdToast = MDToast.makeText(this, "account", MDToast.LENGTH_SHORT, MDToast.TYPE_ERROR);
                mdToast.show();
                // first select resource
                shareToInstagramStory(R.drawable.badge2);
                break;
            case R.id.logout:
                dialogManager.openLogoutDialog(mainActivity);
                break;
        }
        return false;
    }

    // Navigation View Switch Clicked
    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        dialogManager.openLocationDialog(mainActivity, compoundButton, b);
    }

    public void shareToInstagramStory(int drawable) {
        Bitmap bitPictureBitmap = BitmapFactory.decodeResource(getResources(), drawable);
        SaveBitmapToFileCache(getResources().getResourceEntryName(drawable) + ".jpg", bitPictureBitmap, getFilesDir().getAbsolutePath());
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.instagram.android");
        if (intent != null) {
            File file = new File(getFilesDir().getAbsolutePath(), getResources().getResourceEntryName(drawable) + ".jpg");
            // Define image asset URI
            Uri backgroundAssetUri = FileProvider.getUriForFile(getApplicationContext(), getApplicationContext().getPackageName() + ".fileprovider", file);

            // Instantiate implicit intent with ADD_TO_STORY action and background asset
            intent = new Intent("com.instagram.share.ADD_TO_STORY");

            intent.setDataAndType(backgroundAssetUri, "image/jpeg");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Instantiate activity and verify it will resolve implicit intent
            if (getPackageManager().resolveActivity(intent, 0) != null) {
                startActivityForResult(intent, 0);
            }

            /*
            // Define image asset URI
            Uri stickerAssetUri = resIdToUri(getApplicationContext(), getResId("date2", R.drawable.class));
            String sourceApplication = "com.odengmin.handtracer";

            // Instantiate implicit intent with ADD_TO_STORY action,
            // sticker asset, and background colors
            intent = new Intent("com.instagram.share.ADD_TO_STORY");
            intent.putExtra("source_application", sourceApplication);

            intent.setType("image/*");
            intent.putExtra("interactive_asset_uri", stickerAssetUri);
            intent.putExtra("top_background_color", "#33FF33");
            intent.putExtra("bottom_background_color", "#FF00FF");

            // Instantiate activity and verify it will resolve implicit intent
            grantUriPermission(
                    "com.instagram.android", stickerAssetUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (getPackageManager().resolveActivity(intent, 0) != null) {
                startActivityForResult(intent, 0);
            }
            */
        } else {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("market://details?id=" + "com.instagram.android"));
            startActivity(intent);
        }
    }

    @SuppressLint({"SetWorldWritable", "SetWorldReadable"})
    private void SaveBitmapToFileCache(String name, Bitmap bitmap, String strFilePath) {
        File fileCacheItem = new File(strFilePath, name);
        fileCacheItem.setReadable(true, false);
        fileCacheItem.setWritable(true, false);
        OutputStream out = null;
        try {
            fileCacheItem.createNewFile();
            out = new FileOutputStream(fileCacheItem);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startKakaoLogin(View view) {
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.getSession().open(AuthType.KAKAO_LOGIN_ALL, mainActivity);
    }
}