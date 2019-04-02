package com.zyf.music.musiclibrary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.zyf.music.musiclibrary.utils.MusicFileUtils;
import com.zyf.music.musiclibrary.utils.MusicPlayer;

public class MusicPlayBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
       if(MusicFileUtils.NEXT.equals(intent.getAction())){
           MusicPlayer.next();
       }
       if(MusicFileUtils.PREVIOUS.equals(intent.getAction())){
           MusicPlayer.previous();
       }
       if(MusicFileUtils.PLAYORPAUSE.equals(intent.getAction())){
           MusicPlayer.playOrPause();
       }
        if(MusicFileUtils.PLAYOCLOSE.equals(intent.getAction())){
            MusicPlayer.playClose();
        }
    }
}
