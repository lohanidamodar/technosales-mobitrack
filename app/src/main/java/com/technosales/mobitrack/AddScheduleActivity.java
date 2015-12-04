package com.technosales.mobitrack;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
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
                    if (getNumberOfSchedules(curDay + 1) < 5) {
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
                            setIntentForSchedule(ContentUris.parseId(inserted));
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.msg_failed_save_schedule), Toast.LENGTH_SHORT).show();
                            break;
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Cannot save schedule for " + dayNames[curDay] + ", only 5 schedules allowed.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "failed to add schedule due to max for " + dayNames[curDay]);
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

    private void setIntentForSchedule(long id) {
        Utils.setScheduleAlarmForId(id, this);
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

    private int getNumberOfSchedules(int day) {
        String[] projection = {"_id", "day", "startHour", "startMinute", "stopHour", "stopMinute"};
        String selection = "day=?";
        String[] selection_arguments = new String[]{String.valueOf(day)};
        Cursor cursor = getContentResolver().query(MobitrackContentProvider.SCHEDULES_URI, projection, selection, selection_arguments, null);
        if (cursor != null)
            return cursor.getCount();
        else
            return 0;
    }
}
