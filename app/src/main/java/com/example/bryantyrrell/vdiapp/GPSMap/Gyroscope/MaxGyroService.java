package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;


import java.util.ArrayList;

class MaxGyroService  {
    private double MaxSteering = 2d;
    private double MinSteering = -2d;
    private GyroscopeActivity gyroscopeActivity;
    private GyroscopeService gyroscopeService;
    private static ArrayList<GyroscopeObject> GyroLine;
    private static ArrayList<Double> AverageThreshold = new ArrayList<>();
    private double summedSteeringValue;
    private double AveragedSteeringValue;
    private int count = 0;
    private double doubledSteeringValue;

    public MaxGyroService(ArrayList<GyroscopeObject> GyroLine,GyroscopeActivity gyroscopeActivity) {
        this.GyroLine=GyroLine;
        this.gyroscopeActivity=gyroscopeActivity;
        MonitorRotationUpdate();
    }
    public MaxGyroService(ArrayList<GyroscopeObject> GyroLine,GyroscopeService gyroscopeService) {
        this.GyroLine=GyroLine;
        this.gyroscopeService=gyroscopeService;
        MonitorRotationUpdate();
    }
    public MaxGyroService() {
    }




    private void MonitorRotationUpdate(){
        int currSize = GyroLine.size();
        if(currSize < GyroLine.size()){
            currSize = GyroLine.size();
            CheckSteering();

        }
    }

    public void CheckSteering() {
        int currSize = GyroLine.size();
        GyroscopeObject gyroscopeObj = GyroLine.get(currSize-1);
        UpdateThresholds(gyroscopeObj.getTruetrueGyro());
        System.out.println("True acceleration value is: "+gyroscopeObj.getTruetrueGyro());
        if(gyroscopeObj.getTruetrueGyro()>MaxSteering||gyroscopeObj.getTruetrueGyro()<MinSteering){
            //updateMapUI
            updateActivity(gyroscopeObj.getTruetrueGyro());
        }


    }

    private void updateActivity(double trueAccel){
        if(gyroscopeActivity!=null) {
            if(trueAccel>MaxSteering){
                gyroscopeActivity.SetDangerousCorneringLeft();
            }
            else if(trueAccel<MinSteering){
                gyroscopeActivity.SetDangerousCorneringRight();
            }
        }
        if(gyroscopeService!=null){
            if(trueAccel>MaxSteering){
                gyroscopeService.SetDangerousSteering();
            }
            else if(trueAccel<MinSteering){
                gyroscopeService.SetDangerousSteering();
            }
        }
    }

    private void UpdateThresholds(double trueAccel){

        AverageThreshold.add(MaxSteering);

    }
    public ArrayList<Double> ReturnAverageThreshold(){
        return AverageThreshold;
    }
}

