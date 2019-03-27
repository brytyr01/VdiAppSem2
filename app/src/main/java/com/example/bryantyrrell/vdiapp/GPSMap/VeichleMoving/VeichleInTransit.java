package com.example.bryantyrrell.vdiapp.GPSMap.VeichleMoving;

import android.hardware.SensorEvent;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;

import java.util.ArrayList;
import java.util.List;

public class VeichleInTransit {
    private List<Double> SingleAccelerationMag=new ArrayList<>();
    double MaxThreshold = 0.2d;//.1
    double MinThreshold = -0.2d;//should be .1

    public void VeichleInTransit(){

        SingleAccelerationMag=new ArrayList<>();
    }



    public boolean checkVeichleMovement(SensorEvent event){
        AccelData currentPoint = new AccelData(System.currentTimeMillis(), event.values[0], event.values[1], event.values[2]);
        double magnitude = calculateAccelerationMagnitude(currentPoint);
        SingleAccelerationMag.add(magnitude);

        double totalMagnitude=0;
        double AverageMagnitude=0;

        //loop through the arraylist to calculate the average acceleration magnitude
        for(int i=0;i<SingleAccelerationMag.size();i++){
            totalMagnitude+=SingleAccelerationMag.get(i);

            if(i==(SingleAccelerationMag.size()-1)){
                AverageMagnitude=totalMagnitude/SingleAccelerationMag.size();
                //AverageAccelerationMag.add(AverageMagnitude);
            }
        }
        double variance = CalculateVariance(AverageMagnitude,SingleAccelerationMag);

        double StandardDeviation = CalculateStandardDeviation(variance);

        //remove the first index if the arraylist goes above 10!
        if(SingleAccelerationMag.size()>15){
            SingleAccelerationMag.remove(0);
        }
        // wont allow threshold check until at least 15 points have been gathered roughly 1 second
        if (SingleAccelerationMag.size()>14) {
            boolean result = checkSandardDeviationThreshold(StandardDeviation);
            return result;
        }
        else{
            return true;
        }


    }

    private double CalculateStandardDeviation(double variance) {
        return Math.sqrt(variance);
    }

    private double CalculateVariance(double mean, List<Double> singleAccelerationMagList) {
        double totalSumMagnitudeMinusMean=0;
        double variance = 0;

        for(int i=0;i<singleAccelerationMagList.size();i++){

            totalSumMagnitudeMinusMean=totalSumMagnitudeMinusMean+Math.pow(((singleAccelerationMagList.get(i)-mean)),2);

        }
        variance=totalSumMagnitudeMinusMean/singleAccelerationMagList.size();
        return variance;
    }

    private boolean checkSandardDeviationThreshold(double StandardDeviation) {
        boolean  AboveThreshold;
        //use in thesis to show output of acccelerometer threshold
        //System.out.println("The standard deviation Magnitude is: "+StandardDeviation);


        if(StandardDeviation>MaxThreshold||StandardDeviation<MinThreshold){
            AboveThreshold = true;
        }else{
            AboveThreshold = false;
        }
        return AboveThreshold;
    }

    private double calculateAccelerationMagnitude(AccelData currentPoint) {
        double magnitude = Math.sqrt((Math.pow(currentPoint.getX(),2)+Math.pow(currentPoint.getY(),2)+Math.pow(currentPoint.getZ(),2)));
        return magnitude;
    }
}
