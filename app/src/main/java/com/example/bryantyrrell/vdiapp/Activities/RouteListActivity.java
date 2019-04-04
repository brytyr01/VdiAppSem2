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
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RouteListActivity extends AppCompatActivity {
    private int count = 0;
    private ArrayList<String> routeNames;
    private DatabaseService routeList;
    private String UserName,UserID;
    private DocumentReference userDocument;
    private ArrayList<Integer> NumberOfIncidents;
    String RouteName="";
    HashMap<String,Integer> IncidentAmount=new HashMap<>();
    LinkedHashMap<String,Integer> OrderedIncidentAmount=new LinkedHashMap<>();

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
        userDocument = routeList.getUserDocument();
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
    private void printRouteNames() {
        int size = 0;
        // if loop checks to ensure arraylist not empty
        if (routeNames != null) {
            //loop through all route names
            for (int i = 0; i < routeNames.size(); i++) {
                RouteName = routeNames.get(i);
                getIncidentCount(RouteName);

            }
        }
    }
            private void getIncidentCount(final String routeName){


                userDocument.collection("GPS_Location").document(routeName).collection("GPS_Pings").document("Video_location_Pings").collection("VideoObjects").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<DocumentSnapshot> myListOfDocuments = task.getResult().getDocuments();
                            int size;
                            size = myListOfDocuments.size();
                            //addRow(routeName,size);
                            IncidentAmount.put(routeName,size);
                            if(IncidentAmount.size()==routeNames.size()){
                                reOrder();

                            }
                        }
                    }
                });
            }

    private void reOrder() {
        for(String name : routeNames) {

            int number = IncidentAmount.remove(name);
            OrderedIncidentAmount.put(name,number);
            addRow(name,number);
        }

    }

    private void addRow(String RouteName,int incidentCount){
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

                // add textview with route name
                TextView tv2 = new TextView(this);
                tv2.setTextSize(25);

                tv2.setText(String.valueOf(incidentCount));

                newRow.addView(tv);
                newRow.addView(bt);
                newRow.addView(tv2);
                count++;
                tbl.addView(newRow, 2);

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