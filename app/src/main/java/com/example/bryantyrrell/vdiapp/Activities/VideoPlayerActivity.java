package com.example.bryantyrrell.vdiapp.Activities;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.bryantyrrell.vdiapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.File;

public class VideoPlayerActivity extends Activity {

    private File localFile = null;
    private VideoView videoView;
    private MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        videoView = (VideoView) findViewById(R.id.videoView1);

        //Creating MediaController
         mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        String VideoName = getIntent().getStringExtra("VideoName");
        String UserID = getIntent().getStringExtra("UserID");
        String RouteName = getIntent().getStringExtra("RouteName");
        GetVideoFile(RouteName, VideoName, UserID);

    }

    private void GetVideoFile(String routeName, String VideoName, String userID) {

        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference islandRef = storage.child("users").child(userID).child(routeName).child(VideoName);
        System.out.println(islandRef.toString());
        File rootPath = new File(Environment.getExternalStorageDirectory(), "file_name");
        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        localFile = new File(this.getExternalCacheDir() + "/videoFiles/", "VideoToBePlayed.mp4");

        islandRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Log.e("firebase ", ";local tem file created  created " + localFile.toString());
                //  updateDb(timestamp,localFile.toString(),position);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e("firebase ", ";local tem file not created  created " + exception.toString());
            }
        }).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    fileDownloaded();
                } else {
                    // Handle failures
                    // ...
                }
            }
        });

    }

    private void fileDownloaded(){
        Uri uri = Uri.parse(localFile.getAbsolutePath());

        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController);
        videoView.setVideoURI(uri);
        videoView.requestFocus();
        videoView.start();
    }
}


