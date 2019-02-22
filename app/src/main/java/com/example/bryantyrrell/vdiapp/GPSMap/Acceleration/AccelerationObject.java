package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

public class AccelerationObject {
    private double trueAccel;
    private double timestamp;

    AccelerationObject(double timestamp,double trueAccel){
        this.trueAccel=trueAccel;
        this.timestamp=timestamp;
    }

    public double getTrueAccel(){
        return trueAccel;
    }
    public double getTimeStamp(){
        return timestamp;
    }
}
