package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

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
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.MeanProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.SignalProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.DrivingBroadcast;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.VeichleMoving.VeichleInTransit;
import com.example.bryantyrrell.vdiapp.R;

import java.util.ArrayList;

public class AccelerometerClass extends Service implements SensorEventListener {



private SensorManager sensorManager;
private com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.MeanProcessing MeanProcessing;
private Sensor accel;
private  Sensor gravitySensor;
private MaxAccelService service;
private Sensor magnet;
private SignalProcessing processing;
private int count;
private int AngleCount;
private DrivingBroadcast driverAlertSystem;
private double TotalAngle;
private long startTime;
private static ArrayList<AccelData> sensorData;
private static ArrayList<AccelerationObject> accelLine;
private ArrayList<AccelData> MeanFilterList;
private float[] gravity,geomagnetic;
private double CurrAngle;
private AngleCalcAsync angleCalc;
private boolean SetUp;
private boolean alert=true;
private VeichleInTransit check;
private MapsActivity mapsActivity;


public AccelerometerClass(MapsActivity mapsActivity){
    this.mapsActivity=mapsActivity;
    //setup();
}
    public AccelerometerClass(){
    }
private void setup(){

            registerAccelerationReceiver();
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorData = new ArrayList();
            MeanFilterList = new ArrayList();
            MeanProcessing = new MeanProcessing(MeanFilterList);
            sensorData = new ArrayList();
            accelLine = new ArrayList();
            startTime = System.nanoTime();

            count = 0;
            AngleCount=0;
            TotalAngle=0;

            service = new MaxAccelService(accelLine,this);


            accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, magnet, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);

            check = new VeichleInTransit();
            SetUp=true;

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

            if (SetUp) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    SetUp = check.checkVeichleMovement(event);
                }
            }

            if (!SetUp) {

                if(event.sensor.getType() == Sensor.TYPE_GRAVITY){
                    gravity= event.values;
                }

                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    count++;
                    MeanFilterList.add(new AccelData(System.nanoTime(), event.values[0], event.values[1], event.values[2]));
                }

                if (count > 200 && CurrAngle != 0) {
                    processing = new SignalProcessing(this, 0.5, startTime, MeanFilterList, CurrAngle, MeanProcessing,gravity);
                    processing.execute(event);
                    count = 0;
                }
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
            service.CheckAccelerationSensorActivity(9);


        }
        public void setCurrAngle(Double angle) {
            TotalAngle=TotalAngle+angle;
            CurrAngle=TotalAngle/AngleCount;

        }


    public void SetDangerousAcceleration() {
      if(alert){
                  try {
                        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dangerousacceleration);
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
                    Intent test1 = new Intent("com.vdi.driving.acceleration");
                    test1.putExtra("incidentTime",IncidentTime);
                    this.sendBroadcast(test1);
                    setAlertFalse();
                }
}

    public void SetDangerousDeceleration() {
        if(alert){
                    try {
                            MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.dangerousdeceleration);
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
                        Intent test1 = new Intent("com.vdi.driving.acceleration");
                        test1.putExtra("incidentTime",IncidentTime);
                        this.sendBroadcast(test1);
                        setAlertFalse();
    }
}

    @Override
    public IBinder onBind(Intent intent) {
    //intent.

    return null;
    }

    @Override
    public void onCreate() {
        setup();
   }

   public void setAlertTrue(){alert=true;}
   public void setAlertFalse(){alert=false;}

   private void registerAccelerationReceiver() {
        driverAlertSystem = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setAlertTrue();
            }
        };
        registerReceiver(driverAlertSystem, new IntentFilter("com.vdi.driving.driverAlertSystemAcceleration"));
    }


    @Override
    public void onDestroy() {
        sensorManager.unregisterListener(this, accel);
        sensorManager.unregisterListener(this, magnet);
        sensorManager.unregisterListener(this, gravitySensor);
    }
    }



