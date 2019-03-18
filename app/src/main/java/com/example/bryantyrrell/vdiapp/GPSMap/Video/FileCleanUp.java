package com.example.bryantyrrell.vdiapp.GPSMap.Video;

import android.content.Context;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.GeoPoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FileCleanUp {
    static ArrayList<Integer> videoIndex = new ArrayList<>();
    static ArrayList<Long> StartTimes = new ArrayList<>();
    static ArrayList<VideoCutTimes> CutObjects = new ArrayList<>();
    private static boolean ClassReady=false;
    private long videoEditingTime=4000;
    private long videoLength=12000;
    private File processedVideoFile;
    private String root;
    private Context context;

    public FileCleanUp(Context context){
        this.context=context;
        root = context.getExternalCacheDir().toString()+"/videoFiles/";

    }

    FileCleanUp(String absoluteFilePath, boolean ClassReady){
        this.ClassReady=ClassReady;
        root = absoluteFilePath;

    }

    public boolean isClassReady(){
        return ClassReady;
    }

    public void addVideoIndex(int countValue, Long startTime){
        videoIndex.add(countValue);
        StartTimes.add(startTime);
        if(videoIndex.size()>3){
            deleteFile(videoIndex.remove(0));
            StartTimes.remove(0);
        }
    }

    private void deleteFile(Integer remove) {
        int index = remove;
        System.out.println("The index is: "+index);
        System.out.println("The root directory is: "+root);
        File file = new File(root,"Video"+index+".mp4");
        boolean deleted = file.delete();
        System.out.println("file deleted: "+ deleted);

    }

    public VideoObject ProcessVideo(Marker marker, String TypeOfDriving, long incidentTime){
        // split a video into minus 4 seconds and plus 4 seconds
        VideoCutTimes cutTimes = new VideoCutTimes();

       int StartTimeIndex = GetTimeIndex(incidentTime);

       if(StartTimeIndex>=0) {
           getVideoEditingStartTimes(StartTimeIndex, incidentTime,cutTimes);
           getVideoEditingStopTimes(StartTimeIndex, incidentTime,cutTimes);
           cutTimes.setIncidentTime(incidentTime);
           cutTimes.setIndex(StartTimeIndex);
           CutObjects.add(cutTimes);
           System.out.println("This incident time is: "+incidentTime);
           System.out.println("This start time is: "+StartTimes.get(StartTimeIndex));
           System.out.println("This is the index: "+cutTimes.getIndex());
           System.out.println("This is the time before: "+cutTimes.getTimeBefore());
           System.out.println("This is the time After: "+cutTimes.getTimeAfter());



       }





        int indexTemp=0;
        if(videoIndex.size()>1) {
            indexTemp = videoIndex.get((videoIndex.size() - 2));
        }else{
            indexTemp = videoIndex.get((videoIndex.size() - 1));
        }
        long totalTime = cutTimes.getTimeBefore()+ cutTimes.getTimeAfter()-1000;
        long StartTime = cutTimes.getIncidentTime()-StartTimes.get(cutTimes.getIndex());
        String[] cmd = {"-ss", "" + StartTime / 1000, "-y", "-i", root+"Video"+indexTemp+".mp4", "-t", "" + (totalTime) / 1000, "-s", "320x240", "-r", "15", "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", root+"ProcessedVideoFFMPeg"+indexTemp+".mp4"};

        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
             ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {}

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            final long[] Time = new long[1];
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Time[0] =System.currentTimeMillis();
                }

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {}

                @Override
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {
                    //call update server inside here
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
        }

        File NewFile = new File(root,"ProcessedVideoFFMPeg"+indexTemp+".mp4");
        VideoObject video = CreateVideoObject(NewFile, marker);
        processedVideoFile=NewFile;
        //might need to add a wait into here?
        return video;
    }

    private int GetTimeIndex(long incidentTime) {
        for(int i=StartTimes.size()-1;i>=0;i--){
            if(StartTimes.get(i)<=incidentTime){
                System.out.println("Got inside index call:   "+i);
                return i;
            }
        }
        return -1;
    }
    private void getVideoEditingStartTimes(int startTimeIndex, long incidentTime, VideoCutTimes cutTimes) {

        if(StartTimes.get(startTimeIndex)<(incidentTime-videoEditingTime)){
            //say its fine to cut from the current video index
            cutTimes.setTimeBefore(videoEditingTime);
        }else{
            // say you need the remainder to be taken from index before
            cutTimes.setTimeBefore((incidentTime-StartTimes.get(startTimeIndex)));
        }
    }
    private void getVideoEditingStopTimes(int startTimeIndex, long incidentTime, VideoCutTimes cutTimes) {

        if((videoLength+StartTimes.get(startTimeIndex))>incidentTime+videoEditingTime){
            //say its fine to cut from the current video index
            cutTimes.setTimeAfter(videoEditingTime);
        }else{
            // say you need the remainder to be taken from index before
            cutTimes.setTimeAfter((videoLength-(incidentTime-StartTimes.get(startTimeIndex))));
        }
    }

    private VideoObject CreateVideoObject(File processedVideo,Marker marker) {
        GeoPoint geopoint = new GeoPoint(marker.getPosition().latitude,marker.getPosition().longitude);
        String TypeOfDriving = marker.getTitle();
        String VideoName = processedVideo.getName();

        VideoObject video = new VideoObject(TypeOfDriving,geopoint,VideoName);
        return video;

    }


    public File getVideoFile() {
        return processedVideoFile;
    }
}
