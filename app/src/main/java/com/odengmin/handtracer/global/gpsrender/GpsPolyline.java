package com.odengmin.handtracer.global.gpsrender;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.odengmin.handtracer.database.local.entity.GpsEntity;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GpsPolyline {
    GpsArrowhead gpsArrowhead;
    GoogleMap mMap;

    public GpsPolyline(GpsArrowhead gpsArrowhead, GoogleMap mMap) {
        this.gpsArrowhead = gpsArrowhead;
        this.mMap = mMap;
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void drawPolyLines(List<List<GpsEntity>> gpsEntities) throws ParseException {
        for(List<GpsEntity> gpsEntity : gpsEntities) {
            for (int z = 0; z < gpsEntity.size() - 1; z++) {
                LatLng src = new LatLng(Double.parseDouble(gpsEntity.get(z).latitude), Double.parseDouble(gpsEntity.get(z).longitude));
                LatLng dest = new LatLng(Double.parseDouble(gpsEntity.get(z + 1).latitude), Double.parseDouble(gpsEntity.get(z + 1).longitude));
                final DateTimeFormatter OLD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                final DateTimeFormatter NEW_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

                // yyyy-MM-dd HH:mm:ss -> yyyyMMdd
                String oldString = gpsEntity.get(z).created_at;
                LocalDate localDate = LocalDate.parse(oldString, OLD_FORMATTER);
                String newString = localDate.format(NEW_FORMATTER);

                // yyyyMMdd -> day
                @SuppressLint("SimpleDateFormat") final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                Date date = dateFormat.parse(newString);
                Calendar calendar = Calendar.getInstance();
                assert date != null;
                calendar.setTime(date);

                //set Polyline color
                List<Integer> colors = List.of(
                        Color.parseColor("#FFFF0000"),
                        Color.parseColor("#FFFF7F00"),
                        Color.parseColor("#FFFFFF00"),
                        Color.parseColor("#FF00FF00"),
                        Color.parseColor("#FF0000FF"),
                        Color.parseColor("#FF4B0082"),
                        Color.parseColor("#FF8F00FF"));

                //draw Polyline
                mMap.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude))
                        .width(25).color(colors.get(calendar.get(Calendar.DAY_OF_WEEK) - 1)).geodesic(true));

            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    public void drawPolyLine(GpsEntity src, GpsEntity dest) throws ParseException {
        final DateTimeFormatter OLD_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        final DateTimeFormatter NEW_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

        // yyyy-MM-dd HH:mm:ss -> yyyyMMdd
        String oldString = dest.created_at;
        LocalDate localDate = LocalDate.parse(oldString, OLD_FORMATTER);
        String newString = localDate.format(NEW_FORMATTER);

        // yyyyMMdd -> day
        @SuppressLint("SimpleDateFormat") final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = dateFormat.parse(newString);
        Calendar calendar = Calendar.getInstance();
        assert date != null;
        calendar.setTime(date);

        //set Polyline color
        List<Integer> colors = List.of(
                Color.parseColor("#FFFF0000"),
                Color.parseColor("#FFFF7F00"),
                Color.parseColor("#FFFFFF00"),
                Color.parseColor("#FF00FF00"),
                Color.parseColor("#FF0000FF"),
                Color.parseColor("#FF4B0082"),
                Color.parseColor("#FF8F00FF"));

        //draw Polyline
        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(Double.parseDouble(src.latitude), Double.parseDouble(src.longitude)),
                        new LatLng(Double.parseDouble(dest.latitude), Double.parseDouble(dest.longitude)))
                .width(25).color(colors.get(calendar.get(Calendar.DAY_OF_WEEK) - 1)).geodesic(true));
    }
}
