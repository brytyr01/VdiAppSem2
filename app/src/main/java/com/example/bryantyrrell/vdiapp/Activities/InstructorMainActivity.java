package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.bryantyrrell.vdiapp.R;

public class InstructorMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructor_main);
    }




        public void updateToUserListScreen(View view) {
            Intent intent = new Intent(this, UserActivity.class);
            startActivity(intent);
        }

    public void updateToPairingScreen(View view) {
        Intent intent = new Intent(this, PairingActivity.class);
        startActivity(intent);
    }

    }


