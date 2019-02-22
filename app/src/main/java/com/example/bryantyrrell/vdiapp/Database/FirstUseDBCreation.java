package com.example.bryantyrrell.vdiapp.Database;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirstUseDBCreation {

    private String userID,userName;
    private String users="users";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FirstUseDBCreation(String uid, String userName) {

        userID=uid;
        this.userName=userName;
        createUserDocuments();
    }

    private void createUserDocuments() {
        // cretaes route list document
        Map<String, ArrayList<String>> FirstRoute = new HashMap<>();
        ArrayList<String> RoutesArray=new ArrayList<>();
        FirstRoute.put("Routes", RoutesArray);



        DocumentReference userDocument = db.collection(users).document((userName+userID));
         userDocument.collection("GPS_Location").document("RouteNames").set(FirstRoute);
    }

}
