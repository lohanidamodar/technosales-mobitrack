package com.technosales.mobitrack;

import android.database.Cursor;

/**
 * Created by dlohani on 11/23/15.
 */
public class Schedule {
    private long id;
    private int day;
    private int startHour;
    private int startMinute;
    private int stopHour;
    private int stopMinute;

    public Schedule() {

    }

    public Schedule(int day, int startHour, int startMinute, int stopHour, int stopMinute) {
        this.day = day;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.stopHour = stopHour;
        this.stopMinute = stopMinute;
    }

    public static Schedule fromCursor(Cursor cursor) {
        Schedule schedule = new Schedule();
        schedule.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        schedule.setDay(cursor.getInt(cursor.getColumnIndex("day")));
        schedule.setStartHour(cursor.getInt(cursor.getColumnIndex("startHour")));
        schedule.setStartMinute(cursor.getInt(cursor.getColumnIndex("startMinute")));
        schedule.setStopHour(cursor.getInt(cursor.getColumnIndex("stopHour")));
        schedule.setStopMinute(cursor.getInt(cursor.getColumnIndex("stopMinute")));
        return schedule;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getStartMinute() {
        return startMinute;
    }

    public void setStartMinute(int startMinute) {
        this.startMinute = startMinute;
    }

    public int getStopHour() {
        return stopHour;
    }

    public void setStopHour(int stopHour) {
        this.stopHour = stopHour;
    }

    public int getStopMinute() {
        return stopMinute;
    }

    public void setStopMinute(int stopMinute) {
        this.stopMinute = stopMinute;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
