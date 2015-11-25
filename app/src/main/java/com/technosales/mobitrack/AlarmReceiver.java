package com.technosales.mobitrack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_START_SERVICE = "com.technosales.mobitrack.ACTION_START_SERVICE";
    public static final String ACTION_STOP_SERVICE = "com.technosales.mobitrack.ACTION_STOP_SERVICE";
    private static final String TAG = AlarmReceiver.class.getSimpleName();

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        switch (intent.getAction()) {
            case ACTION_START_SERVICE:
                Log.d(TAG, "Start service alarm received");
                if (Utils.isValidMobile(preferences.getString(MainActivity.KEY_DEVICE, ""))) {
                    preferences.edit().putBoolean(MainActivity.KEY_STATUS, true).commit();
                    context.startService(new Intent(context, TrackingService.class));
                } else {
                    Log.d(TAG, "failed to start service, invalid mobile number");
                }
                break;
            case ACTION_STOP_SERVICE:
                Log.d(TAG, "Stop service alarm received");
                preferences.edit().putBoolean(MainActivity.KEY_STATUS, false).commit();
                context.stopService(new Intent(context, TrackingService.class));
                break;
        }
    }
}
