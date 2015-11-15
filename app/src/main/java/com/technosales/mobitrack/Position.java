/*
 * Copyright 2015 Anton Tananaev (anton.tananaev@gmail.com)
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

import android.location.Location;

import java.util.Date;

public class Position {

    private long id;
    private String deviceId;
    private Date time;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private double course;
    private double battery;
    public Position() {
    }
    public Position(String deviceId, Location location, double battery) {
        this.deviceId = deviceId;
        time = new Date(location.getTime());
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        speed = location.getSpeed() * 1.943844; // speed in knots
        course = location.getBearing();
        this.battery = battery;
    }

    public long getId() { return id; }

    public void setId(long id) { this.id = id; }

    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Date getTime() { return time; }

    public void setTime(Date time) { this.time = time; }

    public double getLatitude() { return latitude; }

    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }

    public void setLongitude(double longitude) { this.longitude = longitude; }

    public double getAltitude() { return altitude; }

    public void setAltitude(double altitude) { this.altitude = altitude; }

    public double getSpeed() { return speed; }

    public void setSpeed(double speed) { this.speed = speed; }

    public double getCourse() { return course; }

    public void setCourse(double course) { this.course = course; }

    public double getBattery() { return battery; }
    public void setBattery(double battery) { this.battery = battery; }

}
