package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.AngleCalcAsync;
import com.example.bryantyrrell.vdiapp.GPSMap.VeichleMoving.VeichleInTransit;

import java.util.ArrayList;

public class GyroscopeService  extends Service implements SensorEventListener {

        private SensorManager sensorManager;
        private MaxGyroService service;
        private GyroProcessing processing;
        private static ArrayList<GyroData> sensorData;
        private static ArrayList<GyroscopeObject> gyroLine;
        private float[] gravity,geomagnetic;
        private AngleCalcAsync angleCalc;
        private double currAngle;
        private int AngleCount=0;
        private double TotalAngle;
        private boolean SetUp;
        private VeichleInTransit check;


        private void setup() {

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            sensorData = new ArrayList();
            gyroLine = new ArrayList();
            sensorData = new ArrayList();

            Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
            Sensor magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_NORMAL);

            service = new MaxGyroService(gyroLine,this);


            check = new VeichleInTransit();
            SetUp=true;

            //AngleCount=0;


        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if(SetUp) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    SetUp = check.checkVeichleMovement(event);
                }
            }
                if (!SetUp) {
                    if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        sensorData.add(new GyroData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
                    }
                    if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                        gravity = event.values;
                    if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                        geomagnetic = event.values;

                    if (AngleCount < 5 && gravity != null && geomagnetic != null) {
                        AngleCount++;
                        angleCalc = new AngleCalcAsync(this, gravity, geomagnetic);
                        angleCalc.execute();
                        return;
                    }
                    if (currAngle != 0 && sensorData.size() != 0) {
                        processing = new GyroProcessing(this, sensorData.get(sensorData.size() - 1), currAngle);
                        processing.execute();
                    }
                }
            }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }

        public void setCurrAngle(double currAngle) {
            TotalAngle=TotalAngle+currAngle;
            this.currAngle=TotalAngle/AngleCount;
        }

        public void addDataPoint(GyroData data) {
            sensorData.add(data);
        }

        public void addAccelPoint(GyroscopeObject trueGyro) {
            gyroLine.add(trueGyro);
            service.CheckSteering();
        }

        public void SetDangerousSteering() {
            //try {
            //    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.beep);
            //    mp.start();
            //} catch (Exception e) {
            //    e.printStackTrace();
            //}
            Intent test1 = new Intent("com.vdi.driving.steering");
            this.sendBroadcast(test1);
        }

    @Override
    public IBinder onBind(Intent intent) {
        setup();
        return null;
    }
    @Override
    public void onCreate() {
        setup();
    }
}




