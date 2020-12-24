package com.odengmin.handtracer.fragment;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.odengmin.handtracer.R;
import com.odengmin.handtracer.activity.MainActivity;
import com.odengmin.handtracer.database.local.entity.GpsEntity;
import com.odengmin.handtracer.databinding.ActivityMainBinding;
import com.odengmin.handtracer.databinding.FragmentMapsBinding;
import com.odengmin.handtracer.global.gpsrender.GpsArrowhead;
import com.odengmin.handtracer.global.gpsrender.GpsClusterItem;
import com.odengmin.handtracer.global.gpsrender.GpsMarker;
import com.odengmin.handtracer.global.gpsrender.GpsPolyline;
import com.odengmin.handtracer.global.service.LocationUpdateService;
import com.odengmin.handtracer.global.view.GpsViewModel;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.valdesekamdem.library.mdtoast.MDToast;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MapsFragment extends Fragment implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener,
        GoogleMap.OnMyLocationButtonClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private FragmentMapsBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng currentLocation;
    private Intent mServiceIntent;
    private GpsViewModel gpsViewModel;
    private ActivityMainBinding mainBinding;
    private String email;
    private List<List<GpsEntity>> gpsEntities;
    private List<GpsEntity> gpsEntity;
    public ClusterManager<GpsClusterItem> getClusterManager() {
        return clusterManager;
    }
    private ClusterManager<GpsClusterItem> clusterManager;
    public MapsFragment(String email) {
        this.email = email;
    }
    private Messenger mServiceMessenger = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d("test","onServiceConnected");
            mServiceMessenger = new Messenger(iBinder);
            try {
                Message msg = Message.obtain(null, LocationUpdateService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mServiceMessenger.send(msg);
            }
            catch (RemoteException e) {
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };

    /** Service 로 부터 message를 받음 */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.i("test","act : what " + msg.what);
            switch (msg.what) {
                case LocationUpdateService.MSG_SEND_TO_ACTIVITY:
                    GpsEntity src = null;
                    if(gpsViewModel != null) {
                        try {
                            src = gpsViewModel.getLastGps();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    String latitude = msg.getData().getString("latitude");
                    String longtitude = msg.getData().getString("longtitude");
                    GpsEntity dest = new GpsEntity(latitude, longtitude);
                    clusterManager.addItem(new GpsClusterItem(new LatLng(Double.parseDouble(latitude), Double.parseDouble(longtitude)), dest.getDate(), dest.getTime()));
                    if(src != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            try {
                                new GpsPolyline(new GpsArrowhead(getActivity()), mMap)
                                        .drawPolyLine(src, dest);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    showCurrentLocation();
                    break;
            }
            return false;
        }
    }));


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_maps, container, false);
        View root = binding.getRoot();


        binding.map.onCreate(savedInstanceState);
        binding.map.getMapAsync(this);

        mainBinding = ((MainActivity) getActivity()).getBinding();
        mainBinding.appbar.getLayoutParams().height = ((MainActivity) getActivity()).getToolbarHeight(getContext());
        mainBinding.appbar.setVisibility(View.VISIBLE);

        return root;
    }

    public void clearMap() {
        mMap.clear();
    }

    @Override
    public void onResume() {
        binding.map.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mainBinding.appbar.setVisibility(View.GONE);
        ((MainActivity) getActivity()).setCalendarOpened(false);
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        binding.map.onLowMemory();
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        MapsInitializer.initialize(getActivity());

        mMap.setOnMyLocationClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);

        enablePermissions();


        try {
            initMapInfo(true);
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mMap.getUiSettings().setRotateGesturesEnabled(false); // Arrow Head의 올바른 방향을 위한 Map 로테이션 방지
    }

    private void enablePermissions() {

        if (isNoPermissions()) {
            requestLocationPermission();
        } else {
            mMap.setMyLocationEnabled(true);

            mServiceIntent = new Intent(getActivity(), LocationUpdateService.class);
            mServiceIntent.putExtra("email", email);
            getActivity().startService(mServiceIntent);
            getActivity().bindService(mServiceIntent, mConnection, Context.BIND_AUTO_CREATE);

            // 시작하자마자 내 위치로 줌 땡겨
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
            mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
            });
        }
    }

    private boolean isNoPermissions() {
//        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                || ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED;
        return ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
//        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }


    // requestPermissions 호출되면 콜백 함수로 호출됨
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        enablePermissions();
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void initMapInfo(boolean isToday) throws ExecutionException, InterruptedException, ParseException {
        clearMap();

        gpsEntities = new ArrayList<List<GpsEntity>>();
        gpsEntity = new ArrayList<GpsEntity>();

        clusterManager = new ClusterManager<>(getContext(), mMap);
        mMap.setOnCameraIdleListener(clusterManager);
        mMap.setOnMarkerClickListener(clusterManager);

        gpsViewModel = new GpsViewModel(getActivity().getApplication());

        if (isToday) {
            gpsEntities.add(gpsViewModel.getDateGps(new SimpleDateFormat("yyyy-MM-dd"
                    , Locale.getDefault()).format(new Date())));
        } else {
            for (CalendarDay cd : ((MainActivity) getActivity()).getBinding().calendarView.getSelectedDates()) {
                gpsEntities.add(gpsViewModel.getDateGps(cd.getYear()
                        + "-" + getDatePadding(cd.getMonth() + 1)
                        + "-" + getDatePadding(cd.getDay())));
            }
        }

        GpsMarker.drawMarker(clusterManager, gpsEntities);
        new GpsPolyline(new GpsArrowhead(getActivity()), mMap)
                .drawPolyLines(gpsEntities);
        showCurrentLocation();
    }

    private void showCurrentLocation() {
        mFusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18.0f));
        });
    }

    public String getDatePadding(int date) {
        if (date > 9) {
            return Integer.toString(date);
        } else {
            return String.format("0%d", date);
        }
    }


}