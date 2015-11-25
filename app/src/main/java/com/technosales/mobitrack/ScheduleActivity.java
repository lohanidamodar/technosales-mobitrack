package com.technosales.mobitrack;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Switch;

/**
 * Created by dlohani on 11/21/15.
 */
public class ScheduleActivity extends Activity
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    public static final String KEY_SCHEDULING = "scheduling";
    private static final String TAG = ScheduleActivity.class.getSimpleName();
    CheckBox cbSchedule;
    Switch swSchedule;
    SharedPreferences preferences;
    ListView lvDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        lvDays = (ListView) findViewById(R.id.lv_days);
        lvDays.setOnItemClickListener(this);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            swSchedule = (Switch) findViewById(R.id.scheduling);
            swSchedule.setOnClickListener(this);
            swSchedule.setChecked(preferences.getBoolean(KEY_SCHEDULING, false));
        } else {
            cbSchedule = (CheckBox) findViewById(R.id.scheduling);
            cbSchedule.setOnClickListener(this);
            cbSchedule.setChecked(preferences.getBoolean(KEY_SCHEDULING, false));
        }
        configureListView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.scheduling:

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    preferences.edit().putBoolean(KEY_SCHEDULING, swSchedule.isChecked()).commit();
                } else {
                    preferences.edit().putBoolean(KEY_SCHEDULING, cbSchedule.isChecked()).commit();
                }
                configureListView();
                Utils.scheduling(preferences.getBoolean(KEY_SCHEDULING, false), this);
                break;
        }
    }

    private void configureListView() {
        if (preferences.getBoolean(KEY_SCHEDULING, false))
            lvDays.setVisibility(View.VISIBLE);
        else
            lvDays.setVisibility(View.GONE);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent viewIntent = new Intent(this, ViewScheduleActivity.class);
        viewIntent.putExtra(ViewScheduleActivity.ARG_DAY, position + 1);
        startActivity(viewIntent);
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
                startActivity(new Intent(this, AddScheduleActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
