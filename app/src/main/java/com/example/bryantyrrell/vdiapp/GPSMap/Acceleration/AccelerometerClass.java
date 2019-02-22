package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.AngleCalcAsync;
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.MeanProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.SignalProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.DrivingBroadcast;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.example.bryantyrrell.vdiapp.R;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.ArrayList;

public class AccelerometerClass extends Service implements SensorEventListener {



private SensorManager sensorManager;
private com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.MeanProcessing MeanProcessing;
private Sensor accel;
private MaxAccelService service;
private Sensor magnet;
private SignalProcessing processing;
private int count = 0;
private int AngleCount=0;
private double TotalAngle=0;
private boolean started = false;
private long startTime;
private static ArrayList<AccelData> sensorData;
private static ArrayList<AccelerationObject> accelLine;
private ArrayList<AccelData> MeanFilterList;
private float[] gravity,geomagnetic;
private double CurrAngle;
private AngleCalcAsync angleCalc;
private MapsActivity mapsActivity;


public AccelerometerClass(MapsActivity mapsActivity){
    this.mapsActivity=mapsActivity;
    //setup();
}
    public AccelerometerClass(){
    }
private void setup(){

            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorData = new ArrayList();
            MeanFilterList = new ArrayList();
            MeanProcessing = new MeanProcessing(MeanFilterList);
            sensorData = new ArrayList();
            accelLine = new ArrayList();
            startTime = System.nanoTime();


            service = new MaxAccelService(accelLine,this);


            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_NORMAL);

        }

        @Override
        public void onSensorChanged(SensorEvent event) {


                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER ) {
                    count++;
                    MeanFilterList.add(new AccelData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
                }

                if(count>2&&CurrAngle!=0) {
                    processing=new SignalProcessing(this, 0.5, startTime,MeanFilterList,CurrAngle,MeanProcessing);
                    processing.execute(event);
                    count=0;
                }

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    geomagnetic = event.values;



                if (AngleCount<5&&gravity != null && geomagnetic != null) {
                    AngleCount++;
                    angleCalc = new AngleCalcAsync(this,gravity,geomagnetic);
                    angleCalc.execute();
                }

            }



        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void addDataPoint(AccelData data) {
            sensorData.add(data);

        }
        public void addAccelPoint(AccelerationObject data) {
            accelLine.add(data);

            service.CheckAcceleration();


        }
        public void setCurrAngle(Double angle) {
            TotalAngle=TotalAngle+angle;
            CurrAngle=TotalAngle/AngleCount;

        }

        public void SetDangerousAcceleration() {
           // try {
                //MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.beep);
               // mp.start();
            //} catch (Exception e) {
                //e.printStackTrace();
            //}

            Intent test1 = new Intent("com.vdi.driving.acceleration");
            this.sendBroadcast(test1);

        }

    @Override
    public IBinder onBind(Intent intent) {
    //intent.
        setup();

    return null;
    }

    @Override
    public void onCreate() {
        setup();
   }
}



