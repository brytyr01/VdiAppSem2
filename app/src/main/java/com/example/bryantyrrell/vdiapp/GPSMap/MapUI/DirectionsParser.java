package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;
import android.graphics.Color;
import android.os.AsyncTask;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.firestore.GeoPoint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

// this class takes the map and geo points and draws a road line
//The Roads API identifies the roads a vehicle was traveling along
//and provides additional metadata about those roads, such as speed limits.

public class DirectionsParser {
    private GoogleMap mMap;
    private ArrayList<LatLng> preProcessedPoints;
    private ArrayList<GPSPoint> postProcessedPoints;
    private ArrayList<String> LocationID;
    private BufferedReader reader;
    private String JSONFile;
    //private boolean result = false;
    private DatabaseService databaseService;
    private ArrayList<Polyline> lines = new ArrayList<>();
    private boolean polylinedrawn=false;


    public DirectionsParser(GoogleMap mMap, ArrayList<LatLng> storedPoints, DatabaseService databaseUser) {
        this.mMap = mMap;
        this.preProcessedPoints = storedPoints;
        postProcessedPoints=new ArrayList<>();
        LocationID=new ArrayList<>();
        databaseService=databaseUser;
    }


    public void URLstringBuilder() {
        //Creates a string in the form of http request for google api
        StringBuilder path = new StringBuilder();
        StringBuilder Speedpath = new StringBuilder();
        path.append("https://roads.googleapis.com/v1/snapToRoads?path=");
        Speedpath.append("https://roads.googleapis.com/v1/speedLimits?path=");
        // for loop iterates through all gps points and appends them as a string
        for (int i = 0; i < preProcessedPoints.size(); i++) {
            LatLng gpsPoint = preProcessedPoints.get(i);
            if (i == (preProcessedPoints.size() - 1)) {
                path.append(gpsPoint.latitude + "," + gpsPoint.longitude + "&");
                Speedpath.append(gpsPoint.latitude + "," + gpsPoint.longitude + "&");
            } else {
                path.append(gpsPoint.latitude + "," + gpsPoint.longitude + "|");
                Speedpath.append(gpsPoint.latitude + "," + gpsPoint.longitude + "|");
            }
        }
        path.append("interpolate=true&");
        path.append("key=AIzaSyBIXmHTXWs1LfOc7E6ERSGMQRWd4sA6swM");

        Speedpath.append("key=AIzaSyBu3T_2-eHuWB216xOnv6Dew2oEpnjVB28");

        // calls snap to route api from google in inner private class
        SnapToRoadAPICall SnapToRoad = new SnapToRoadAPICall();
        SnapToRoad.execute(path.toString());

    }



    private class SnapToRoadAPICall extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            //https://stackoverflow.com/questions/2938502/sending-post-data-in-android
            StringBuilder jsonStringBuilder= new StringBuilder();
            // Making HTTP request
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    reader = new BufferedReader(new InputStreamReader(in));
                    StringBuffer buffer = new StringBuffer();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                        jsonStringBuilder.append(line);
                        jsonStringBuilder.append("\n");
                    }
                    //System.out.println(jsonStringBuilder.toString());
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //JSONFile=jsonStringBuilder.toString();
            return jsonStringBuilder.toString();
        }

        @Override
        protected void onPostExecute(String GeoResult) {
            JSONFile=GeoResult;
            ParseJsonResult();
        }

    }

    private void ParseJsonResult() {
        try {
            JSONObject obj = new JSONObject(JSONFile);
            JSONArray geodata = obj.getJSONArray("snappedPoints");
            final int n = geodata.length();
            for (int i = 0; i < n; ++i) {
                JSONObject geopoint = geodata.getJSONObject(i);
                if (geopoint.has("location")) {
                    JSONObject obje = geopoint.getJSONObject("location");
                    LatLng point = new LatLng(obje.getDouble("latitude"), obje.getDouble("longitude"));
                    GeoPoint geoPoint= new GeoPoint(point.latitude,point.longitude);
                    GPSPoint gpsPoint = new GPSPoint(0,"safe",geoPoint);
                    postProcessedPoints.add(gpsPoint);
                }
               if (geopoint.has("placeId")) {
                    String SingleLocationID = new String(geopoint.getString("placeId"));
                    LocationID.add(SingleLocationID);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RedrawLine();

    }

    private void RedrawLine() {
        PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);

        for (int i = 0; i < postProcessedPoints.size(); i++) {
            LatLng point = new LatLng(postProcessedPoints.get(i).getGpsPoint().getLatitude(),postProcessedPoints.get(i).getGpsPoint().getLongitude());
            options.add(point);
        }
        //add polyline in current position
        Polyline line = mMap.addPolyline(options); //add Polyline
        lines.add(line);
        // takes old gps points out of the array and sends them to the database class to be uploaded
        RemoveGPSPoints();
    }
    public void DangerousSteeringLine() {
        if(!polylinedrawn) {
            if(lines!=null&&!lines.isEmpty()){
            Polyline lastLine = lines.get(lines.size() - 1);
                lastLine.remove();
            }

            PolylineOptions options = new PolylineOptions().width(8).color(Color.RED).geodesic(true);

                for (int i = 0; i < postProcessedPoints.size(); i++) {
                    LatLng point = new LatLng(postProcessedPoints.get(i).getGpsPoint().getLatitude(), postProcessedPoints.get(i).getGpsPoint().getLongitude());
                    postProcessedPoints.get(i).setDrivingType("steering");
                    options.add(point);
                }

            //add polyline in current position

            mMap.addPolyline(options); //add Polyline

            polylinedrawn = true;
        }
        // takes old gps points out of the array and sends them to the database class to be uploaded
        //RemoveGPSPoints();
    }
    public void DangerousAccelerationLine() {
        if (!polylinedrawn) {
            if (lines != null && !lines.isEmpty()) {
                Polyline lastLine = lines.get(lines.size() - 1);
                lastLine.remove();
            }
            PolylineOptions options = new PolylineOptions().width(8).color(Color.RED).geodesic(true);

            for (int i = 0; i < postProcessedPoints.size(); i++) {
                LatLng point = new LatLng(postProcessedPoints.get(i).getGpsPoint().getLatitude(), postProcessedPoints.get(i).getGpsPoint().getLongitude());
                postProcessedPoints.get(i).setDrivingType("acceleration");
                options.add(point);
            }

            //add polyline in current position
            mMap.addPolyline(options); //add Polyline

            polylinedrawn = true;
        }
    }

    // takes old gps points out of the array and sends them to the database class to be uploaded
    private void RemoveGPSPoints() {
        // leaves 1 point for accuracy
        for(int i=0;i<(preProcessedPoints.size()-1);i++) {

           databaseService.addPreGpsPoint(preProcessedPoints.remove(i));

       }
        for(int i=0;i<(postProcessedPoints.size());i++) {

            databaseService.addPostGpsPoint(postProcessedPoints.remove(i));
        }
        for(int i=0;i<(LocationID.size());i++) {

            databaseService.addLocationPoint(LocationID.remove(i));
        }
        // clears all the points from the processed arraylist
        LocationID.clear();
        polylinedrawn=false;
    }


}