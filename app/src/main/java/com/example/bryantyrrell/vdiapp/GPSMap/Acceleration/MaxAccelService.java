package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;

import java.util.ArrayList;

public class MaxAccelService {
    private double MaxAcceleration = 0.000015d;
    private double MinAcceleration = -0.000015d;
    private AccelerometerActivity accelerometerActivity;
    private AccelerometerClass accelerometerClass;
    private static ArrayList<AccelerationObject> accelLine;

    public MaxAccelService(ArrayList<AccelerationObject> accelLine,AccelerometerActivity accelerometerActivity) {
        this.accelLine=accelLine;
        this.accelerometerActivity=accelerometerActivity;
        MonitorAccelerationUpdate();
    }
    public MaxAccelService(ArrayList<AccelerationObject> accelLine,AccelerometerClass accelerometerClass) {
        this.accelLine=accelLine;
        this.accelerometerClass=accelerometerClass;
        MonitorAccelerationUpdate();
    }
    public MaxAccelService() {
    }




    private void MonitorAccelerationUpdate(){
        int currSize = accelLine.size();
      //  while(true){
            if(currSize < accelLine.size()){
                currSize = accelLine.size();
                CheckAcceleration();

            }

      //  }
    }

    public void CheckAcceleration() {
        int currSize = accelLine.size();
        AccelerationObject accelerationObj = accelLine.get(currSize-1);
        System.out.println("True acceleration value is: "+accelerationObj.getTrueAccel());
        if(accelerationObj.getTrueAccel()>MaxAcceleration||accelerationObj.getTrueAccel()<MinAcceleration){
            //updateMapUI
            updateActivity();
        }


    }

    private void updateActivity(){
        if(accelerometerActivity!=null) {
            accelerometerActivity.SetDangerousAcceleration();
        }
        if(accelerometerClass!=null){
            accelerometerClass.SetDangerousAcceleration();
        }
    }
}
