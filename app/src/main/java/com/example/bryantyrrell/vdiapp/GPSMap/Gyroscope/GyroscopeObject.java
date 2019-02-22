package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;

public class GyroscopeObject {
    private double trueGyro;
    private double timestamp;

    GyroscopeObject(double timestamp,double trueGyro){
        this.trueGyro=trueGyro;
        this.timestamp=timestamp;
    }

    public double getTruetrueGyro(){
        return trueGyro;
    }
    public double getTimeStamp(){
        return timestamp;
    }
}

