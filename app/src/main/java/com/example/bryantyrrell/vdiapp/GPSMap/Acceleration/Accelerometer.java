package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;


public class Accelerometer extends Service implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor LinearAccelerometer,gravityAccelerometer,unCalAccelerometer;
    private float last_x=0;
    private float last_y=0;
    private float last_z = 0;
    private long lastUpdate=0;

    public Accelerometer(){
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("got to start command");
        // Get sensor manager on starting the service.
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // Get default sensor type
        LinearAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Get default sensor type
        gravityAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Get default sensor type
        unCalAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // Registering...
       // mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
//        //super.onCreate();
//        System.out.println("got to create command");
//        // Get sensor manager on starting the service.
//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//
//        // Get default sensor type
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        // Registering...
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public IBinder onBind(Intent intent) {
//        System.out.println("got to bind command");
//        // Get sensor manager on starting the service.
//        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
//
//        // Get default sensor type
//        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//
//        // Registering...
//        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        return null;
    }


    public void onSensorChanged(SensorEvent se) {

        Sensor mySensor = se.sensor;
        System.out.println("The sensor changed!!!!");

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float[] values = se.values;
            float x = values[0];
            float y = values[1];
            float z = values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 1000) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                last_x = x;
                last_y = y;
                last_z = z;

                System.out.println("accelerometer"+Float.toString(values[0]) + ";" +
                        Float.toString(values[1]) + ";" +
                        Float.toString(values[2]) + ";" +
                        "Speed: "+ speed + "\n");
            }
            }

    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}