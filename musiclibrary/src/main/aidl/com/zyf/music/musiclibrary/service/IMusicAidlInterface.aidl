// IMusicAidlInterface.aidl
package com.zyf.music.musiclibrary.service;

import com.zyf.music.musiclibrary.utils.IMusicPlayerAidlInterface;

interface IMusicAidlInterface {
        //打开文件
        void openFile(String path);
        void openFileSong(String path,String Songname,String author);
        void change(String path);
        void stop();
        void pause();
        void play();
        //void next();
         //总长度
        long duration();
        //当前进度
        long position();
        int buffering();
        //跳转指定位置
        long seek(long pos);

        boolean isPlaying();
        void registerLoginUser(IMusicPlayerAidlInterface listener);
        void unRegisterLoginUser(IMusicPlayerAidlInterface listener);
        void setFilePath(String path);
        void setMusicIcon(String icon);
}
