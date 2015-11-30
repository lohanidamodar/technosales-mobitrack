package com.technosales.mobitrack;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * Created by dlohani on 11/25/15.
 */
public class ViewScheduleActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    public static final String ARG_DAY = "day";
    private static final String TAG = ViewScheduleActivity.class.getSimpleName();
    SimpleCursorAdapter mAdapter;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle arguments = getIntent().getExtras();
        if (arguments != null) {
            day = arguments.getInt(ARG_DAY);
        } else {
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);
        getSupportLoaderManager().initLoader(0, null, this);
        mAdapter = new SimpleCursorAdapter(this,
                R.layout.schedule_item_layout,
                null,
                new String[]{"startHour", "startMinute", "stopHour", "stopMinute"},
                new int[]{R.id.tvStartHour, R.id.tvStartMinute, R.id.tvStopHour, R.id.tvStopMinute}, 0);
        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(this);
        listView.setAdapter(mAdapter);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {"_id", "day", "startHour", "startMinute", "stopHour", "stopMinute"};
        String selection = "day=?";
        String[] selection_arguments = new String[]{String.valueOf(day)};
        CursorLoader cursorLoader = new CursorLoader(this,
                MobitrackContentProvider.SCHEDULES_URI, projection, selection, selection_arguments, "_id");
        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.schedule, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_schedule:
                Intent addSchedule = new Intent(this, AddScheduleActivity.class);
                addSchedule.putExtra(ViewScheduleActivity.ARG_DAY, day - 1);
                startActivity(addSchedule);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm");
        builder.setMessage("Are you sure you want to delete this schedule?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long id = mAdapter.getItemId(position);
                deleteSchedule(id);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "don't delete this schedule " + id);
            }
        });
        builder.create().show();
    }

    private void deleteSchedule(long id) {
        Uri scheduleUri = ContentUris.withAppendedId(MobitrackContentProvider.SCHEDULES_URI, id);
        int a = getContentResolver().delete(scheduleUri, null, null);
        if (a == 1) {
            //cancel alarms
            AlarmManager alarmManager = (AlarmManager) this.getSystemService(ALARM_SERVICE);
            Intent intent = new Intent(this, AlarmReceiver.class);
            intent.setAction(AlarmReceiver.ACTION_START_SERVICE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, 0);
            alarmManager.cancel(pendingIntent);
            intent.setAction(AlarmReceiver.ACTION_STOP_SERVICE);
            pendingIntent = PendingIntent.getBroadcast(this, (int) id, intent, 0);
            alarmManager.cancel(pendingIntent);
            Log.d(TAG, "Schedule deleted and alarms cancelled");
        }
    }
}
