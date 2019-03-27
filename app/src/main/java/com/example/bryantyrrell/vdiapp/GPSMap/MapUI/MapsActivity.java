package com.example.bryantyrrell.vdiapp.GPSMap.MapUI;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerClass;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeService;
import com.example.bryantyrrell.vdiapp.GPSMap.Video.FileCleanUp;
import com.example.bryantyrrell.vdiapp.GPSMap.Video.Ready;
import com.example.bryantyrrell.vdiapp.GPSMap.Video.SimpleVideoService;
import com.example.bryantyrrell.vdiapp.GPSMap.Video.VideoObject;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends AppCompatActivity implements LocationListener ,OnMapReadyCallback,SurfaceHolder.Callback {
    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static Camera mCamera ;
    public static boolean mPreviewRunning;
    static int countVideo=0;
    public static final int CAMERA_REQUEST = 1;

    private GoogleMap mMap;
    private ProgressDialog LocationDialog;
    private Marker markerLocation;
    private Marker DangerousMarkerLocation;
    private DatabaseService databaseUser;
    private ArrayList<LatLng> PreProcessedGPSPoints, PostProcessedGPSPoints;
    private ArrayList<Marker> DangerousMarkers;
    private DirectionsParser directionsParser;
    private ImageButton fabButton;//fab
    private View fabAction1, fabAction2, fabAction3;
    private FabButtons fab;
    private FileCleanUp fileCleanUp;
    private Intent accelService;
    private Intent steeringService;
    private Timer Videotimer;
    private Timer VideoProcessingtask;
    private int count = 0;
    private boolean IllegalDriving = false;
    private boolean PutExtra=true;
    private boolean IllegalSteering = false;
    private boolean IllegalAcceleration = false;
    private String steeringString = "steering";
    private String accelerationString = "acceleration";
    private DrivingBroadcast drivingBroadcastAcceleration;
    private DrivingBroadcast drivingBroadcastSteering;
    private DrivingBroadcast BroadcastVideoRestarted;
    private boolean BooleanVideoFinished;
    private Location Currentlatlng,LastlatLng;
    long IncidentTime;
    Handler Videohandler;
    Runnable runnable;

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

        ReceivedVideoRestart();


        // fab set up
        final ViewGroup fabContainer = (ViewGroup) findViewById(R.id.fab_container);
        fabButton = (ImageButton) findViewById(R.id.fab);
        fabAction1 = findViewById(R.id.fab_action_1);
        fabAction2 = findViewById(R.id.fab_action_2);
        fabAction3 = findViewById(R.id.fab_action_3);
        // initialises the arraylist to store the gps points
        PreProcessedGPSPoints = new ArrayList<>();
        PostProcessedGPSPoints = new ArrayList<>();
        DangerousMarkers=new ArrayList<>();
        //asks the user for permission
        checkLocationPermission();
        // the users database entry is gathered
        InitialiseDataBaseUser();
        // starts harvesting gps locations
        startGettingLocations();
        // Initialise the fab buttons in its own class
        fab = new FabButtons(databaseUser, fabButton, fabContainer, fabAction1, fabAction2, fabAction3, this,this);

        //video set up
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    private void registerAccelerationReceiver() {
        drivingBroadcastAcceleration = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {
                IllegalDriving("acceleration");
                directionsParser.setDangerousAcceleration();
                LatLng curentPosition = new LatLng(Currentlatlng.getLatitude(),Currentlatlng.getLongitude());
                // adds dangerous driving marker to current position
                SetDangerousDrivingMarker(curentPosition);
                //the incident time is passed by the broadcast intent
                IncidentTime = intent.getLongExtra("incidentTime",-1);
                if(IncidentTime==-1){
                    IncidentTime=System.currentTimeMillis();
                }
                OndemandVideoRestart();
                //VideoObject VideoObject = fileCleanUp.ProcessVideo(DangerousMarkers.get(DangerousMarkers.size()-1),"acceleration",IncidentTime);
                //File video = fileCleanUp.getVideoFile();
                //databaseUser.uploadVideoPoint(VideoObject);

                //databaseUser.uploadFile(video);

                RegisterCoolDownAcceleration();
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
                directionsParser.setDangerousSteering();
                LatLng currentPosition = new LatLng(Currentlatlng.getLatitude(),Currentlatlng.getLongitude());
                // adds dangerous driving marker to current position
                SetDangerousDrivingMarker(currentPosition);
                //the incident time is passed by the broadcast intent
                 IncidentTime = intent.getLongExtra("incidentTime",-1);
                if(IncidentTime==-1){
                    IncidentTime=System.currentTimeMillis();
                }
                OndemandVideoRestart();
                RegisterCoolDownSteering();

            }
        };
        registerReceiver(drivingBroadcastSteering, new IntentFilter("com.vdi.driving.steering"));
    }











    private void RegisterCoolDownAcceleration() {
        unregisterReceiver(drivingBroadcastAcceleration);

        //unregisterReceiver(drivingBroadcastSteering);
        TimerTask coolDownTask = new TimerTask() {
            @Override
            public void run() {
                registerReceiver(drivingBroadcastAcceleration, new IntentFilter("com.vdi.driving.acceleration"));
                //registerReceiver(drivingBroadcastSteering, new IntentFilter("com.vdi.driving.steering"));

                Intent ChangeAlertToTrue = new Intent("com.vdi.driving.driverAlertSystemAcceleration");
                sendBroadcast(ChangeAlertToTrue);
            }
        };
        Timer timer = new Timer();

        // schedules the task to be run after a 20 delay
        timer.schedule(coolDownTask,60000);


    }
    private void RegisterCoolDownSteering() {
        //unregisterReceiver(drivingBroadcastAcceleration);

        unregisterReceiver(drivingBroadcastSteering);
        TimerTask coolDownTask = new TimerTask() {
            @Override
            public void run() {
          //      registerReceiver(drivingBroadcastAcceleration, new IntentFilter("com.vdi.driving.acceleration"));
                registerReceiver(drivingBroadcastSteering, new IntentFilter("com.vdi.driving.steering"));

                Intent ChangeAlertToTrue = new Intent("com.vdi.driving.driverAlertSystemSteering");
                sendBroadcast(ChangeAlertToTrue);
            }
        };
        Timer timer = new Timer();

        // schedules the task to be run after a 20 delay
        timer.schedule(coolDownTask,60000);


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

    private void OndemandVideoRestart(){
//        Intent test1 = new Intent("com.vdi.driving.video");
//        test1.putExtra("StopType",1);
//        sendBroadcast(test1);
        startRecordingVideo();
        PutExtra=true;
        System.out.println("broadcast sent for video stop");
        //((Ready) this.getApplication()).setVideoFinished(false);
    }

    private void ReceivedVideoRestart(){
        BroadcastVideoRestarted = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {
                System.out.println("Broadcast received");
                int videoIndex = intent.getIntExtra("VideoIndex",0);
                System.out.println("The video index is: "+videoIndex);
                fileCleanUp.ProcessVideo(DangerousMarkers.get(DangerousMarkers.size()-1),"steering", IncidentTime,videoIndex);
            }
        };
        registerReceiver(BroadcastVideoRestarted, new IntentFilter("com.vdi.driving.VideoRestarted"));
    }


    private void startRecordingVideo(){
        if(Videotimer!=null){
        Videotimer.cancel();
        Videotimer.purge();
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(countVideo==0) {
                    Intent intent = new Intent(MapsActivity.this, SimpleVideoService.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(intent);
                    countVideo++;
                }
                if(!PutExtra){
                    Intent test1 = new Intent("com.vdi.driving.video");
                    sendBroadcast(test1);
                    System.out.println("broadcast sent");
                }else{
                    PutExtra=false;
                    Intent test1 = new Intent("com.vdi.driving.video");
                    test1.putExtra("StopType",1);
                    sendBroadcast(test1);
                    System.out.println("broadcast sent");
                }
            }
        };
        Videotimer = new Timer();
        long delayBetweenTasks = 45000;

        // schedules the task to be run after a 15 delay
        Videotimer.scheduleAtFixedRate(task,0, delayBetweenTasks);

    }


    public void stopTracking() {
        accelService = new Intent(this, AccelerometerClass.class);
        stopService(accelService);

        steeringService = new Intent(this, GyroscopeService.class);
        stopService(steeringService);
        Videotimer.cancel();
        Videotimer.purge();
    }


    public void StartTracking() {
        fileCleanUp = new FileCleanUp(this,databaseUser);

        startRecordingVideo();

        registerAccelerationReceiver();
        registerSteeringReceiver();

        accelService = new Intent(this, AccelerometerClass.class);
        startService(accelService);

        steeringService = new Intent(this, GyroscopeService.class);
        startService(steeringService);


    }




    public void fabAction1(View v) {
        fab.fabAction1(v);
    }

    public void fabAction2(View v) {
        fab.fabAction2(v);
    }

    public void fabAction3(View v) { fab.fabAction3(v); }

    private void InitialiseDataBaseUser() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        databaseUser = new DatabaseService(user.getUid(), user.getEmail(),this);
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        directionsParser = new DirectionsParser(mMap, PreProcessedGPSPoints, databaseUser);

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
        PreProcessedGPSPoints.add(latLng);

        // if 2 gps points and go state selected
        if (PreProcessedGPSPoints.size() > 1 && fab.getState() == 1) {
            directionsParser.URLstringBuilder();
        }

        float speed = ((location.getSpeed()*3600)/1000);
        System.out.println("The location m/s speed is: "+location.getSpeed());
        System.out.println("The location m/s speed is: "+speed);
        if(location.hasAccuracy()) {
            //System.out.println("The speed accuracy is: " + location.getSpeedAccuracyMetersPerSecond());
            System.out.println("The accuracy is: " + location.getAccuracy());
        }
//        System.out.println("The distance is: "+;
//        System.out.println("The location m/s speed is: "+location.getSpeed());
//        System.out.println("The speed is: "+speed);


    }
    public void SetDangerousDrivingMarker(LatLng latLng){
       if(IllegalAcceleration) {
           MarkerOptions markerOptions = new MarkerOptions();
           markerOptions.position(latLng);
           markerOptions.title("dangerous acceleration");
           markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
           if (mMap != null)
               DangerousMarkerLocation = mMap.addMarker(markerOptions);
           DangerousMarkers.add(DangerousMarkerLocation);
           IllegalAcceleration=false;
       }
       if(IllegalSteering) {
           MarkerOptions markerOptions = new MarkerOptions();
           markerOptions.position(latLng);
           markerOptions.title("dangerous Steering");
           markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
           if (mMap != null)
               DangerousMarkerLocation = mMap.addMarker(markerOptions);
           DangerousMarkers.add(DangerousMarkerLocation);
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


        return;
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
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }


}
