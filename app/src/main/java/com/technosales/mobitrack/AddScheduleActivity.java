package com.technosales.mobitrack;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by dlohani on 11/23/15.
 */
public class AddScheduleActivity extends Activity implements TimePickerDialog.OnTimeSetListener {

    public static final String ARG_DAY = "day";
    private static final String TAG = AddScheduleActivity.class.getSimpleName();
    private static final int TIME_TYPE_START = 1;
    private static final int TIME_TYPE_STOP = 2;
    private int startHour, startMinute, stopHour, stopMinute, day = 1;
    private int timeType;
    private ImageButton ibStartTime, ibStopTime;
    private TextView tvStartTime, tvStopTime;
    private Button btnSave;
    private Spinner spDay;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        setContentView(R.layout.add_schedule);
        Bundle args = getIntent().getExtras();
        if (args != null) {
            day = args.getInt(ARG_DAY, 1);
        }
        initialize();

    }

    private void initialize() {
        spDay = (Spinner) findViewById(R.id.spDay);
        if (day != 1) {
            spDay.setSelection(day - 1);
        }
        spDay.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                day = position + 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        tvStartTime = (TextView) findViewById(R.id.tvStartTime);
        tvStopTime = (TextView) findViewById(R.id.tvStopTime);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        startHour = c.get(Calendar.HOUR_OF_DAY);
        startMinute = c.get(Calendar.MINUTE);
        stopHour = startHour + 1;
        stopMinute = startMinute;


        setTime(startHour, startMinute, tvStartTime);
        setTime(stopHour, stopMinute, tvStopTime);

        ibStartTime = (ImageButton) findViewById(R.id.ibStartTime);
        ibStopTime = (ImageButton) findViewById(R.id.ibStopTime);
        ibStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                timeType = TIME_TYPE_START;
                showTimeDialog(startHour, startMinute);
            }
        });
        ibStopTime.setOnClickListener(new View.OnClickListener() {
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
                final Schedule schedule = new Schedule(day, startHour, startMinute, stopHour, stopMinute);
                ContentValues values = new ContentValues();
                values.put("day", schedule.getDay());
                values.put("startHour", schedule.getStartHour());
                values.put("startMinute", schedule.getStartMinute());
                values.put("stopHour", schedule.getStopHour());
                values.put("stopMinute", schedule.getStopMinute());
                Uri inserted = getContentResolver().insert(MobitrackContentProvider.SCHEDULES_URI, values);
                if (inserted != null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_success_save_schedule), Toast.LENGTH_SHORT).show();
                    setIntentForSchedule((int) ContentUris.parseId(inserted));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.msg_failed_save_schedule), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setIntentForSchedule(int id) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_START_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, 0);
        long interval = AlarmManager.INTERVAL_DAY * 7;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, startHour);
        c.set(Calendar.MINUTE, startMinute);
        c.set(Calendar.DAY_OF_WEEK, day);
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
                setTime(startHour, startMinute, tvStartTime);
                break;
            case TIME_TYPE_STOP:
                stopHour = hourOfDay;
                stopMinute = minute;
                setTime(stopHour, stopMinute, tvStopTime);
                break;
        }
    }
}
