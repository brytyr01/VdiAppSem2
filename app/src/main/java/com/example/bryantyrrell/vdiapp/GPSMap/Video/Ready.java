package com.example.bryantyrrell.vdiapp.GPSMap.Video;

import android.app.Application;

public class Ready extends Application {

        private boolean VideoFinished;

        public boolean getVideoFinished() {
            return VideoFinished;
        }

        public void setVideoFinished(boolean set) {
            this.VideoFinished = set;
        }
    }

