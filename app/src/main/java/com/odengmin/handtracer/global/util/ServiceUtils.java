package com.odengmin.handtracer.global.util;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.odengmin.handtracer.R;

import java.text.DateFormat;
import java.util.Date;

public class ServiceUtils {
    static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_locaction_updates";
    public static final String WASH_HANDS_ACTION = "com.odengmin.handtracer.global.service.wash";
    public static final String INDOOR_ACTION = "com.odengmin.handtracer.global.service.indoor";
    public static final String RESTART_SERVICE_ACTION = "com.odengmin.handtracer.global.service.restart";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    public static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    public static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    public static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    public static String getLocationTitle(Context context) {
        return context.getString(R.string.location_updated,
                DateFormat.getDateTimeInstance().format(new Date()));
    }
}
