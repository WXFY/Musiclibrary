package com.zyf.music.musiclibrary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.zyf.music.musiclibrary.R;

public class MusicFileUtils {
    private static String filePath = "music-cache";
    private static Bitmap music_ico;
    public static String MESSAGECILCK = "com.musicplayer.play";
    public static void setFilePath(String filePath) {
        MusicFileUtils.filePath = filePath;
    }

    public static String getFilePath() {
        return filePath;
    }

    public static void setMusic_ico(Bitmap music_ico) {
        try {
            MusicFileUtils.music_ico = music_ico;
        } catch (Exception e) {
            e.printStackTrace();
            MusicFileUtils.music_ico = null;
        }
    }

    public static Bitmap getMusic_ico(Context context) {
        if(MusicFileUtils.music_ico==null){
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_music_note_white_48dp);
        }
        return MusicFileUtils.music_ico;
    }
}
