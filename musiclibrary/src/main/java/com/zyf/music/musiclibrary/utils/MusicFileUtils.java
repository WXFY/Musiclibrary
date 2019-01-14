package com.zyf.music.musiclibrary.utils;

public class MusicFileUtils {
    private static String filePath = "music-cache";

    public static void setFilePath(String filePath) {
        MusicFileUtils.filePath = filePath;
    }

    public static String getFilePath() {
        return filePath;
    }
}
