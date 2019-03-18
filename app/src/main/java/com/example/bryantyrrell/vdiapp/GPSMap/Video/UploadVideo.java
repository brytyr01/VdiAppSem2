package com.example.bryantyrrell.vdiapp.GPSMap.Video;

import android.util.Log;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.firebase.firestore.DocumentReference;

import static com.firebase.ui.auth.ui.phone.SubmitConfirmationCodeFragment.TAG;

public class UploadVideo {

    private DatabaseService databaseService;
    DocumentReference userDoc;


    UploadVideo(DatabaseService databaseService){
        this.databaseService=databaseService;
    }

    public void setUpVideoUpload(){
        userDoc = databaseService.getUserDocument();
    }


}
