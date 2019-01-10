// IMusicPlayerAidlInterface.aidl
package com.zyf.music.musiclibrary.utils;

// Declare any non-default types here with import statements

interface IMusicPlayerAidlInterface {
   void bufferingProgress(int percent);
   void onCompletion(boolean isNext);
}
