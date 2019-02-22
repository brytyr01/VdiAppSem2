package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;

import com.google.firebase.firestore.GeoPoint;

import java.util.HashMap;

public class GEOPoint {

        private String username;
        private HashMap<String, GeoPoint> geoPoint;
    GeoPoint geoPoint1;
    HashMap<String, Object> geoPoint2;

    public GEOPoint() {}
       // public GEOPoint(String username, HashMap<String, GeoPoint> geoPoint) {
       //   this.username = username;
       //  this.geoPoint = geoPoint;
       //}

    public GEOPoint(String username, HashMap<String, Object> geoPoint2) {
        this.username = username;
        this.geoPoint2 = geoPoint2;
    }
    public GEOPoint(String username,  GeoPoint geoPoint) {
        this.username = username;
        this.geoPoint1 = geoPoint;
    }
    public GEOPoint(GeoPoint geoPoint) {
        this.username = username;
        this.geoPoint1 = geoPoint;
    }
    public GEOPoint(Object geoPoint) {
        Object username34 = geoPoint;

    }
    public GEOPoint(HashMap<String, GeoPoint> geoPoint) {

        this.geoPoint = geoPoint;
    }
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public HashMap<String, GeoPoint> getGEOPoint() {
            return geoPoint;
        }
    public HashMap<String, GeoPoint> setgeoPoint() {
        return geoPoint;
    }

        public void setGEOPoint(HashMap<String, GeoPoint> geoPoint) {
            this.geoPoint = geoPoint;
        }
    public void getGEOPoint(HashMap<String, GeoPoint> geoPoint) {
        this.geoPoint = geoPoint;
    }
    }

