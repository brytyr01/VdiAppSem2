package com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.AsyncTask;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerClass;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeService;

public class AngleCalcAsync extends AsyncTask<Void , Void, Void> {

    private float[] gravity,geomagnetic;
    private AccelerometerActivity accelActivity;
    private GyroscopeActivity gyroActivity;
    private AccelerometerClass accelerometerClass;
    private GyroscopeService gyroscopeService;

    public AngleCalcAsync(AccelerometerActivity accelerometerActivity, float[] gravity, float[] geomagnetic){
        accelActivity=accelerometerActivity;
        this.geomagnetic=geomagnetic;
        this.gravity=gravity;
    }
    public AngleCalcAsync(GyroscopeActivity gyroscopeActivity, float[] gravity, float[] geomagnetic){
        gyroActivity=gyroscopeActivity;
        this.geomagnetic=geomagnetic;
        this.gravity=gravity;
    }
    public AngleCalcAsync(AccelerometerClass accelerometerClass, float[] gravity, float[] geomagnetic){
        this.accelerometerClass=accelerometerClass;
        this.geomagnetic=geomagnetic;
        this.gravity=gravity;
    }
    public AngleCalcAsync(GyroscopeService gyroscopeService, float[] gravity, float[] geomagnetic){
        this.gyroscopeService=gyroscopeService;
        this.geomagnetic=geomagnetic;
        this.gravity=gravity;
    }




    @Override
    protected Void doInBackground(Void... voids) {
        float R[] = new float[9];
        float I[] = new float[9];
        boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomagnetic);
        if (success) {
            float orientation[] = new float[3];
            SensorManager.getOrientation(R, orientation);
            double pitch = orientation[1]; // pitch is The geomagnetic inclination angle in radians.
            double CurrAngle = Math.toDegrees(pitch);
            System.out.println("got the anglllllllllllllllllllllllllllllleeeeeeeeeeeeeeeeeeeeeeeeeee" + CurrAngle);

            if(accelActivity!=null) {
                accelActivity.setCurrAngle(CurrAngle);
            }
            if(gyroActivity!=null){
                gyroActivity.setCurrAngle(CurrAngle);
            }
            if(accelerometerClass!=null){
                accelerometerClass.setCurrAngle(CurrAngle);
            }
            if(gyroscopeService!=null){
                gyroscopeService.setCurrAngle(CurrAngle);
            }
        }
        return null;
    }
}
