package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.bryantyrrell.vdiapp.GPSMap.Acceleration.AccelerometerActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.Gyroscope.GyroscopeActivity;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.example.bryantyrrell.vdiapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomePageActivity extends AppCompatActivity {

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
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

}
