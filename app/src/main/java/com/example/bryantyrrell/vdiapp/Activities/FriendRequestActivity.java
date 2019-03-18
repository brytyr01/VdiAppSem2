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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FriendRequestActivity extends AppCompatActivity {

    private static final String TAG ="" ;
    private String UserName,UserID;
    private DatabaseService dbref;
    private ArrayList<Users> Instructors= new ArrayList<>();
    private int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        UserName = getIntent().getStringExtra("UserName");
        UserID = getIntent().getStringExtra("UserID");

        //Pass details to database class
        dbref = new DatabaseService(UserID,UserName,this);

        //get and display route names
        getFriendRequests();

    }

    public void getFriendRequests() {
        //get an instance of the user document
        DocumentReference userDocument = dbref.getUserDocument();
        userDocument.collection("PendingFriendRequest").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {

                    Users user = doc.toObject(Users.class);
                    Instructors.add(user);
                }
                printFriendRequestNames();
            }
        });
    }
    private void printFriendRequestNames(){
        // if loop checks to ensure arraylist not empty
        if(Instructors!=null) {
            //loop through all route names
            for (int i = 0; i < Instructors.size(); i++) {
                String RouteName = Instructors.get(i).getUserName();
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
                bt.setText("ACCEPT");
                bt.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        // calls method to start map veiw activity with route name
                        AcceptRequest(Instructors.get(v.getId()));
                        //restart activity
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });

                newRow.addView(tv);
                newRow.addView(bt);
                count++;
                tbl.addView(newRow, 2);

            }
        }
    }

    private void AcceptRequest(Users user) {
        //accept requests

        dbref.AcceptFriendRequest(user);
        dbref.removePendingFriend(user);
    }
}