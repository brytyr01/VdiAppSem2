package com.example.bryantyrrell.vdiapp.Activities;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.example.bryantyrrell.vdiapp.Chat.ChatAdapter;
import com.example.bryantyrrell.vdiapp.Chat.ChatMessage;
import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.R;
import com.firebase.ui.database.FirebaseListAdapter;
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

//https://code.tutsplus.com/tutorials/how-to-create-an-android-chat-app-using-firebase--cms-27397
public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "";
    private FirebaseListAdapter<ChatMessage> adapter;
    private ArrayList<ChatMessage> messageList;
    private boolean firstTime = true;
    private String RouteName,UserName,UserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        messageList = new ArrayList<>();

        RouteName = getIntent().getStringExtra("RouteName");
        UserName = getIntent().getStringExtra("UserName");
        UserID = getIntent().getStringExtra("UserID");

        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText input = (EditText)findViewById(R.id.input);
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser user = mAuth.getCurrentUser();
                DatabaseService dbService = new DatabaseService(UserID,UserName,RouteName);
                // Read the input field and push a new instance
                // of ChatMessage to the Firebase database

                dbService.SendChatMessages(new ChatMessage(input.getText().toString(),
                                FirebaseAuth.getInstance().getCurrentUser().getDisplayName()
                                )
                        );

                // Clear the input
                input.setText("");
            }
        });
        displayChatMessages();
    }



    private void displayChatMessages() {
        ListView listOfMessages = (ListView) findViewById(R.id.list_of_messages);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocument = db.collection("users").document(UserName + UserID);


        //final DocumentReference docRef = userDocument.collection("ChatMessages").document("message");
        final CollectionReference colRef = userDocument.collection("ChatMessages").document(RouteName).collection(("Messages"));

        //colRef.orderBy("messageTime", Query.Direction.DESCENDING);
        colRef.orderBy("messageTime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }

                ArrayList<ChatMessage> messageList = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {

                    ChatMessage message = doc.toObject(ChatMessage.class);
                    System.out.println("We did it! "+message.getMessageText());
                    messageList.add(message);
                }
                UpdateMessage(messageList);

            }
        });


//        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot,
//                                @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w(TAG, "Listen failed.", e);
//                    return;
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    Log.d(TAG, "Current data: " + snapshot.getData());
//                    ChatMessage message = snapshot.toObject(ChatMessage.class);
//                    UpdateMessage(message);
//
//                } else {
//                    Log.d(TAG, "Current data: null");
//                }
//            }
       // });
//        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot snapshot,
//                                @Nullable FirebaseFirestoreException e) {
//                if (e != null) {
//                    Log.w(TAG, "Listen failed.", e);
//                    return;
//                }
//
//                if (snapshot != null && snapshot.exists()) {
//                    Log.d(TAG, "Current data: " + snapshot.getData());
//                    ChatMessage message = snapshot.toObject(ChatMessage.class);
//                    UpdateMessage(message);
//
//                } else {
//                    Log.d(TAG, "Current data: null");
//                }
//            }
//        });

    }

  private void  UpdateMessage(ArrayList<ChatMessage> message) {
      ListView listOfMessages = (ListView)findViewById(R.id.list_of_messages);
      int size = message.size();
      if(size!=0&&firstTime==false) {

          messageList.add(message.get(0));
          ChatAdapter adapter = new ChatAdapter(this, messageList);
          listOfMessages.setAdapter(adapter);
      }
      if(firstTime==true){
          for(int i=message.size()-1;i>=0;i--){
          messageList.add(message.get(i));
          }
          ChatAdapter adapter = new ChatAdapter(this, messageList);
          listOfMessages.setAdapter(adapter);
          firstTime=false;

      }

        //messageList.clear();

     }
}
