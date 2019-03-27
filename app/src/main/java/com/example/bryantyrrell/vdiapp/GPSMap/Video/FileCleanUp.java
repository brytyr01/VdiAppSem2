package com.example.bryantyrrell.vdiapp.GPSMap.Video;

import android.content.Context;
import android.util.Log;

import com.example.bryantyrrell.vdiapp.Database.DatabaseService;
import com.example.bryantyrrell.vdiapp.GPSMap.MapUI.MapsActivity;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.GeoPoint;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileCleanUp {
    static ArrayList<Integer> videoIndex = new ArrayList<>();
    static ArrayList<Long> StartTimes = new ArrayList<>();
    static ArrayList<VideoCutTimes> CutObjects = new ArrayList<>();
    static Map<Integer,Long> referenceList = new HashMap<>();
    private long videoEditingTime=4000;
    private long videoLength=12000;
    private File processedVideoFile;
    private String root;
    private Context context;
    private SimpleVideoService videoDoneCheck;
    private DatabaseService databaseUser;

    public FileCleanUp(Context context){
        this.context=context;
        root = context.getExternalCacheDir().toString()+"/videoFiles/";

    }
    public FileCleanUp(MapsActivity activity, DatabaseService databaseUser){
        root = activity.getExternalCacheDir().toString()+"/videoFiles/";
        this.databaseUser=databaseUser;
        context=activity;

    }

    public void addVideoIndex(int videoIndexInt, Long startTime){
        videoIndex.add(videoIndexInt);
        StartTimes.add(startTime);
        referenceList.put(videoIndexInt,startTime);

        if(videoIndex.size()>3){
            //deleteFile(videoIndex.remove(0));
            //StartTimes.remove(0);
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

    public void ProcessVideo(final Marker marker, String TypeOfDriving, long incidentTime, int videoIndex){
        // split a video into minus 4 seconds and plus 4 seconds
        VideoCutTimes cutTimes = new VideoCutTimes();

       int StartTimeIndex = GetTimeIndex(incidentTime,videoIndex);

       if(StartTimeIndex>=0) {
           getVideoEditingStartTimes(StartTimeIndex, incidentTime,cutTimes);
           getVideoEditingStopTimes(StartTimeIndex, incidentTime,cutTimes);
           cutTimes.setIncidentTime(incidentTime);
           cutTimes.setIndex(StartTimeIndex);
           CutObjects.add(cutTimes);
           System.out.println("This incident time is: "+incidentTime);
           System.out.println("This start time is: "+StartTimes.get(StartTimeIndex));
           System.out.println("This is the start time index: "+cutTimes.getIndex());
           System.out.println("This is the time before: "+cutTimes.getTimeBefore());
           System.out.println("This is the time After: "+cutTimes.getTimeAfter());



       }
        System.out.println("This is the video index used: "+videoIndex);
        int indexTemp=videoIndex;
//        if(FileCleanUp.videoIndex.size()>1) {
//            indexTemp = FileCleanUp.videoIndex.get((FileCleanUp.videoIndex.size() - 2));
//            //indexTemp = videoIndex.get((videoIndex.size() - 1));
//        }else{
//            indexTemp = FileCleanUp.videoIndex.get((FileCleanUp.videoIndex.size() - 1));
//        }

        long totalTime = 15;
        long StartTime = 0;
        long PotientialStartTime = (cutTimes.getIncidentTime()-StartTimes.get(cutTimes.getIndex()))/1000;
        System.out.println("The start time is: "+PotientialStartTime);
        long PotientialtotalTime = (cutTimes.getTimeBefore()+cutTimes.getTimeAfter())/1000;


        PotientialStartTime=PotientialStartTime-((cutTimes.getTimeBefore()+cutTimes.getTimeAfter())/1000);

        if(PotientialStartTime<0||PotientialStartTime>27){
            PotientialStartTime=0;
        }
//        if(totalTime<=0||totalTime>7){
//            totalTime = 4;
//        }
        System.out.println("The start time is: "+PotientialStartTime);
        System.out.println("The total time is: "+totalTime);
        System.out.println("The potiential total time after is: "+PotientialtotalTime);
        //String[] cmd = {"-ss", "" + StartTime / 1000, "-y", "-i", root+"Video"+indexTemp+".mp4", "-t", "" + (totalTime) / 1000, "-s", "320x240", "-r", "15", "-vcodec", "mpeg4", "-b:v", "2097152", "-b:a", "48000", "-ac", "2", "-ar", "22050", root+"ProcessedVideoFFMPeg"+indexTemp+".mp4"};
        //ffmpeg -i in.mp4 -ss [start] -t [duration] -c copy out.mp4
        String[] cmd = {"-y","-i",root+"Video"+indexTemp+".mp4","-ss",""+PotientialStartTime, "-c","copy","-t", "" + PotientialtotalTime, root+"ProcessedVideoFFMPeg"+indexTemp+".mp4"};

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
        } catch (Exception e) {
            // Handle if FFmpeg is not supported by device
        }
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            final long[] Time = new long[1];
            final int finalIndexTemp = indexTemp;
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    Time[0] =System.currentTimeMillis();
                }

                @Override
                public void onProgress(String message) {
                    Log.i("FFmpeg", message);
                }

                @Override
                public void onFailure(String message) {
                    Log.e("FFmpeg", message);
                }

                @Override
                public void onSuccess(String message) {}

                @Override
                public void onFinish() {
                    //call update server inside here
                    Log.i("FFmpeg", "on finish");

                    System.out.println("it finished ffmpeg ");


                    File NewFile = new File(root,"ProcessedVideoFFMPeg"+ finalIndexTemp +".mp4");
                    VideoObject videoObject = CreateVideoObject(NewFile, marker);
                    processedVideoFile=NewFile;
                    File videoFile = getVideoFile();

                    databaseUser.uploadVideoPoint(videoObject);
                    databaseUser.uploadFile(videoFile);

                }
            });
        } catch (Exception e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }



//        File NewFile = new File(root,"ProcessedVideoFFMPeg"+indexTemp+".mp4");
//        VideoObject video = CreateVideoObject(NewFile, marker);
//        processedVideoFile=NewFile;
            //might need to add a wait into here?
           // return video;

    }

    private int GetTimeIndex(long incidentTime,int VideoIndex) {

        //long startTime = referenceList.get(VideoIndex);
            for (int i = StartTimes.size() - 1; i >= 0; i--) {
                System.out.println("StartTimes inide for loop Are"+StartTimes.get(i));
                System.out.println("Incident time is"+incidentTime);
                if (StartTimes.get(i) <= incidentTime) {
                    System.out.println("Got inside index call:   " + i);
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
            //cutTimes.setTimeAfter((videoLength-(incidentTime-StartTimes.get(startTimeIndex))));
            cutTimes.setTimeAfter(2000);
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
