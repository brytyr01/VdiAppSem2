package com.example.bryantyrrell.vdiapp.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.R;
import com.example.bryantyrrell.vdiapp.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    private DatabaseService dbref;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
    }

    public void getcreds(View view){

        EditText usernameBox = findViewById(R.id.UserName);
        EditText passwordBox = findViewById(R.id.Password);
        String username = usernameBox.getText().toString();
        String password = passwordBox.getText().toString();

        signIn(username, password);

    }




    private void signIn(String email, String password){

        // set up a listener to check email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            user = mAuth.getCurrentUser();
                            FirebaseFirestore.getInstance().collection("UserObjectList").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value,
                                                    @Nullable FirebaseFirestoreException e) {
                                    if (e != null) {
                                        Log.w(TAG, "Listen failed.", e);
                                        return;
                                    }
                                    Boolean instructor=false;
                                    for (QueryDocumentSnapshot doc : value) {

                                        Users userObject = doc.toObject(Users.class);
                                        System.out.println("Instrucor before for loop "+userObject.getInstructor());
                                        if(userObject.getUserID().equals(user.getUid())){
                                            instructor=userObject.getInstructor();

                                            System.out.println("Instrucor in for loop "+instructor);
                                        }
                                    }
                                    System.out.println("Instrucor after for loop "+instructor);
                                    if(instructor==true){
                                        updateUIInstructor();
                                    }
                                    else{
                                        updateUIStudent();
                                    }
                                }
                            });

                            //
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    private void updateUIStudent() {
        // change to home page
        Intent intent = new Intent(this, HomePageActivity.class);
        startActivity(intent);
    }
    private void updateUIInstructor() {
        // change to home page
        Intent intent = new Intent(this, InstructorMainActivity.class);
        startActivity(intent);
    }

    public void updateToRegisterScreen(View view) {
        Intent intent = new Intent(this, RegisterPageActivity.class);
        startActivity(intent);
    }

}
