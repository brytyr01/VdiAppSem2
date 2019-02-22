package com.example.bryantyrrell.vdiapp.GPSMap.Speed;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class GpsSpeedService extends Service {
    private DatabaseService databaseUser;

    public GpsSpeedService() {
        setUpDatabase();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void setUpDatabase(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        databaseUser = new DatabaseService(user.getUid(), user.getEmail());

    }
}
