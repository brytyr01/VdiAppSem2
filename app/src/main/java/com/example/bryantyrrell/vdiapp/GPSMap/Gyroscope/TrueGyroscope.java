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
            this.angle = Math.abs(angle);
        }

        public GyroscopeObject calculateTrueGyroscopeVector(){

            //angle = Math.abs(angle);
            double yAccel = (datapoint.getY()) * (Math.cos(Math.toRadians(angle)));
            double zAccel = (datapoint.getZ()) / (Math.sin(Math.toRadians(angle)));

            double result = yAccel + zAccel;
            GyroscopeObject object = new GyroscopeObject(datapoint.getTimestamp(),result);

            return object;
        }

}
