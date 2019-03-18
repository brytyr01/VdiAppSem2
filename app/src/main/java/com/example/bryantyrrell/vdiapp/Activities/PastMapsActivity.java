package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.example.bryantyrrell.vdiapp.Chat.ChatMessage;
import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.GPSPoint;
import com.example.bryantyrrell.vdiapp.GPSMap.Video.VideoObject;
import com.example.bryantyrrell.vdiapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import javax.annotation.Nullable;

public class PastMapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    private String routeName,UserName,UserID;
    Marker marker;
    private ArrayList<VideoObject> MarkerList = new ArrayList<>();
    private ArrayList<GPSPoint> GPSList;
    LatLng LtLngPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        routeName = getIntent().getStringExtra("RouteName");
        UserName = getIntent().getStringExtra("UserName");
        UserID = getIntent().getStringExtra("UserID");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera


        getGPSPoints();
    }

    private void getGPSPoints() {
        //gets database reference
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        DatabaseService DatabaseAccessor = new DatabaseService(UserID, UserName,this);

        DocumentReference userDocument = DatabaseAccessor.getUserDocument();

        //gets gps points in geoPoint format
        final CollectionReference colRef = userDocument.collection("GPS_Location").document(routeName).collection("GPS_Pings").document("Processed_GPS_Pings").collection("GPSObjects");


        colRef.orderBy("name", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                GPSList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {

                    GPSPoint gpsPoint = doc.toObject(GPSPoint.class);
                    System.out.println("We did it! "+gpsPoint.getname());
                    GPSList.add(gpsPoint);
                }


                    RedrawLine(GPSList);

            }
        });

        final CollectionReference colRefMarkersVideo = userDocument.collection("GPS_Location").document(routeName).collection("GPS_Pings").document("Video_location_Pings").collection("VideoObjects");


        colRefMarkersVideo.orderBy("drivingType", Query.Direction.ASCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {


            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                GPSList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {

                    VideoObject videoObject = doc.toObject(VideoObject.class);
                    System.out.println("We did it! "+videoObject.getVideoFileName());
                    MarkerList.add(videoObject);
                }


                if(MarkerList.size()>0)
                    AddMarkers(MarkerList);

            }
        });
    }

    private void AddMarkers(ArrayList<VideoObject> markerList) {
        mMap.setOnMarkerClickListener(this);
        for (VideoObject obj : markerList) {
            String drivingtype = obj.getDrivingType();
            LatLng gpsPoint = new LatLng(obj.getGpsPoint().getLatitude(), obj.getGpsPoint().getLongitude());


            if (drivingtype.contains("acceleration")) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(gpsPoint);
                markerOptions.title(obj.getVideoFileName());
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                if (mMap != null)
                    mMap.addMarker(markerOptions);
            }
            if (drivingtype.contains(obj.getVideoFileName())) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(gpsPoint);
                markerOptions.title("dangerous Steering");
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                if (mMap != null)
                     marker = mMap.addMarker(markerOptions);

            }
        }
    }


    private void RedrawLine(ArrayList<GPSPoint> GPSList) {
        PolylineOptions optionsSafe = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        PolylineOptions optionsAcceleration = new PolylineOptions().width(8).color(Color.RED).geodesic(true);
        PolylineOptions optionsSteering = new PolylineOptions().width(8).color(Color.CYAN).geodesic(true);

        for (int i = 0; i < GPSList.size(); i++) {

            LtLngPoint = new LatLng(GPSList.get(i).getGpsPoint().getLatitude(),GPSList.get(i).getGpsPoint().getLongitude());

            if (i == 0) {
                mMap.addMarker(new MarkerOptions().position(LtLngPoint).title("Start of route"));
                //zoom to route size
                zoomToRoute();
            }
            if(GPSList.get(i).getDrivingType().contains("safe")) {
                optionsSafe.add(LtLngPoint);
            }
            if(GPSList.get(i).getDrivingType().contains("acceleration")) {
                optionsAcceleration.add(LtLngPoint);
            }
            if(GPSList.get(i).getDrivingType().contains("steering")) {
                optionsSteering.add(LtLngPoint);
            }
        }
        // adds marker to end of route
        mMap.addMarker(new MarkerOptions().position(LtLngPoint).title("End of route"));

        //add Marker in current position
        mMap.addPolyline(optionsSafe); //add Polyline
        mMap.addPolyline(optionsAcceleration); //add Polyline
        mMap.addPolyline(optionsSteering); //add Polyline






    }



    private void zoomToRoute() {

        if (mMap == null|| GPSList==null || GPSList.isEmpty()) return;

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (GPSPoint latLngPoint :  GPSList) {
            LatLng latLngConvertedPoint=new LatLng(latLngPoint.getGpsPoint().getLatitude(),latLngPoint.getGpsPoint().getLongitude());
            boundsBuilder.include(latLngConvertedPoint);
        }

        int routePadding = 100;
        LatLngBounds latLngBounds = boundsBuilder.build();

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, routePadding));

    }
    // starts map activity with past route
    public void StartChatActivity(View view) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("RouteName",routeName);
        intent.putExtra("UserName",UserName);
        intent.putExtra("UserID",UserID);
        startActivity(intent);
    }
    // starts map activity with past route
//    public void StartStaticticsActivity(View view) {
//        Intent intent = new Intent(this, PastMapsActivity.class);
//        startActivity(intent);
//    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        System.out.println("This marker was clicked: "+marker.getTitle());
        Intent intent = new Intent(this,VideoPlayerActivity.class);
        intent.putExtra("VideoName",marker.getTitle());
        intent.putExtra("UserID",UserID);
        intent.putExtra("RouteName",routeName);
        startActivity(intent);
        return true;
    }
}



