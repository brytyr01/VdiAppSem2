package com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelData;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerationObject;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

public class TrueGyroscope {
        private GyroData datapoint;
        private double angle;

        public TrueGyroscope(GyroData datapoint, double angle) {
            this.datapoint = datapoint;
            this.angle = angle;
        }

        public GyroscopeObject calculateTrueGyroscopeVector(){
            angle = Math.abs(angle);
            double xAccel=datapoint.getX();
            double yAccel=datapoint.getY();
            double zAccel=datapoint.getZ();
            double result=xAccel+yAccel+zAccel;
            GyroscopeObject object = new GyroscopeObject(datapoint.getTimestamp(),result);

            return object;
        }

}
