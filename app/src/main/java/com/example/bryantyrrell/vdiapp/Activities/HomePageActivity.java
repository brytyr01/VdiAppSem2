package com.example.bryantyrrell.vdiapp.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Video.VideoTOPActivity;
import com.example.bryantyrrell.vdiapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.example.bryantyrrell.vdiapp.GPSMap.Video.VideoTOPActivity.CAMERA_REQUEST;

public class HomePageActivity extends AppCompatActivity {


    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    public static final int NOTIFICATION_REQUEST = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
    }

    public void updateToMapsScreen(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }


    public void updateToRouteListScreen(View view) {
        Intent intent = new Intent(this, RouteListActivity.class);
        // get user details for database reference
        intent.putExtra("UserName",user.getEmail());
        intent.putExtra("UserID", user.getUid());
        startActivity(intent);
    }

    public void updateToFriendRequestScreen(View view) {
        Intent intent = new Intent(this, FriendRequestActivity.class);
        intent.putExtra("UserName",user.getEmail());
        intent.putExtra("UserID", user.getUid());
        startActivity(intent);
    }
    public void updateToAccelScreen(View view) {
        Intent intent = new Intent(this, AccelerometerActivity.class);
        startActivity(intent);
    }
    public void updateToGyroScreen(View view) {
        Intent intent = new Intent(this, GyroscopeActivity.class);
        startActivity(intent);
    }

    public void updateToVideoScreen(View view) {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)== PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, CAMERA_REQUEST);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_REQUEST);
        else {
            Intent intent = new Intent(this, VideoTOPActivity.class);
            startActivity(intent);
        }
    }

}
