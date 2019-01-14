package com.zyf.music.utils;

import android.os.CountDownTimer;

import com.zyf.music.musiclibrary.utils.MusicPlayer;

public class ClockSongUtils {
    public static boolean isClock = false;
    private static CountDownTimer timer;
    public static int pos = 0;
    private static OnClockLinsenter linsenter;
    public interface OnClockLinsenter{
        void onTick(String millisUntilFinished);
        void onFinish();
    }
    public static void startTime(long time){
        if(timer!=null){
            timer.cancel();
            timer = null;
        }
        timer = new CountDownTimer(time,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(linsenter != null){
                    linsenter.onTick(formatClockTime(millisUntilFinished));
                }
            }

            @Override
            public void onFinish() {
                if(linsenter != null){
                    linsenter.onFinish();
                }
                isClock = false;
                if(MusicPlayer.isPlaying()){
                    MusicPlayer.playOrPause();
                }
            }
        };
    }

    public static void start(){
        if(timer!=null){
            timer.start();
        }
    }
    public static OnClockLinsenter getLinsenter() {
        return linsenter;
    }

    public static void setLinsenter(OnClockLinsenter linsenter) {
        ClockSongUtils.linsenter = linsenter;
    }

    public static void stopTime(){
        if(timer!=null){
            timer.cancel();
            timer =null;
            linsenter = null;
        }
    }
    public static String formatClockTime(long time) {
        String min = (int)(time / (1000 * 60)) + "";
        String sec =  (int)(time / 1000 % 60) + "";
        String hour = (int)(time/(1000 * 60 * 3600))+"";
        if (hour.length() < 2) {
            hour = "0" + hour;
        }
        if (min.length() < 2) {
            min = "0" + min ;
        }
        if (sec.length() < 2) {
            sec = "0" + sec;
        }
        return hour + ":" + min + ":" + sec;
    }
}
