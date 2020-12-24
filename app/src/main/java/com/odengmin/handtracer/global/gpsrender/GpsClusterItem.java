package com.odengmin.handtracer.global.gpsrender;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class GpsClusterItem implements ClusterItem {
    private LatLng location;
    private String address;
    private String snippet;

    public GpsClusterItem(LatLng location, String address, String snippet) {
        this.location = location;
        this.address = address;
        this.snippet = snippet;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return location;
    }

    @Nullable
    @Override
    public String getTitle() {
        return address;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }
}
