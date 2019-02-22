package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


import com.example.bryantyrrell.vdiapp.Chat.ChatMessage;
import com.example.bryantyrrell.vdiapp.R;
import com.example.bryantyrrell.vdiapp.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PairingActivity extends AppCompatActivity {
    private static final String TAG = "";
    private ArrayList<Users> UserList = new ArrayList<>();
    private int count=0;
    private FirebaseFirestore db;
    private FirebaseAuth Auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pairing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Auth=FirebaseAuth.getInstance();


        //UserList = UserObjectRef.getUserObject().getUserNamesList();
        // get userNames
        getUserNames();

    }

    private void getUserNames() {
        db = FirebaseFirestore.getInstance();
        CollectionReference colRef = db.collection("UserObjectList");


        //colRef.orderBy("messageTime", Query.Direction.DESCENDING);
        colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                for (QueryDocumentSnapshot doc : value) {

                    Users user = doc.toObject(Users.class);
                    System.out.println("We did it! "+user.getFirstName());
                    UserList.add(user);

                }

                getAcceptedFriendsList();

            }
        });
    }

    private void getAcceptedFriendsList() {
            final ArrayList<Users> AcceptedFriendsList = new ArrayList<>();
            CollectionReference colRef = db.collection("users").document(Auth.getCurrentUser().getEmail()+Auth.getCurrentUser().getUid()).collection("FriendsList");
            CollectionReference colRef1 = db.collection("users").document(Auth.getCurrentUser().getEmail()+Auth.getCurrentUser().getUid()).collection("PendingFriendRequest");
            colRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot doc : value) {

                    Users user = doc.toObject(Users.class);
                    AcceptedFriendsList.add(user);
                }

            }
        });

        colRef1.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                for (QueryDocumentSnapshot doc : value) {

                    Users user = doc.toObject(Users.class);
                    AcceptedFriendsList.add(user);
                }
                CompareFriendsList(AcceptedFriendsList);
            }
        });



    }

    private void CompareFriendsList(ArrayList<Users> AcceptedFriendsList) {

        // checks for already accepted changes
        ArrayList<Users> tempList = new ArrayList<>();
         //removes already accepted friends from the list
         for(Users user : UserList){
             //removes instructors from the list
             if(user.getInstructor()==true){
                 tempList.add(user);
             }
             for (Users friend : AcceptedFriendsList){
                if(friend.getUserID().equals(user.getUserID())){
                    tempList.add(user);
                }
            }
        }
        // uses second for loop to remove friends from list
        for(Users Friend : tempList ){
            int index =UserList.indexOf(Friend);
            if(index!=-1) {
                UserList.remove(index);
            }


        }
        printUserNames();
    }


    private void printUserNames(){
            // if loop checks to ensure arraylist not empty
            if(UserList!=null) {
                //loop through all route names
                for (int i = 0; i < UserList.size(); i++) {
                    String RouteName = UserList.get(i).getUserName();
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
                    bt.setText("REQUEST");
                    bt.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            // calls method to start map veiw activity with route name
                            SendRequest(UserList.get(v.getId()));
                            UserList.remove(v.getId());
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
        // starts map activity with past route
        private void SendRequest(Users user) {
            //send request to user to pair
            //Map<String,Users> map = new HashMap<>();
            //map.put(Auth.getCurrentUser().getUid(),new Users(Auth.getCurrentUser().getEmail(),Auth.getCurrentUser().getUid(),Auth.getCurrentUser().getDisplayName(),true));
            db.collection("users").document(user.getUserName()+user.getUserID()).collection("PendingFriendRequest").document(Auth.getCurrentUser().getUid()).set(new Users(Auth.getCurrentUser().getEmail(),Auth.getCurrentUser().getUid(),Auth.getCurrentUser().getDisplayName(),true));
            db.collection("users").document(Auth.getCurrentUser().getEmail()+Auth.getCurrentUser().getUid()).collection("PendingFriendRequest").document(user.getUserID()).set(new Users(user.getUserName(),user.getUserID(),user.getFirstName(),false));
        }
    }


