package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;

import com.google.firebase.firestore.GeoPoint;


public class GPSPoint {
        private String DrivingType;
        private GeoPoint GpsPoint;
        private int name;

        public GPSPoint(int name, String DrivingType, GeoPoint gpsPoint) {
            this.name = name;
            this.DrivingType = DrivingType;
            this.GpsPoint=gpsPoint;

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
}


