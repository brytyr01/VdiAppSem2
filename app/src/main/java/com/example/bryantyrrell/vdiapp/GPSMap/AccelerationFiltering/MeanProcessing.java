package com.example.bryantyrrell.vdiapp.GPSMap.AccelerationFiltering;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;

import java.util.ArrayList;

public class MeanProcessing  {
    private ArrayList<AccelData> meanFilterList;
    private AccelData meanValue;
    private int count = 0;
    double[] Summedvalues;
    private long Summedtimestamp;

    public MeanProcessing(ArrayList<AccelData> meanFilterList) {
        this.meanFilterList=meanFilterList;

    }

    public void CalculateMean() {
        int size = meanFilterList.size();

        Summedvalues=new double[]{0,0,0};
        for(int i=1;i<4;i++){

            Summedtimestamp=Summedtimestamp+meanFilterList.get(size-i).getTimestamp();
            Summedvalues[0]=Summedvalues[0]+meanFilterList.get(size-i).getX();
            Summedvalues[1]=Summedvalues[1]+meanFilterList.get(size-i).getY();
            Summedvalues[2]=Summedvalues[2]+meanFilterList.get(size-i).getZ();
            count++;
        }
        System.out.println("Count value is: "+count);
        meanValue=new AccelData((Summedtimestamp/3),(Summedvalues[0]/3),(Summedvalues[1]/3),(Summedvalues[2]/3));
    }

    public AccelData getDataPoint() {
        return meanValue;
    }
}
