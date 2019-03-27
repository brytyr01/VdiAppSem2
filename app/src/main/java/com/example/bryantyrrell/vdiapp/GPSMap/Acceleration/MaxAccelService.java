package com.example.bryantyrrell.vdiapp.GPSMap.Acceleration;

import java.util.ArrayList;

public class MaxAccelService {
    private double MaxAcceleration = 1d;
    private double MinAcceleration = -1d;
    private AccelerometerActivity accelerometerActivity;
    private AccelerometerClass accelerometerClass;
    private static ArrayList<AccelerationObject> accelLine;
    private static ArrayList<Double> summedThreshold = new ArrayList<>();
    private static ArrayList<Double> AverageThreshold = new ArrayList<>();
    private static ArrayList<Double> RateOfChangeThreshold = new ArrayList<>();
    private double summedAccelerationValue;
    private boolean DangerousAcceleration=false;
    private double AveragedAccelerationValue;
    private int count = 0;
    private double doubledAccelerationValue;

    public MaxAccelService(ArrayList<AccelerationObject> accelLine,AccelerometerActivity accelerometerActivity) {
        this.accelLine=accelLine;
        this.accelerometerActivity=accelerometerActivity;
        MonitorAccelerationUpdate();
    }
    public MaxAccelService(ArrayList<AccelerationObject> accelLine,AccelerometerClass accelerometerClass) {
        this.accelLine=accelLine;
        this.accelerometerClass=accelerometerClass;
        MonitorAccelerationUpdate();
    }
    public MaxAccelService() {
    }




    private void MonitorAccelerationUpdate(){
        int currSize = accelLine.size();
            if(currSize < accelLine.size()){
                currSize = accelLine.size();
               // CheckAcceleration();

            }
    }

    public void CheckAcceleration() {
        int currSize = accelLine.size();
        AccelerationObject accelerationObj = accelLine.get(currSize-1);
        UpdateThresholds(accelerationObj.getTrueAccel());
        System.out.println("True acceleration value is: "+accelerationObj.getTrueAccel());
        if(accelerationObj.getTrueAccel()>MaxAcceleration||accelerationObj.getTrueAccel()<MinAcceleration){
            //updateMapUI
            updateActivity(accelerationObj.getTrueAccel());
        }


    }
    public void CheckAccelerationSensorActivity(double thresholdValue) {
        int currSize = accelLine.size();
        AccelerationObject accelerationObj = accelLine.get(currSize-1);
        UpdateThresholdsActivity(thresholdValue);
        System.out.println("True acceleration value is: "+accelerationObj.getTrueAccel());
        MaxAcceleration=thresholdValue;
        MinAcceleration=thresholdValue*-1;
        if(accelerationObj.getTrueAccel()>MaxAcceleration||accelerationObj.getTrueAccel()<MinAcceleration){
            //updateMapUI
            updateActivity(accelerationObj.getTrueAccel());
        }


    }

    private void updateActivity(double trueAccel){
        if(accelerometerActivity!=null) {
            if(trueAccel>MaxAcceleration){
                accelerometerActivity.SetDangerousAcceleration();
            }
            else if(trueAccel<MinAcceleration){
                        accelerometerActivity.SetDangerousDeceleration();
            }
        }
        if(accelerometerClass!=null){
            if(trueAccel>MaxAcceleration){
                accelerometerClass.SetDangerousAcceleration();
            }
            else if(trueAccel<MinAcceleration){
                accelerometerClass.SetDangerousDeceleration();
            }
        }
    }

    private void UpdateThresholds(double trueAccel){
        double positiveAcceleration = Math.abs(trueAccel);
        count++;
        summedThreshold.add(positiveAcceleration);
        if(summedThreshold.size()>3){
            summedThreshold.remove(0);
        }
        double SummedValues=0;
        for(double singleValue:summedThreshold){
            SummedValues=SummedValues+singleValue;
        }
        double averageValue =SummedValues/summedThreshold.size();
//        summedAccelerationValue=summedAccelerationValue+positiveAcceleration;
//        System.out.println("summedAccelerationValue is "+summedAccelerationValue);
//
//        AveragedAccelerationValue=summedAccelerationValue/count;
        System.out.println("AveragedAccelerationValue is "+averageValue);
//
        doubledAccelerationValue=averageValue*2;
//
        MaxAcceleration=((doubledAccelerationValue/100)*500);
//        System.out.println("MaxAcceleration is "+MaxAcceleration);
//
//
//        RateOfChangeAcceleration(MinAcceleration,MaxAcceleration);
//
        AverageThreshold.add(MaxAcceleration);
        MinAcceleration=MaxAcceleration*-1;

        System.out.println("Max acceleration threshold is "+MaxAcceleration);
    }










    private void UpdateThresholdsActivity(double ThresholdSet){

        AverageThreshold.add(ThresholdSet);
    }










    private void RateOfChangeAcceleration(double minAcceleration, double maxAcceleration) {

        RateOfChangeThreshold.add(maxAcceleration);
        if(RateOfChangeThreshold.size()>4){
            RateOfChangeThreshold.remove(0);
        }


        double PreviousValue=0;
        double summedDifference=0;
        for(double rateOfChangeValue:RateOfChangeThreshold){

            if(PreviousValue==0){
                PreviousValue=rateOfChangeValue;
                continue;
            }
            double change = Math.cos(1/(rateOfChangeValue-PreviousValue));
            System.out.println("Rate of change is: "+change);
            PreviousValue=rateOfChangeValue;
        }
        System.out.println("count value: "+count);




    }

    public ArrayList<Double> ReturnAverageThreshold(){
        return AverageThreshold;
    }
}
