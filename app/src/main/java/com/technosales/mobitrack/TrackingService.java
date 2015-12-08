/*
 * Copyright 2012 - 2015 Anton Tananaev (anton.tananaev@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.technosales.mobitrack;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TrackingService extends Service implements SensorEventListener {

    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 1;

    private TrackingController trackingController;

    private SensorManager sensorManager;
    private Sensor mAccelerationSensor;
    private long lastSensorUpdate = 0;
    private float[] mGravity = new float[3];
    private float mAccelCurrent = SensorManager.GRAVITY_EARTH;
    private float mAccelLast = SensorManager.GRAVITY_EARTH;
    private float mAccel = 0.00f;
    private boolean trackingStopped = false;

    @SuppressWarnings("deprecation")
    private static Notification createNotification(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Notification notification = new Notification(android.R.drawable.stat_notify_sync_noanim, null, 0);
        try {
            Method method = notification.getClass().getMethod("setLatestEventInfo", Context.class, CharSequence.class, CharSequence.class, PendingIntent.class);
            try {
                method.invoke(notification, context, context.getString(R.string.app_name), context.getString(R.string.settings_status_on_summary), pendingIntent);
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
                Log.w(TAG, e);
            }
        } catch (SecurityException | NoSuchMethodException e) {
            Log.w(TAG, e);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            notification.priority = Notification.PRIORITY_MIN;
        }

        return notification;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "service create");
        StatusActivity.addMessage(getString(R.string.status_service_create));

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerationSensor != null) {
            sensorManager.registerListener(this, mAccelerationSensor, SensorManager.SENSOR_DELAY_NORMAL);
            lastSensorUpdate = System.currentTimeMillis();
        } else
            Log.d(TAG, "Sorry, Accelerometer not found.");

        trackingController = new TrackingController(this);
        trackingController.start();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            startForeground(NOTIFICATION_ID, createNotification(this));
            startService(new Intent(this, HideNotificationService.class));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        if (intent != null) {
            AutostartReceiver.completeWakefulIntent(intent);
        }
    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        onStart(intent, startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "service destroy");
        StatusActivity.addMessage(getString(R.string.status_service_destroy));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            stopForeground(true);
        }

        if (trackingController != null && !trackingStopped) {
            trackingController.stop();
        }
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            mGravity = event.values.clone();
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if (mAccel > 1) {
                // do something
                lastSensorUpdate = System.currentTimeMillis();
                if (trackingStopped) {
                    StatusActivity.addMessage("Device moved, starting location tracking");
                    Log.d(TAG, "Device moved");
                    trackingController.start();
                    trackingStopped = false;
                }
            } else {
                if ((System.currentTimeMillis() - lastSensorUpdate) > 30000) {
                    if (trackingController != null && !trackingStopped) {
                        StatusActivity.addMessage("Device was motionless for 30 seconds, stopping location tracking");
                        Log.d(TAG, "device not moving for 30 seconds");
                        trackingController.stop();
                        trackingStopped = true;
                    }
                }
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @TargetApi(Build.VERSION_CODES.ECLAIR)
    public static class HideNotificationService extends Service {
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onCreate() {
            startForeground(NOTIFICATION_ID, createNotification(this));
            stopForeground(true);
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            stopSelfResult(startId);
            return START_NOT_STICKY;
        }
    }

}
