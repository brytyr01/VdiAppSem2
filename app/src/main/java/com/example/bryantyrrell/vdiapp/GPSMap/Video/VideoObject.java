package com.example.bryantyrrell.vdiapp.GPSMap.Video;

import com.google.firebase.firestore.GeoPoint;

public class VideoObject {
    private String DrivingType;
    private GeoPoint GpsPoint;
    private String VideoFileName;

    public VideoObject(){

    }
    public VideoObject(String DrivingType,GeoPoint GpsPoint,String VideoFileName){
        this.DrivingType=DrivingType;
        this.GpsPoint=GpsPoint;
        this.VideoFileName=VideoFileName;

    }
    public String getDrivingType() {
        return DrivingType;
    }

    public void setDrivingType(String drivingType) {
        DrivingType = drivingType;
    }

    public GeoPoint getGpsPoint() {
        return GpsPoint;
    }

    public void setGpsPoint(GeoPoint gpsPoint) {
        GpsPoint = gpsPoint;
    }

    public String getVideoFileName() {
        return VideoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        VideoFileName = videoFileName;
    }
}
