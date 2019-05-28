// IMusicPlayerAidlInterface.aidl
package com.zyf.music.musiclibrary.utils;

// Declare any non-default types here with import statements

interface IMusicPlayerAidlInterface {
    //缓存进度
   void bufferingProgress(int percent);
    //播放完成
   void onCompletion(boolean isNext);
   //播放错误
   void onError();
   /*
    * 播放网络音乐是会进行缓冲
    * 缓冲完开始播放时回调用于开启播放页面的状态等
    * */
   void onStart();
   /*
    * 通知栏触发下一曲
    * 获取下一首需要播放的歌曲
    * 调用此方法需要手动调用openfile方法传入对应的参数
    * 主要作为通知需要下一首方法
    * */
   void onNext();
   /*
    * 通知栏触发上一曲
    * 同上
    * */
   void onLast();
    /**
      * 播放器id
      * */
    void playerId(in int mediaPlayerId);
}
