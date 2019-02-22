package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;


import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerationObject;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class TrueAcceleration {
    private AccelData dataPoint;
    private double angle;

    public TrueAcceleration(AccelData dataPoint, double angle) {
        this.dataPoint = dataPoint;
        this.angle = angle;
    }

    public AccelerationObject calculateTrueAccelerationVector() {


        angle = Math.abs(angle);
        double yAccel = dataPoint.getY() * (cos(angle));
        double zAccel = dataPoint.getZ() / (sin(angle));

        double result = yAccel + zAccel;
        AccelerationObject object = new AccelerationObject(dataPoint.getTimestamp(), result);
        return object;

    }
}