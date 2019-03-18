package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.R;
import com.example.bryantyrrell.vdiapp.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = "";
    private int count = 0;
        private ArrayList<Users> UserNames;
        private static  ArrayList<Users> AcceptedUsers = new ArrayList<>();
        private DatabaseService routeList;
    private FirebaseFirestore db;
        private FirebaseUser user;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_route_list);

            // get user details for database reference
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            user = mAuth.getCurrentUser();

            //Pass details to database class
            routeList = new DatabaseService(user.getUid(),user.getEmail(),this);

            //get and display route names
            getUserNames();

        }
        // references a list of accepted usernames
        public void getUserNames() {
            db = FirebaseFirestore.getInstance();
            CollectionReference colRef = db.collection("users").document(user.getEmail()+user.getUid()).collection("FriendsList");
            colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }
                    AcceptedUsers.clear();
                    ArrayList<Users> FriendsList = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : value) {

                        Users user = doc.toObject(Users.class);
                        AcceptedUsers.add(user);
                    }

                    if(AcceptedUsers.size()!=0){
                        printUserNames();

                    }
                }
            });






        }
        private void printUserNames(){
            // if loop checks to ensure arraylist not empty
            if(AcceptedUsers!=null) {
                //System.out.println("The username is: "+UserNames.get(0).getUserName());
                //loop through all route names
                for (int i = 0; i < AcceptedUsers.size(); i++) {
                    String RouteName = AcceptedUsers.get(i).getUserName();

                    // reference the table layout
                    TableLayout tbl = findViewById(R.id.tableLayout2);

                    // delcare a new row
                    TableRow newRow = new TableRow(this);

                    // add textview with route name
                    TextView tv = new TextView(this);
                    tv.setTextSize(35);
                    tv.setText(RouteName);

                    // add button with listener
                    Button bt = new Button(this);
                    bt.setId(count);
                    bt.setText("GO");
                    bt.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // calls method to start map veiw activity with route name
                            StartMapView(AcceptedUsers.get(v.getId()));
                        }
                    });

                    newRow.addView(tv);
                    newRow.addView(bt);
                    count++;
                    tbl.addView(newRow, 2);

                }
            }else{System.out.println("Username was null");}
        }
        // starts map activity with past route
        private void StartMapView(Users user) {
            Intent intent = new Intent(this, RouteListActivity.class);
            System.out.println("Username: "+user.getUserName());
            intent.putExtra("UserName",user.getUserName());
            intent.putExtra("UserID", user.getUserID());
            startActivity(intent);
        }

//    public void UpdateAcceptedUsers(Users user) {
//
//
//            AcceptedUsers.add(user);
//
//    }
    }
