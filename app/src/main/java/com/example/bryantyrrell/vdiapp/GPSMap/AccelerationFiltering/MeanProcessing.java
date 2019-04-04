package com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;

import java.util.ArrayList;

public class MeanProcessing  {
    private ArrayList<AccelData> meanFilterList;
    private AccelData meanValue;
    private int count;
    double[] Summedvalues;
    private int RequiredSize=20;
    private long Summedtimestamp;

    public MeanProcessing(ArrayList<AccelData> meanFilterList) {
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

        meanValue=new AccelData((Summedtimestamp/count),(Summedvalues[0]/count),(Summedvalues[1]/count),(Summedvalues[2]/count));
        System.out.println("the x y and z after mean averaging are"+meanValue.getX()+"  "+meanValue.getY()+"  "+meanValue.getZ());
    }

    public AccelData getDataPoint() {
        return meanValue;
    }
}
