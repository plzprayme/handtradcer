package com.odengmin.handtracer.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.odengmin.handtracer.BuildConfig;
import com.odengmin.handtracer.R;
import com.odengmin.handtracer.database.local.entity.WashCount;
import com.odengmin.handtracer.global.view.GpsViewModel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;

import lombok.SneakyThrows;
import okhttp3.MediaType;


public class WashHandActivity extends AppCompatActivity {

    private static final String WASH_COUNT = "%d회";

    private GpsViewModel gpsViewModel = new GpsViewModel(getApplication());
    private String TODAY = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    private int maxBadge;

    private static final String ON_CREATE_FLOW = "AFTER_CRETAED";

    @SneakyThrows
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wash_hand);
        WashCount entity = gpsViewModel.startCountLogic(TODAY);


        int washCount = entity.getWashCount();
        selectBadgeByCount(washCount);

        getIntent().putExtra("FLOW", ON_CREATE_FLOW);

        Button btnShareStory = findViewById(R.id.post_story);
        btnShareStory.setOnClickListener(view -> {
            shareToInstagramStory(maxBadge);
        });
    }

    @SneakyThrows
    @Override
    protected void onResume() {
        super.onResume();

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            WashCount entity = gpsViewModel.startCountLogic(TODAY);

            int washCount = entity.getWashCount();
            selectBadgeByCount(washCount);
        }
    }

    private void selectBadgeByCount(int washCount) {
        TextView tvWashCount = (TextView) findViewById(R.id.count_num);
        tvWashCount.setText(String.format(WASH_COUNT, washCount));
        System.out.println();

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        ratingBar.setRating(washCount);

        if (washCount > 0) {
            ImageView badge1 = (ImageView) findViewById(R.id.badge1);
            badge1.setImageResource(R.drawable.badge1);
            maxBadge = R.drawable.badge1;
            if (washCount >= 5) {
                ImageView badge2 = (ImageView) findViewById(R.id.badge2);
                badge2.setImageResource(R.drawable.badge2);
                maxBadge = R.drawable.badge2;
                if (washCount >= 10) {
                    ImageView badge3 = (ImageView) findViewById(R.id.badge3);
                    badge3.setImageResource(R.drawable.badge3);
                    maxBadge = R.drawable.badge3;
                }
            }
        }
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
}
