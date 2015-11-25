package com.technosales.mobitrack;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String[] projection = {"_id", "day", "startHour", "startMinute", "stopHour", "stopMinute"};
        Cursor c = context.getContentResolver().query(MobitrackContentProvider.SCHEDULES_URI, projection, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    Schedule schedule = Schedule.fromCursor(c);
                    Calendar calendar = Calendar.getInstance();
                    long interval = AlarmManager.INTERVAL_DAY * 7;

                    Intent intent = new Intent(context, AlarmReceiver.class);
                    intent.setAction(AlarmReceiver.ACTION_START_SERVICE);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) schedule.getId(), intent, 0);

                    Calendar now = Calendar.getInstance();
                    long initialTime;
                    if (toSchedule) {
                        calendar.setTimeInMillis(System.currentTimeMillis());
                        calendar.set(Calendar.HOUR_OF_DAY, schedule.getStartHour());
                        calendar.set(Calendar.MINUTE, schedule.getStartMinute());
                        calendar.set(Calendar.DAY_OF_WEEK, schedule.getDay());
                        initialTime = calendar.getTimeInMillis();
                        if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
                            initialTime += AlarmManager.INTERVAL_DAY * 7;
                        }
                        Log.d(TAG, "Start alarm set at " + calendar.getTime().toString());
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, initialTime, interval, pendingIntent);
                    } else {
                        Log.d(TAG, "Start alarm cancelled");
                        alarmManager.cancel(pendingIntent);
                    }

                    intent.setAction(AlarmReceiver.ACTION_STOP_SERVICE);
                    pendingIntent = PendingIntent.getBroadcast(context, (int) schedule.getId(), intent, 0);

                    if (toSchedule) {
                        calendar.set(Calendar.HOUR_OF_DAY, schedule.getStopHour());
                        calendar.set(Calendar.MINUTE, schedule.getStopMinute());
                        Log.d(TAG, "Stop alarm set at " + calendar.getTime().toString());
                        initialTime = calendar.getTimeInMillis();
                        if (calendar.getTimeInMillis() <= now.getTimeInMillis()) {
                            initialTime += AlarmManager.INTERVAL_DAY * 7;
                        }
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, initialTime, interval, pendingIntent);
                    } else {
                        Log.d(TAG, "Stop alarm cancelled");
                        alarmManager.cancel(pendingIntent);
                    }

                } while (c.moveToNext());
            }
        }
    }
}
