package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.AngleCalcAsync;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeProcessing.GyroscopeMeanProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.DrivingBroadcast;
import com.example.bryantyrrell.vdiapp.GPSMap.VeichleMoving.VeichleInTransit;
import com.example.bryantyrrell.vdiapp.R;

import java.util.ArrayList;

public class GyroscopeService  extends Service implements SensorEventListener {

        private SensorManager sensorManager;
        private MaxGyroService service;
        private GyroProcessing processing;
        private DrivingBroadcast driverAlertSystem;
        private GyroscopeMeanProcessing gyroscopeMeanProcessing;
        private static ArrayList<GyroData> MeanFilterList;
        private static ArrayList<GyroscopeObject> gyroLine;
    private static ArrayList<GyroData> sensorData;
        private float[] gravity,geomagnetic;
        private AngleCalcAsync angleCalc;
        private double currAngle;
        private boolean alert=true;
        private int AngleCount=0;
        private double TotalAngle;
        private boolean SetUp;
        private Sensor gyro;
        private Sensor magnet;
        private Sensor accel;
        private int count;
        private VeichleInTransit check;


        private void setup() {

            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            MeanFilterList = new ArrayList();
            gyroscopeMeanProcessing = new GyroscopeMeanProcessing(MeanFilterList);
            gyroLine = new ArrayList();
            sensorData = new ArrayList();
            count=0;

             gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
             magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
             accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, gyro, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_FASTEST);

            service = new MaxGyroService(gyroLine,this);


            check = new VeichleInTransit();
            SetUp=true;

            registerSteeringReceiver();

            //AngleCount=0;


        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            if(AngleCount < 5) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    gravity = event.values;
                if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    geomagnetic = event.values;

                if ( gravity != null && geomagnetic != null) {
                    AngleCount++;
                    angleCalc = new AngleCalcAsync(this, gravity, geomagnetic);
                    angleCalc.execute();
                    return;
                }
            }
            if(SetUp) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    SetUp = check.checkVeichleMovement(event);
                }
            }
                if (!SetUp) {
                    if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
                        count++;
                        MeanFilterList.add(new GyroData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]));
                    }

                    if (currAngle != 0 && count > 60) {
                        processing = new GyroProcessing(this, MeanFilterList, currAngle,gyroscopeMeanProcessing);
                        processing.execute();
                        count=0;
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
            if(alert){
                    try {
                            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dangerouscornering);
                            mp.start();
                            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                            {

                                @Override
                                public void onCompletion(MediaPlayer mp)
                                {
                                    // TODO Auto-generated method stub
                                    mp.stop();
                                    System.out.println("Media Plyer Is Complete !!!");
                                    mp.release();
                                    System.out.println("Music is over and Button is enable !!!!!!");
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                Long IncidentTime = System.currentTimeMillis();
                Intent test1 = new Intent("com.vdi.driving.steering");
                test1.putExtra("incidentTime",IncidentTime);
                this.sendBroadcast(test1);
                setAlertFalse();
            }
        }
    public void setAlertTrue(){alert=true;}
    public void setAlertFalse(){alert=false;}

    private void registerSteeringReceiver() {
        driverAlertSystem = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setAlertTrue();
            }
        };
        registerReceiver(driverAlertSystem, new IntentFilter("com.vdi.driving.driverAlertSystemSteering"));
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

    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this, accel);
        sensorManager.unregisterListener(this, magnet);
        sensorManager.unregisterListener(this, gyro);
    }
}




