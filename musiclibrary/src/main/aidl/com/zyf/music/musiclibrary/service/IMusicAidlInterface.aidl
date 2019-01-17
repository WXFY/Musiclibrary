// IMusicAidlInterface.aidl
package com.zyf.music.musiclibrary.service;

import com.zyf.music.musiclibrary.utils.IMusicPlayerAidlInterface;
import android.graphics.Bitmap;
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
        //缓冲进度
        int buffering();
        //跳转指定位置
        long seek(long pos);
        //播放状态
        boolean isPlaying();
        //注册服务监听
        void registerLoginUser(IMusicPlayerAidlInterface listener);
        void unRegisterLoginUser(IMusicPlayerAidlInterface listener);
        //设置音频缓存地址
        void setFilePath(String path);
        //通知显示图标
        void setMusicBitmap(in Bitmap icon);
}
