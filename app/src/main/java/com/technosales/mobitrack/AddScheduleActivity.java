package com.technosales.mobitrack;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by dlohani on 11/23/15.
 */
public class AddScheduleActivity extends Activity implements TimePickerDialog.OnTimeSetListener {

    public static final String ARG_DAY = "day";
    private static final String TAG = AddScheduleActivity.class.getSimpleName();
    private static final int TIME_TYPE_START = 1;
    private static final int TIME_TYPE_STOP = 2;
    LinearLayout llDays, llStart, llStop;
    private int startHour, startMinute, stopHour, stopMinute, day = 0;
    private int timeType;
    private List<Integer> days = new ArrayList<>();
    private TextView tvDays, tvStart, tvStop;
    private Button btnSave;
    private String[] dayNames = new String[]{"SUNDAY", "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"};
    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.add_schedule);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            day = args.getInt(ARG_DAY, 0);
        }
        initialize();

    }

    private void initialize() {
        llDays = (LinearLayout) findViewById(R.id.llDays);
        llStart = (LinearLayout) findViewById(R.id.llStart);
        llStop = (LinearLayout) findViewById(R.id.llStop);
        tvStart = (TextView) findViewById(R.id.tvStart);
        tvStop = (TextView) findViewById(R.id.tvStop);
        tvDays = (TextView) findViewById(R.id.tvDays);
        llDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDaysChooser();
            }
        });

        days.add(day);

        displayDays();


        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        startHour = c.get(Calendar.HOUR_OF_DAY);
        startMinute = c.get(Calendar.MINUTE);
        stopHour = startHour + 1;
        stopMinute = startMinute;


        setTime(startHour, startMinute, tvStart);
        setTime(stopHour, stopMinute, tvStop);

        llStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = TIME_TYPE_START;
                showTimeDialog(startHour, startMinute);
            }
        });
        llStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = TIME_TYPE_STOP;
                showTimeDialog(stopHour, stopMinute);
            }
        });
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (days.size() < 1) {
                    Toast.makeText(getApplicationContext(), "Cannot save, please select at least one day.", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (int curDay : days) {
                    final Schedule schedule = new Schedule(curDay + 1, startHour, startMinute, stopHour, stopMinute);
                    ContentValues values = new ContentValues();
                    values.put("day", schedule.getDay());
                    values.put("startHour", schedule.getStartHour());
                    values.put("startMinute", schedule.getStartMinute());
                    values.put("stopHour", schedule.getStopHour());
                    values.put("stopMinute", schedule.getStopMinute());
                    Uri inserted = getContentResolver().insert(MobitrackContentProvider.SCHEDULES_URI, values);
                    if (inserted != null) {
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_success_save_schedule), Toast.LENGTH_SHORT).show();
                        setIntentForSchedule((int) ContentUris.parseId(inserted), curDay + 1);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.msg_failed_save_schedule), Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
                finish();
            }
        });
    }

    private void showDaysChooser() {
        final List<Integer> selected = days;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose days to set the schedule for")
                .setMultiChoiceItems(R.array.days, getCheckedItems(), new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            selected.add(which);
                        } else if (days.contains(which)) {
                            // Else, if the item is already in the array, remove it
                            selected.remove(Integer.valueOf(which));
                        }

                    }
                })
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        days = selected;
                        displayDays();
                    }
                });
        builder.create().show();
    }

    private boolean[] getCheckedItems() {
        boolean[] checkedItems = new boolean[7];
        for (int day : days)
            checkedItems[day] = true;
        return checkedItems;
    }

    private void displayDays() {
        if (days.size() < 1) {
            tvDays.setText("Please select at least one day");
            return;
        }
        String dayCaption = "";
        for (int day : days) {
            dayCaption += dayNames[day] + ", ";
        }
        dayCaption = dayCaption.substring(0, dayCaption.length() - 2);
        tvDays.setText(dayCaption);
    }

    private void setIntentForSchedule(int id, int curDay) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_START_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        long interval = AlarmManager.INTERVAL_DAY * 7;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, startHour);
        c.set(Calendar.MINUTE, startMinute);
        c.set(Calendar.DAY_OF_WEEK, curDay);
        Log.d(TAG, "Start time: " + c.getTime().toString());
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), interval, pendingIntent);

        intent.setAction(AlarmReceiver.ACTION_STOP_SERVICE);
        pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        c.set(Calendar.HOUR_OF_DAY, stopHour);
        c.set(Calendar.MINUTE, stopMinute);
        Log.d(TAG, "Stop time: " + c.getTime().toString());
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), interval, pendingIntent);
    }

    private void showTimeDialog(int hour, int minute) {
        TimePickerDialog dialog = new TimePickerDialog(this, this, hour, minute, false);
        dialog.show();
    }

    private void setTime(int hour, int minute, TextView view) {
        String cap = hour + " : " + minute;
        view.setText(cap);
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        switch (timeType) {
            case TIME_TYPE_START:
                startHour = hourOfDay;
                startMinute = minute;
                setTime(startHour, startMinute, tvStart);
                break;
            case TIME_TYPE_STOP:
                stopHour = hourOfDay;
                stopMinute = minute;
                setTime(stopHour, stopMinute, tvStop);
                break;
        }
    }
}
