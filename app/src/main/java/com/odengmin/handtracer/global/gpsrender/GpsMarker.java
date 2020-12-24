package com.odengmin.handtracer.global.gpsrender;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.odengmin.handtracer.database.local.entity.GpsEntity;

import java.util.ArrayList;
import java.util.List;

public class  GpsMarker {
    public static void drawMarker(ClusterManager<GpsClusterItem> clusterManager, List<List<GpsEntity>> gpsEntities) {
        //List<GpsEntity> gpsEntity = new ArrayList<GpsEntity>();
        for(List<GpsEntity> lgps : gpsEntities) {
            for(GpsEntity gps : lgps) {
                LatLng latLng = new LatLng(Double.parseDouble(gps.latitude), Double.parseDouble(gps.longitude));
                clusterManager.addItem(new GpsClusterItem(latLng, gps.getDate(), gps.getTime()));
            }
        }
    }
}
