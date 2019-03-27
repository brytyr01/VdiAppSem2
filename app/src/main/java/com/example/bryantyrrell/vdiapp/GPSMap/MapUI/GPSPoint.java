package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;

import com.google.firebase.firestore.GeoPoint;

public class GPSPoint {
        private String DrivingType;
        private GeoPoint GpsPoint;
        private int name;
        private long TimeStamp;

        public GPSPoint(int name, String DrivingType, GeoPoint gpsPoint,long TimeStamp) {
            this.name = name;
            this.DrivingType = DrivingType;
            this.GpsPoint=gpsPoint;
            this.TimeStamp=TimeStamp;

        }
        public GPSPoint(){
        }

        public String getDrivingType() {
            return DrivingType;
        }

        public void setDrivingType(String DrivingType) {
            this.DrivingType = DrivingType;
        }

        public int getname() {
            return name;
        }

        public void setname(int name) {
            this.name = name;
        }

        public GeoPoint getGpsPoint() {
            return GpsPoint;
        }

        public void setGpsPoint(GeoPoint GpsPoint) {
            this.GpsPoint = GpsPoint;
        }

        public long getTimeStamp() {
        return TimeStamp;
    }

       public void setTimeStamp(long TimeStamp) {
        this.TimeStamp = TimeStamp;
    }
}


