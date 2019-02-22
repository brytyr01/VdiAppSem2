package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;


import java.util.ArrayList;

class MaxGyroService  {
    private double MaxAcceleration = 2.0d;
    private double MinAcceleration = -2.0d;
    private GyroscopeActivity gyroscopeActivity;
    private GyroscopeService gyroscopeService;
    private static ArrayList<GyroscopeObject> gyroLine;

    public MaxGyroService(ArrayList<GyroscopeObject> gyroLine,GyroscopeActivity gyroscopeActivity) {
        this.gyroLine=gyroLine;
        this.gyroscopeActivity=gyroscopeActivity;
        MonitorAccelerationUpdate();
    }
    public MaxGyroService(ArrayList<GyroscopeObject> gyroLine,GyroscopeService gyroscopeService) {
        this.gyroLine=gyroLine;
        this.gyroscopeService=gyroscopeService;
        MonitorAccelerationUpdate();
    }




    private void MonitorAccelerationUpdate(){
        int currSize = gyroLine.size();
        //  while(true){
        if(currSize < gyroLine.size()){
            currSize = gyroLine.size();
            CheckSteering();

        }

        //  }
    }

    public void CheckSteering() {
        int currSize = gyroLine.size();
        GyroscopeObject gyroscopeObj = gyroLine.get(currSize-1);
        System.out.println("True steering in rads value is: "+gyroscopeObj.getTruetrueGyro());
        if(gyroscopeObj.getTruetrueGyro()>MaxAcceleration){
            //updateMapUI
            updateActivity();
        }else if(gyroscopeObj.getTruetrueGyro()<MinAcceleration){
            //updateMapUI
            updateActivity();
        }


    }

    private void updateActivity(){

        if(gyroscopeActivity!=null) {
            gyroscopeActivity.SetDangerousSteering();
        }
        if(gyroscopeService!=null){
            gyroscopeService.SetDangerousSteering();
        }
    }
}

