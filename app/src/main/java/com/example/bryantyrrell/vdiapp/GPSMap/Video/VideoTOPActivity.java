package com.example.bryantyrrell.vdiapp.GPSMap.Video;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.bryantyrrell.vdiapp.R;

import java.util.Timer;
import java.util.TimerTask;

public class VideoTOPActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "Recorder";
    public static SurfaceView mSurfaceView;
    public static SurfaceHolder mSurfaceHolder;
    public static SurfaceView mSurfaceView1;
    public static SurfaceHolder mSurfaceHolder1;
    public static Camera mCamera ;
    public static boolean mPreviewRunning;
    public static Camera mCamera1 ;
    public static boolean mPreviewRunning1;
    static int count=0;
     public static final int CAMERA_REQUEST = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_top);

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mSurfaceView1 = (SurfaceView) findViewById(R.id.surface_camera1);
        mSurfaceHolder1 = mSurfaceView1.getHolder();
        mSurfaceHolder1.addCallback(this);
        mSurfaceHolder1.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        Button btnStart = (Button) findViewById(R.id.StartService);
        btnStart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                //startRecording();
                if(count==0) {
                    Intent intent = new Intent(VideoTOPActivity.this, SimpleVideoService.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startService(intent);
                    count++;
                }else{
                    Intent test2 = new Intent("com.vdi.driving.steering2");
                    sendBroadcast(test2);
                }

            }
        });
        Button btnStop = (Button) findViewById(R.id.StopService);

        btnStop.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {

                Intent test1 = new Intent("com.vdi.driving.video");
                sendBroadcast(test1);
            }
        });
       // startRecording();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }
    private void startRecording(){
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                if(count>0){
                    stopService(new Intent(VideoTOPActivity.this, SimpleVideoService.class));
                }
                count++;

                Intent intent = new Intent(VideoTOPActivity.this, SimpleVideoService.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startService(intent);

            }
        };


        TimerTask task1 = new TimerTask() {

            @Override
            public void run() {
                if(count>0){
                   // stopService(new Intent(VideoTOPActivity.this, SimpleVideo2Service.class));
                }
                count++;

               // Intent intent = new Intent(VideoTOPActivity.this, SimpleVideo2Service.class);
               // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               // startService(intent);

            }
        };

        Timer timer = new Timer();
        long delay = 9000;

        // schedules the task to be run after a 30 delay
        //timer.schedule(task, delay);
        timer.scheduleAtFixedRate(task,0, delay);
        timer.scheduleAtFixedRate(task1,3000, delay);
    }
}