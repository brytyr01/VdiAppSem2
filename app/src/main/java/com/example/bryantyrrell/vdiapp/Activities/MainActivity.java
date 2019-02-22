package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.bryantyrrell.vdiapp.Activities.RegisterPageActivity;
import com.example.bryantyrrell.vdiapp.R;

public class MainActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        runRegisterActivity();

    }

    private void runRegisterActivity(){
        Intent intent = new Intent(this, RegisterPageActivity.class);
        startActivity(intent);
    }

}
