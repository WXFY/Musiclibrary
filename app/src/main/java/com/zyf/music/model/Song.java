package com.zyf.music.model;

import com.zyf.music.musiclibrary.model.SongName;

public class Song implements SongName{
    private String path;
    private String name;
    private String lrc;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String SongPath() {
        return path;
    }
}
