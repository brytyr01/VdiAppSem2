package com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering;

import android.hardware.SensorEvent;
import android.os.AsyncTask;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerationObject;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerClass;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.TrueAcceleration;

import java.util.ArrayList;

public class SignalProcessing extends AsyncTask<SensorEvent, Void, Void> {

    private AccelerometerActivity activity;
    private AccelerometerClass accelerometerClass;
    private static double[] output=new double[] { 0, 0, 0 };
    private long startTime;
    MeanProcessing processing;
    private static int count = 0;
    private double timeConstant;
    ArrayList<AccelData> meanFilterList;
    private double CurrAngle;
    private float[] gravity;

    public SignalProcessing(AccelerometerActivity accelerometerActivity, double timeConstant, long startTime, ArrayList<AccelData> meanFilterList, double currAngle, MeanProcessing meanProcessing, float[] gravity) {
        activity = accelerometerActivity;
        this.startTime = startTime;
        this.timeConstant=timeConstant;
        this.meanFilterList=meanFilterList;
        this.CurrAngle=currAngle;
        processing=meanProcessing;
        this.gravity=gravity;
    }
    public SignalProcessing(AccelerometerClass accelerometerClass, double timeConstant, long startTime, ArrayList<AccelData> meanFilterList, double currAngle, MeanProcessing meanProcessing, float[] gravity) {
        this.accelerometerClass = accelerometerClass;
        this.startTime = startTime;
        this.timeConstant=timeConstant;
        this.meanFilterList=meanFilterList;
        this.CurrAngle=currAngle;
        processing=meanProcessing;
        this.gravity=gravity;
    }

    public void resetStaticValues(){
         output=new double[] { 0, 0, 0 };
         count = 0;
    }



    @Override
    protected Void doInBackground(SensorEvent... events) {

        processing.CalculateMean();
        AccelData data = processing.getDataPoint();


            processAccelData(data);
            onPostExecute(data);
        return null;
    }

    private void processAccelData(AccelData data) {

//        float dt = 1 / (count++ / ((data.getTimestamp() - startTime) / 1000000000.0f));

        double alpha = timeConstant / (timeConstant + dt);

        output[0] = alpha * output[0] + (1 - alpha) * data.getX();
        output[1] = alpha * output[1] + (1 - alpha) * data.getY();
        output[2] = alpha * output[2] + (1 - alpha) * data.getZ();

        data.setX(data.getX() - output[0]);
        data.setY(data.getY() - output[1]);
        data.setZ(data.getZ() - output[2]);
        data.setX(data.getX() - gravity[0]);
        data.setY(data.getY() - gravity[1]);
        data.setZ(data.getZ() - gravity[2]);

    }

    protected void onPostExecute(AccelData data) {
        TrueAcceleration accelpoint=new TrueAcceleration(data,CurrAngle);
        AccelerationObject trueAccel = accelpoint.calculateTrueAccelerationVector();

        if(activity!=null) {
            activity.addDataPoint(data);
            activity.addAccelPoint(trueAccel);
        }
        if(accelerometerClass!=null){
            accelerometerClass.addDataPoint(data);
            accelerometerClass.addAccelPoint(trueAccel);
        }

    }
}

