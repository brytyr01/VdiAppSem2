package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;

import android.os.AsyncTask;

public class GyroProcessing extends AsyncTask<Void , Void, Void> {

        private GyroscopeActivity activity;
        private GyroscopeService gyroscopeService;
        private double CurrAngle;
        private GyroData Gyrodatapoint;

        public GyroProcessing(GyroscopeActivity GyroscopeActivity, GyroData data, double currAngle) {
            activity = GyroscopeActivity;
            this.CurrAngle=currAngle;
            this.Gyrodatapoint=data;
        }
    public GyroProcessing(GyroscopeService gyroscopeService, GyroData data, double currAngle) {
        this.gyroscopeService = gyroscopeService;
        this.CurrAngle=currAngle;
        this.Gyrodatapoint=data;
    }




        @Override
        protected Void doInBackground(Void... events) {


            processAccelData(Gyrodatapoint);
            return null;
        }

        private void processAccelData(GyroData data) {
            onPostExecute(data);

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



