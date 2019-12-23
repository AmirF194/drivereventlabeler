package com.sharif.armin.drivingeventlabeler.sensor;

import android.location.Location;

public interface SensorsObserver {
    void onSensorChanged(SensorSample sample);
    void onLocationChanged(Location location);
}
