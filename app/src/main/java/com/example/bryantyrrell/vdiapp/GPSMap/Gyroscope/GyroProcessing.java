package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;

import android.os.AsyncTask;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerClass;
import com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering.MeanProcessing;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeProcessing.GyroscopeMeanProcessing;

import java.util.ArrayList;

public class GyroProcessing extends AsyncTask<Void , Void, Void> {

        private GyroscopeActivity activity;
        private GyroscopeService gyroscopeService;
        private double CurrAngle;
        private GyroData Gyrodatapoint;

        private static double[] output=new double[] { 0, 0, 0 };
        GyroscopeMeanProcessing processing;
        private static int count = 0;
        ArrayList<GyroData> meanFilterList;

    public GyroProcessing(GyroscopeActivity gyroscopeActivity, ArrayList<GyroData> meanFilterList, double currAngle, GyroscopeMeanProcessing meanProcessing) {
        activity = gyroscopeActivity;
        this.meanFilterList=meanFilterList;
        this.CurrAngle=currAngle;
        processing=meanProcessing;
    }
    public GyroProcessing(GyroscopeService gyroscopeService, ArrayList<GyroData> meanFilterList, double currAngle, GyroscopeMeanProcessing meanProcessing) {
        this.gyroscopeService = gyroscopeService;
        this.meanFilterList=meanFilterList;
        this.CurrAngle=currAngle;
        processing=meanProcessing;
    }

    public void resetStaticValues(){
        output=new double[] { 0, 0, 0 };
        count = 0;
    }



        @Override
        protected Void doInBackground(Void... events) {

            processing.CalculateMean();
            GyroData data = processing.getDataPoint();


            onPostExecute(data);
            return null;
        }



        protected void onPostExecute(GyroData data) {

            TrueGyroscope gyroPoint=new TrueGyroscope(data,CurrAngle);
            GyroscopeObject trueGyro = gyroPoint.calculateTrueGyroscopeVector();

            if(activity!=null) {
                activity.addDataPoint(data);
                activity.addAccelPoint(trueGyro);
            }
            if(gyroscopeService!=null) {
                gyroscopeService.addDataPoint(data);
                gyroscopeService.addAccelPoint(trueGyro);
            }

        }
}



