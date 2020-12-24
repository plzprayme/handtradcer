package com.odengmin.handtracer.global.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.odengmin.handtracer.activity.WashHandActivity;
import com.odengmin.handtracer.global.service.LocationUpdateService;
import com.odengmin.handtracer.global.util.ServiceUtils;

public class ActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Broadcast Listened", intent.getAction());

        switch (intent.getAction()) {
//            case ServiceUtils.INDOOR_ACTION:
//                inndorAction(context);
//                break;

            case ServiceUtils.WASH_HANDS_ACTION:
                replaceToWashHandFragment(context);
                break;
            case ServiceUtils.RESTART_SERVICE_ACTION:
                restartService(context);
                break;
        }
    }

    private void restartService(Context context) {
        Log.d("Broadcast Listened", "RESTART SERVICE");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, LocationUpdateService.class));
        } else {
            context.startService(new Intent(context, LocationUpdateService.class));
        }
    }

    private void replaceToWashHandFragment(Context context) {
        Toast.makeText(context, "WASH HAND", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, WashHandActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
//        Log.d("Broadcast Listened", "CALL WASH HAND ACTIVITY");
    }

//    private void inndorAction(Context context) {
//        Toast.makeText(context, "INDOOR", Toast.LENGTH_SHORT).show();
////        Log.d("Broadcast Listened", "INDOOR ACTION");
//    }
}
