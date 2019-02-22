package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerClass;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.MaxAccelService;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeService;
import com.example.bryantyrrell.vdiapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
public class MapsActivity extends AppCompatActivity implements LocationListener ,OnMapReadyCallback {
    private GoogleMap mMap;
    private ProgressDialog LocationDialog;
    private Marker markerLocation;
    private DatabaseService databaseUser;
    private ArrayList<LatLng> storedPoints, postProcessedPoints;
    private DirectionsParser directionsParser;
    private ImageButton fabButton;//fab
    private View fabAction1, fabAction2, fabAction3;
    private FabButtons fab;
    private int count = 0;
    private boolean IllegalDriving = false;
    private boolean IllegalSteering = false;
    private boolean IllegalAcceleration = false;
    private String steeringString = "steering";
    private String accelerationString = "acceleration";
    private DrivingBroadcast drivingBroadcastAcceleration;
    private DrivingBroadcast drivingBroadcastSteering;
    private Location Currentlatlng,LastlatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // toolbar set up
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();
        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);



        // fab set up
        final ViewGroup fabContainer = (ViewGroup) findViewById(R.id.fab_container);
        fabButton = (ImageButton) findViewById(R.id.fab);
        fabAction1 = findViewById(R.id.fab_action_1);
        fabAction2 = findViewById(R.id.fab_action_2);
        fabAction3 = findViewById(R.id.fab_action_3);
        // initialises the arraylist to store the gps points
        storedPoints = new ArrayList<>();
        postProcessedPoints = new ArrayList<>();
        //asks the user for permission
        checkLocationPermission();
        // the users database entry is gathered
        InitialiseDataBaseUser();
        // starts harvesting gps locations
        startGettingLocations();
        // Initialise the fab buttons in its own class
        fab = new FabButtons(databaseUser, fabButton, fabContainer, fabAction1, fabAction2, fabAction3, this,this);

    }

    private void registerAccelerationReceiver() {
        drivingBroadcastAcceleration = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {
                IllegalDriving("acceleration");
                directionsParser.DangerousAccelerationLine();
            }
        };
        registerReceiver(drivingBroadcastAcceleration, new IntentFilter("com.vdi.driving.acceleration"));
    }
    //https://medium.com/@anitaa_1990/how-to-update-an-activity-from-background-service-or-a-broadcastreceiver-6dabdb5cef74
    private void registerSteeringReceiver() {
        drivingBroadcastSteering = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {
                 IllegalDriving("steering");
                directionsParser.DangerousSteeringLine();
            }
        };
        registerReceiver(drivingBroadcastSteering, new IntentFilter("com.vdi.driving.steering"));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(drivingBroadcastAcceleration != null) {
        //    unregisterReceiver(drivingBroadcastAcceleration);
        }
        if(drivingBroadcastSteering != null) {
          //  unregisterReceiver(drivingBroadcastAcceleration);
        }
    }














    public void StatTracking() {
        registerAccelerationReceiver();
        registerSteeringReceiver();

        Intent accelService = new Intent(this, AccelerometerClass.class);
        startService(accelService);

        Intent steeringService = new Intent(this, GyroscopeService.class);
        startService(steeringService);
    }




    public void fabAction1(View v) {
        fab.fabAction1(v);
    }

    public void fabAction2(View v) {
        fab.fabAction2(v);
    }

    public void fabAction3(View v) {
        fab.fabAction3(v);

    }

    private void InitialiseDataBaseUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        databaseUser = new DatabaseService(user.getUid(), user.getEmail());
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
        directionsParser = new DirectionsParser(mMap, storedPoints, databaseUser);

    }

    @Override
    public void onLocationChanged(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        if(Currentlatlng!=location&&Currentlatlng!=null){
            LastlatLng = Currentlatlng;
        }
        Currentlatlng = location;

        if(count>=1){
            double speed = 0;
            speed=location.distanceTo(LastlatLng)/((Currentlatlng.getTime()-LastlatLng.getTime())/1000);
//            System.out.println("distance is: "+location.distanceTo(LastlatLng));
//            System.out.println("Manual speed is: "+speed*3.6);
        }
       count++;
        // adds a marker for new gps point
        addMarker(latLng);

        // adds the gps point to an array
        storedPoints.add(latLng);

        // if 2 gps points and go state selected
        if (storedPoints.size() > 1 && fab.getState() == 1) {
            directionsParser.URLstringBuilder();
        }

        float speed = ((location.getSpeed()*3600)/1000);
        System.out.println("The location m/s speed is: "+location.getSpeed());
        System.out.println("The location m/s speed is: "+speed);
        if(location.hasAccuracy()||location.hasSpeedAccuracy()) {
            //System.out.println("The speed accuracy is: " + location.getSpeedAccuracyMetersPerSecond());
            System.out.println("The accuracy is: " + location.getAccuracy());
        }
//        System.out.println("The distance is: "+;
//        System.out.println("The location m/s speed is: "+location.getSpeed());
//        System.out.println("The speed is: "+speed);


    }
    public void SetDangerousDriving(LatLng latLng){
       if(IllegalAcceleration) {
           MarkerOptions markerOptions = new MarkerOptions();
           markerOptions.position(latLng);
           markerOptions.title("dangerous acceleration");
           markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
           if (mMap != null)
               markerLocation = mMap.addMarker(markerOptions);
           IllegalAcceleration=false;
       }
       if(IllegalSteering) {
           MarkerOptions markerOptions = new MarkerOptions();
           markerOptions.position(latLng);
           markerOptions.title("dangerous Steering");
           markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
           if (mMap != null)
               markerLocation = mMap.addMarker(markerOptions);
           IllegalSteering=false;
       }


        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latLng.latitude, latLng.longitude))
                .zoom(16)
                .build();


        if (mMap != null)
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        IllegalDriving=false;
    }

    public void IllegalDriving(String typeOfIncident){
        System.out.println("The acceleration was triggered: "+typeOfIncident);
        IllegalDriving=true;
        if(accelerationString.contains(typeOfIncident)) {
            IllegalAcceleration = true;
        }
        if(steeringString.contains(typeOfIncident)) {
            IllegalSteering = true;
        }
    }


    private void addMarker(LatLng latLng) {
        if (latLng == null) {
            return;
        }
        if (markerLocation != null) {
            markerLocation.remove();
        }
        if(IllegalDriving) {
            SetDangerousDriving(latLng);
        }
        else {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("New Location");
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            if (mMap != null)
                markerLocation = mMap.addMarker(markerOptions);


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(latLng.latitude, latLng.longitude))
                    .zoom(16)
                    .build();
            if (mMap != null)
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

    }


    // Gets the location of the user on demand
//    private void getLocation() {
//        LocationDialog = new ProgressDialog(this);
//        LocationDialog.setMessage("Loading location...");
//        LocationDialog.show();
//
//        if (ActivityCompat.checkSelfPermission(this,
//                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//            Toast.makeText(this, "Permission to access GPS denied", Toast.LENGTH_SHORT).show();
//
//            LocationDialog.dismiss();
//            return;
//        }
//
//        SingleShotLocationProvider.requestSingleUpdate(this,
//                new SingleShotLocationProvider.LocationCallback() {
//                    @Override
//                    public void onNewLocationAvailable(SingleShotLocationProvider.GPSCoordinates location) {
//                        LatLng latLng = new LatLng(location.latitude, location.longitude);
//                        LocationDialog.dismiss();
//                        addMarker(latLng);
//
//                    }
//                });
//    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }


    // asks user for their permission
    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Permission to access GPS")
                        .setMessage("Please allow the app to access you location.")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapsActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        99);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        99);
            }
            return false;
        } else {
            return true;
        }
    }


    private void startGettingLocations() {

        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isGPS = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetwork = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean canGetLocation = true;
        int ALL_PERMISSIONS_RESULT = 101;
        long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;// Distance in meters
        long MIN_TIME_BW_UPDATES = 1000 * 1 * 1;// Time in milliseconds

        ArrayList<String> permissions = new ArrayList<>();
        ArrayList<String> permissionsToRequest;

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);


        //Check if GPS and Network are on, if not asks the user to turn on
        if (!isGPS && !isNetwork) {
            showSettingsAlert();
        } else {
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    canGetLocation = false;
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission not Granted", Toast.LENGTH_SHORT).show();


            return;
        }

        //Starts requesting location updates
        if (canGetLocation) {
            if (isGPS) {
                lm.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            } else if (isNetwork) {
                // from Network Provider

                lm.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

            }
        } else {
            Toast.makeText(this, "Can't get location", Toast.LENGTH_SHORT).show();
        }
    }


    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }


}
