package com.technosales.mobitrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Created by dlohani on 11/25/15.
 */
public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static boolean isValidMobile(String phone) {
        boolean check = false;
        if (!Pattern.matches("[a-zA-Z]+", phone)) {
            if (phone.length() != 10) {
                check = false;
            } else {
                if (phone.charAt(0) != '9' || (phone.charAt(1) != '8' && phone.charAt(1) != '7'))
                    check = false;
                else
                    check = true;
            }
        } else {
            check = false;
        }
        return check;
    }

    public static void scheduling(boolean toSchedule, Context context) {
        String[] projection = {"_id", "day", "startHour", "startMinute", "stopHour", "stopMinute"};
        Cursor c = context.getContentResolver().query(MobitrackContentProvider.SCHEDULES_URI, projection, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Schedule schedule = Schedule.fromCursor(c);
                    setScheduleAlarms(schedule, toSchedule, context);

                } while (c.moveToNext());
            }
            c.close();
        }
    }

    public static void setScheduleAlarmForId(long id, Context context) {
        String[] projection = {"_id", "day", "startHour", "startMinute", "stopHour", "stopMinute"};
        Uri uri = ContentUris.withAppendedId(MobitrackContentProvider.SCHEDULES_URI, id);
        Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
        if (c != null) {
            c.moveToFirst();
            Schedule schedule = Schedule.fromCursor(c);
            setScheduleAlarms(schedule, true, context);
            c.close();
        }
    }

    private static void setScheduleAlarms(Schedule schedule, boolean toSchedule, Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long interval = AlarmManager.INTERVAL_DAY * 7;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_START_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) schedule.getId(), intent, 0);

        Calendar now = Calendar.getInstance();
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(System.currentTimeMillis());
        startCal.set(Calendar.HOUR_OF_DAY, schedule.getStartHour());
        startCal.set(Calendar.MINUTE, schedule.getStartMinute());
        startCal.set(Calendar.DAY_OF_WEEK, schedule.getDay());
        long startTimeMillis = startCal.getTimeInMillis();

        Calendar stopCal = Calendar.getInstance();
        stopCal.set(Calendar.HOUR_OF_DAY, schedule.getStopHour());
        stopCal.set(Calendar.MINUTE, schedule.getStopMinute());
        stopCal.set(Calendar.DAY_OF_WEEK, schedule.getDay());
        long stopTimeMillis = stopCal.getTimeInMillis();
        if (toSchedule) {
            if (stopTimeMillis <= now.getTimeInMillis()) {
                startTimeMillis += AlarmManager.INTERVAL_DAY * 7;
            }
            Log.d(TAG, "Start alarm set at " + startCal.getTime().toString());
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startTimeMillis, interval, pendingIntent);
        } else {
            Log.d(TAG, "Start alarm cancelled");
            alarmManager.cancel(pendingIntent);
        }

        intent.setAction(AlarmReceiver.ACTION_STOP_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(context, (int) schedule.getId(), intent, 0);

        if (toSchedule) {
            Log.d(TAG, "Stop alarm set at " + stopCal.getTime().toString());
            if (stopTimeMillis <= now.getTimeInMillis()) {
                stopTimeMillis += AlarmManager.INTERVAL_DAY * 7;
            }
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, stopTimeMillis, interval, pendingIntent);
        } else {
            Log.d(TAG, "Stop alarm cancelled");
            alarmManager.cancel(pendingIntent);
        }
    }
}
