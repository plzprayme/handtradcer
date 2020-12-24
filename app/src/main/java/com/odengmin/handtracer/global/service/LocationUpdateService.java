package com.odengmin.handtracer.global.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.database.local.entity.GpsEntity;
import com.odengmin.handtracer.global.gpsrender.GpsClusterItem;
import com.odengmin.handtracer.global.manager.PreferenceManager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.odengmin.handtracer.R;
import com.odengmin.handtracer.global.receiver.ActionReceiver;
import com.odengmin.handtracer.global.util.ServiceUtils;
import com.odengmin.handtracer.global.view.GpsViewModel;


import java.util.Arrays;

public class LocationUpdateService extends Service {

    // Location Var
    private LocationManager locationManager;

    //    private final Long INTERVAL = Long.valueOf(1000000); // 밀리세컨드
    private final Long INTERVAL = Long.valueOf(3600000); // 밀리세컨드

    // Notification Var
    private static final String FOREGROUND_CHANNEL_ID = "com.odengmin.handtracer.global.service.foreground";
    private static final String PUSH_CHANNEL_ID = "com.odengmin.handtracer.global.service.push";

    private GpsViewModel gpsViewModel;

    private String email;

    private boolean isGpsReceived = false;
    private boolean isIndoor = false;

    private PreferenceManager preferenceManager;

    private ClusterManager<GpsClusterItem> clusterManager;

    public static final int MSG_REGISTER_CLIENT = 1;
    //public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_SEND_TO_SERVICE = 3;
    public static final int MSG_SEND_TO_ACTIVITY = 4;
    private Messenger mClient = null;   // Activity 에서 가져온 Messenger


    LocationListener gpsListener = location -> {
        isGpsReceived = true;
        updateWithNewLocation(location);
    };


    private Looper serviceLooper;
    private ServiceHandler serviceHandler;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("LOCATION TRACKING", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);
        serviceHandler.post(() -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                createNotificationChannel();
                createLocationService();
                createDatebase();
                createForegroundService();
            } else {
                startForeground(2, new Notification());
            }
        });
    }

    /** activity로부터 binding 된 Messenger */
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.w("test","test : ControlService - message what : "+msg.what +" , msg.obj "+ msg.obj);
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    mClient = msg.replyTo;  // activity로부터 가져온
                    break;
            }
            return false;
        }
    }));

    private void sendMsgToActivity(String latitude, String longtitude) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString("latitude", latitude);
            bundle.putString("longtitude", longtitude);
            Message msg = Message.obtain(null, MSG_SEND_TO_ACTIVITY);
            msg.setData(bundle);
            mClient.send(msg);      // msg 보내기
        } catch (RemoteException e) {
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence name = getString(R.string.foreground_channel);
        String description = getString(R.string.foreground_desc);
        NotificationChannel foregroundChannel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, NotificationManager.IMPORTANCE_MIN);
        foregroundChannel.setDescription(description);


        name = getString(R.string.push_channel);
        description = getString(R.string.push_desc);
        NotificationChannel pushChannel = new NotificationChannel(PUSH_CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
        pushChannel.setDescription(description);
        pushChannel.enableVibration(true);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannels(Arrays.asList(foregroundChannel, pushChannel));
    }

    private void createForegroundService() {
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.hand_tracer_logo);

        Notification notification = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
                .setContentTitle("손자취가 실행되고 있습니다!")
                .setContentText("사용자의 동선을 파악하고 있습니다!")
                .setSmallIcon(R.drawable.hand_tracer_logo) // ContentTitle 위에 뜨는 아이콘
                .setLargeIcon(bm)  // 맨 오른쪽에 뜨는 아이콘 나중에 추가하자
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();

        startForeground(1, notification);
    }

    private void createDatebase() {
        gpsViewModel = new GpsViewModel(getApplication());
    }

    private void createLocationService() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        isGpsReceived = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isGpsReceived) { // 기록 기준 수정 코드
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, gpsListener);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updateWithNewLocation(Location location) {
        if (!isGpsReceived) {
            return;
        }

        // 디버깅 코드
        String bd = bundleToString(location.getExtras());
        // 데이터베이스에 저장할 코드드
        String latitude = location.getLatitude() + "";
        String longtitude = location.getLongitude() + "";

        float ac = location.getAccuracy();
        int st = location.getExtras().getInt("satellites");
        int meanCnO = location.getExtras().getInt("meanCn0");

//        if (st < 20 || ac > 3.0f) {
        if (meanCnO < 30) {
            if (isIndoor) {
                Toast.makeText(this, "이미실내: " + bd, Toast.LENGTH_SHORT).show();
                return;
            }

            // 푸쉬 발생
            NotificationManagerCompat.from(this)
                    .notify(getNotificationID(), createPushNotification());
            if(preferenceManager.getBoolean("locationSwitch", true)) {
                gpsViewModel.insert(new GpsEntity(latitude, longtitude));
                sendMsgToActivity(latitude, longtitude);
            }

            // 최초 실내 입장
            Toast.makeText(this, "실내: " + bd, Toast.LENGTH_SHORT).show();
            isIndoor = true;
            return;
        }


        // 실외일 때
        if(preferenceManager.getBoolean("locationSwitch", true)) {
            gpsViewModel.insert(new GpsEntity(latitude, longtitude));
            sendMsgToActivity(latitude, longtitude);
        }
        Toast.makeText(this, "실외: " + bd, Toast.LENGTH_SHORT).show();
        isIndoor = false;
    }

    private String bundleToString(Bundle bundle) {
        StringBuilder bundleString = new StringBuilder("Bundle[");
        for (String key : bundle.keySet()) {
            bundleString.append(key);
            bundleString.append(": ");
            bundleString.append(bundle.get(key));
            bundleString.append(", ");
        }
        bundleString.append("]");

        return bundleString.toString();
    }

    private int getNotificationID() {
        return (int) System.currentTimeMillis() / 1000;
    }

    private Notification createPushNotification() {
        // 인텐트 생성
        Intent actionIntent = new Intent(this, ActionReceiver.class);
        actionIntent.setAction(ServiceUtils.WASH_HANDS_ACTION);
        PendingIntent checkWash = PendingIntent.getBroadcast(this, 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

//        actionIntent.setAction(ServiceUtils.INDOOR_ACTION);
        actionIntent = new Intent(this, MainActivity.class);
        PendingIntent backToMainActivity = PendingIntent.getActivity(this, 1, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, PUSH_CHANNEL_ID)
                .setSmallIcon(R.drawable.kakaostory_icon)
                .setContentTitle(getString(R.string.push_title1))
                .setContentText(getString(R.string.push_content1))
                .setCategory(Notification.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.hand_tracer_logo) // ContentTitle 위에 뜨는 아이콘
                .setContentIntent(backToMainActivity) // 클리 시 액티비티로 이동
                .addAction(R.drawable.btn_x, "손 씻었어요!", checkWash) // 탭 + 손 씻었는지 체크
                .setAutoCancel(true) // 유저가 클릭 시 자동으로 없애줌
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, ActionReceiver.class);
        broadcastIntent.setAction(ServiceUtils.RESTART_SERVICE_ACTION);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            email = intent.getStringExtra("email");
        }
        preferenceManager = new PreferenceManager(getApplicationContext());
        super.onStartCommand(intent, flags, startId);


        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
