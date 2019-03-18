package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public class RouteListActivity extends AppCompatActivity {
    private int count = 0;
    private ArrayList<String> routeNames;
    private DatabaseService routeList;
    private String UserName,UserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_list);



        UserName = getIntent().getStringExtra("UserName");
        UserID = getIntent().getStringExtra("UserID");
        //Pass details to database class
        routeList = new DatabaseService(UserID,UserName,this);

        //get and display route names
        getRouteNames();

    }

    public void getRouteNames() {
        //get an instance of the user document
        DocumentReference userDocument = routeList.getUserDocument();
        userDocument.collection("GPS_Location").document("RouteNames").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    routeNames = (ArrayList<String>) document.get("Routes");
                    printRouteNames();

                }
            }
        });
    }
    private void printRouteNames(){
        // if loop checks to ensure arraylist not empty
        if(routeNames!=null) {
            //loop through all route names
            for (int i = 0; i < routeNames.size(); i++) {
                String RouteName = routeNames.get(i);
                // reference the table layout
                TableLayout tbl = findViewById(R.id.tableLayout2);

                // delcare a new row
                TableRow newRow = new TableRow(this);

                // add textview with route name
                TextView tv = new TextView(this);
                tv.setTextSize(25);

                tv.setText(RouteName);

                // add button with listener
                Button bt = new Button(this);
                bt.setId(count);
                bt.setText("GO");
                bt.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // calls method to start map veiw activity with route name
                        StartMapView(routeNames.get(v.getId()));
                    }
                });

                newRow.addView(tv);
                newRow.addView(bt);
                count++;
                tbl.addView(newRow, 2);

            }
        }
    }
    // starts map activity with past route
    private void StartMapView(String RouteName) {
        Intent intent = new Intent(this, PastMapsActivity.class);
        intent.putExtra("RouteName",RouteName);
        intent.putExtra("UserName",UserName);
        intent.putExtra("UserID",UserID);
        startActivity(intent);
    }
}