package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeProcessing;

import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroData;

import java.util.ArrayList;

public class GyroscopeMeanProcessing  {
    private ArrayList<GyroData> meanFilterList;
    private GyroData meanValue;
    private int count;
    double[] Summedvalues;
    private int RequiredSize=60;
    private long Summedtimestamp;

    public GyroscopeMeanProcessing(ArrayList<GyroData> meanFilterList) {
        this.meanFilterList=meanFilterList;

    }

    public void CalculateMean() {
        int size = meanFilterList.size();
        count = 0;
        Summedvalues=new double[]{0,0,0};
        for(int i=1;i<RequiredSize;i++){

            Summedtimestamp=Summedtimestamp+meanFilterList.get(size-i).getTimestamp();
            Summedvalues[0]=Summedvalues[0]+meanFilterList.get(size-i).getX();
            Summedvalues[1]=Summedvalues[1]+meanFilterList.get(size-i).getY();
            Summedvalues[2]=Summedvalues[2]+meanFilterList.get(size-i).getZ();
            count++;
        }

        meanValue=new GyroData((Summedtimestamp/count),(Summedvalues[0]/count),(Summedvalues[1]/count),(Summedvalues[2]/count));
        System.out.println("the x y and z after mean averaging are"+meanValue.getX()+"  "+meanValue.getY()+"  "+meanValue.getZ());
    }

    public GyroData getDataPoint() {
        return meanValue;
    }
}
