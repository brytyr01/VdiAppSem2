package com.example.bryantyrrell.vdiapp.GPSMap.Video;
import java.io.File;
import java.io.IOException;
import java.util.List;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.DrivingBroadcast;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.google.android.gms.maps.GoogleMap;

public class SimpleVideoService extends Service implements MediaRecorder.OnInfoListener {
    private static final String TAG = "RecorderService";
    private File directory;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private static Camera mServiceCamera;
    private boolean mRecordingStatus;
    static boolean VideoStopped=false;
    private MediaRecorder mMediaRecorder;
    private FileCleanUp fileClean;
    static int countvalue = 0;
    DrivingBroadcast drivingBroadcastSteering1;



    private void registerVideoReceiver() {
        drivingBroadcastSteering1 = new DrivingBroadcast() {
            @Override
            public void onReceive(Context context, Intent intent) {

                int signal = intent.getIntExtra("StopType",0);
                if(signal==1){
                    VideoStopped=true;
                    stopRecording();
                }else{
                    stopRecording();
                }
            }
        };
        registerReceiver(drivingBroadcastSteering1, new IntentFilter("com.vdi.driving.video"));
    }

    @Override
    public void onCreate() {
        mRecordingStatus = false;
        //mServiceCamera = CameraRecorder.mCamera;
        mServiceCamera = Camera.open(0);
        mSurfaceView = MapsActivity.mSurfaceView;
        mSurfaceHolder = MapsActivity.mSurfaceHolder;
        if(mSurfaceView==null){
            mSurfaceView = VideoTOPActivity.mSurfaceView;
            mSurfaceHolder = VideoTOPActivity.mSurfaceHolder;
        }
        createDirectory();
        fileClean = new FileCleanUp(this);
        super.onCreate();
        if (mRecordingStatus == false) {
            startRecording();
        }
        registerVideoReceiver();
    }

    private void createDirectory() {
        directory = new File(getExternalCacheDir()+ File.separator+"videoFiles");
        boolean result = directory.mkdirs();
        System.out.println("The directory created is: "+directory.getAbsolutePath());
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        mRecordingStatus = false;

       super.onDestroy();
    }

    public boolean startRecording(){
        try {
            Toast.makeText(getBaseContext(), "Recording Started", Toast.LENGTH_SHORT).show();

            //mServiceCamera = Camera.open();
            Camera.Parameters params = mServiceCamera.getParameters();
            mServiceCamera.setParameters(params);
            Camera.Parameters p = mServiceCamera.getParameters();

            final List<Size> listSize = p.getSupportedPreviewSizes();
            Size mPreviewSize = listSize.get(2);
            Log.v(TAG, "use: width = " + mPreviewSize.width
                    + " height = " + mPreviewSize.height);
            p.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            p.setPreviewFormat(PixelFormat.YCbCr_420_SP);
            mServiceCamera.setParameters(p);
            mServiceCamera.setDisplayOrientation(90);

            try {
                mServiceCamera.setPreviewDisplay(mSurfaceHolder);
                mServiceCamera.startPreview();
            }
            catch (IOException e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
            if(countvalue==0) {
                mServiceCamera.unlock();
            }
            if(countvalue>0) {
                mServiceCamera.lock();
                mServiceCamera.unlock();
            }

            mMediaRecorder = new MediaRecorder();
            mMediaRecorder.setCamera(mServiceCamera);
            mMediaRecorder.setOrientationHint(90);
           // mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
           // mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setOutputFile(directory.getAbsolutePath()+File.separator+"Video"+countvalue+".mp4");
            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoSize(mPreviewSize.width, mPreviewSize.height);
            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
           // mMediaRecorder.setMaxDuration(10000); // 10 seconds
            //mMediaRecorder.setOnInfoListener(this);

            mMediaRecorder.prepare();
            mMediaRecorder.start();
            Long StartTime = System.currentTimeMillis();

            fileClean.addVideoIndex(countvalue,StartTime);
            countvalue++;
            mRecordingStatus = true;


            return true;
        } catch (IllegalStateException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    public void onInfo(MediaRecorder mr, int what, int extra) {
    }


    public void stopRecording() {
        Toast.makeText(getBaseContext(), "Recording Stopped", Toast.LENGTH_SHORT).show();
        try {
            mServiceCamera.reconnect();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            mMediaRecorder.stop();
        }catch(java.lang.RuntimeException e){
            e.printStackTrace();
        }

        startRecording();

        if(VideoStopped==true){
            VideoStopped=false;
            Intent test1 = new Intent("com.vdi.driving.VideoRestarted");

            int videoValue=countvalue;
            if(videoValue==1) {
                videoValue=videoValue-1;
                test1.putExtra("VideoIndex", videoValue);
            }else if(videoValue>1){
                videoValue=videoValue-2;
                test1.putExtra("VideoIndex", videoValue);
            }
            sendBroadcast(test1);
            System.out.println("video restarted broadcast sent");
        }

        //startRecording();


    }

}