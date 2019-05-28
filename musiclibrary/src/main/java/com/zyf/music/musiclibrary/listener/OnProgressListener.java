package com.zyf.music.musiclibrary.listener;

public interface OnProgressListener {
    void onLongProgress(long duration,long current);
    void onStringProgress(String duration,String current);
    void onBufferingUpdate(int percent);
    void onCurrentSong(Object song);
    void onError();
    void onStart();
    void playerId(int mediaPlayerId);
}
