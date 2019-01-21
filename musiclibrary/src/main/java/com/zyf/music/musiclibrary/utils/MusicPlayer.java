package com.zyf.music.musiclibrary.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import com.zyf.music.musiclibrary.listener.OnProgressListener;
import com.zyf.music.musiclibrary.model.SongName;
import com.zyf.music.musiclibrary.receiver.MusicPlayBroadcastReceiver;
import com.zyf.music.musiclibrary.service.IMusicAidlInterface;
import com.zyf.music.musiclibrary.service.MusicPlayerService;

import java.util.List;
import java.util.WeakHashMap;

public class MusicPlayer {

    public static IMusicAidlInterface mService = null;
    private static PlaybackMode mode = PlaybackMode.LISTLOOP;
    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap;
    private static List<?> list;
    private static int pos = -1;
    private static int next = -1;
    private static WeakHashMap<Class,OnProgressListener> listeners;
    private static MusicPlayBroadcastReceiver receiver;
    private static IMusicPlayerAidlInterface listener = new IMusicPlayerAidlInterface.Stub() {
        @Override
        public void bufferingProgress(int percent) {
            for (OnProgressListener value : listeners.values()) {
                value.onBufferingUpdate((int)(percent*duration()));
            }
        }

        @Override
        public void onCompletion(boolean isNext) {
            completion(isNext);
        }

        @Override
        public void onError() {
            for (OnProgressListener value : listeners.values()) {
                value.onError();
                completion(true);
            }
        }

        @Override
        public void onStart() {
            for (OnProgressListener value : listeners.values()) {
                value.onStart();
            }
        }
        /**
         * 通知栏点击下一首方法回调
         * 预留功能
         * */
        @Override
        public void onNext() {

        }
        /**
         * 通知栏点击上一首方法回调
         * 预留功能
         * */
        @Override
        public void onLast() {

        }
    };
    private static Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(duration()==-1||duration()==0){
                handler.sendEmptyMessageDelayed(0,500);
                return;
            }
            for (OnProgressListener value : listeners.values()) {
                value.onLongProgress(duration(),position());
                value.onStringProgress(TimeFormatUtils.formatTime(duration()),TimeFormatUtils.formatTime(position()));
            }
            handler.sendEmptyMessageDelayed(0,500);
        }
    };

    static {
        mConnectionMap = new WeakHashMap<Context, ServiceBinder>();
        listeners = new WeakHashMap<>();
    }

    public static ServiceToken bindToService(final Context context,
                                             final ServiceConnection callback) {

        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }
        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicPlayerService.class));
        final ServiceBinder binder = new ServiceBinder(callback,
                contextWrapper.getApplicationContext());
        if (contextWrapper.bindService(
                new Intent().setClass(contextWrapper, MusicPlayerService.class), binder, 0)) {
            mConnectionMap.put(contextWrapper, binder);
            if(receiver==null){
                receiver = new MusicPlayBroadcastReceiver();
                IntentFilter filter = new IntentFilter(MusicFileUtils.MESSAGECILCK);
                filter.addAction(MusicFileUtils.NEXT);
                filter.addAction(MusicFileUtils.PREVIOUS);
                filter.addAction(MusicFileUtils.PLAYORPAUSE);
                context.registerReceiver(receiver,filter);
            }
            return new ServiceToken(contextWrapper);
        }
        return null;
    }

    public static void unbindFromService(final ServiceToken token) {
        if (token == null) {
            return;
        }
        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }
        handler.removeMessages(0);
        pos = -1;
        next = -1;
        listeners.clear();
        if(list!=null)
        try {
            mService.unRegisterLoginUser(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mContextWrapper.unregisterReceiver(receiver);
        mContextWrapper.unbindService(mBinder);
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;
        private final Context mContext;


        public ServiceBinder(final ServiceConnection callback, final Context context) {
            mCallback = callback;
            mContext = context;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            mService = IMusicAidlInterface.Stub.asInterface(service);
            try {
                mService.registerLoginUser(listener);
                mService.setMusicBitmap(MusicFileUtils.getMusic_ico(mContext));
                mService.setFilePath(MusicFileUtils.getFilePath());
                readyMusic();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }
            mService = null;
        }
    }

    private static void readyMusic() {
        if(list!=null&&list.size()>pos) {
            openFile(((SongName) (list.get(pos))).SongPath());
        }
    }

    public static void  setListener(Class clazz,OnProgressListener listener) {
        listeners.put(clazz,listener);
        handler.removeMessages(0);
        handler.sendEmptyMessage(0);
    }
    public static void  removeListener(Class clazz) {
        listeners.remove(clazz);
    }
    public static void playOrPause() {
        try {
            if (mService != null) {
                if (mService.isPlaying()) {
                    mService.pause();
                } else {
                    mService.play();
                }
                for (OnProgressListener value : listeners.values()) {
                    value.onStart();
                }
            }
        } catch (final Exception ignored) {
        }
    }
    /**
     * 下一首
     * */
    public static void next() {
        switch (mode){
            case RANDOM:
                pos = (int)(Math.random()*(list.size()-1));
                break;
            default:
                if(pos>=list.size()-1){
                    pos = 0;
                }else {
                    pos++;
                }
                break;
        }
        for (OnProgressListener value : listeners.values()) {
            value.onCurrentSong(list.get(pos));
        }
        readyMusic();
        playOrPause();
    }
    /**
     * 上一首
     * */
    public static void previous() {
        switch (mode){
            case RANDOM:
                pos = (int)(Math.random()*(list.size()-1));
                break;
            default:
                if(pos<=0){
                    pos = list.size()-1;
                }else {
                    pos--;
                }
                break;
        }
        for (OnProgressListener value : listeners.values()) {
            value.onCurrentSong(list.get(pos));
        }
        readyMusic();
        playOrPause();
    }
    /**
     * 播放完成回调
     * @param isNext true下一首已准备好  false未准备好
     * */
    private static void completion(boolean isNext){
        if(list!=null&&list.size()>0){
            try {
                /*switch (mode){
                    case RANDOM:
                        if(isNext){
                            pos = next;
                            next = (int)(Math.random()*(list.size()-1));
                            change(((SongName)(list.get(next))).SongPath());
                        }else {
                            pos = (int)(Math.random()*(list.size()-1));
                            openFile(((SongName)(list.get(pos))).SongPath());
                            playOrPause();
                            next = (int)(Math.random()*(list.size()-1));
                            change(((SongName)(list.get(next))).SongPath());
                        }
                        break;
                    case LISTLOOP:
                        if(isNext){
                            pos = next;
                            if(next>=list.size()-1){
                                next = 0;
                            }else {
                                next++;
                            }
                            change(((SongName)(list.get(next))).SongPath());
                        }else {
                            if(pos>=list.size()-1){
                                pos = 0;
                            }else {
                                pos++;
                            }
                            openFile(((SongName)(list.get(pos))).SongPath());
                            playOrPause();
                            next = pos++;
                            change(((SongName)(list.get(next))).SongPath());
                        }
                        break;
                    case SINGLESONG:
                        if(isNext){
                            change(((SongName)(list.get(pos))).SongPath());
                        }else {
                            openFile(((SongName)(list.get(pos))).SongPath());
                            playOrPause();
                        }

                        break;
                    case LISTORDER:
                        if(isNext){
                            pos = next;
                            if(next>=list.size()-1){
                                seek(0);
                                return;
                            }else {
                                next++;
                            }
                            change(((SongName)(list.get(next))).SongPath());
                        }else {
                            if(pos>=list.size()-1){
                                seek(0);
                                return;
                            }else {
                                pos++;
                            }
                            openFile(((SongName)(list.get(pos))).SongPath());
                            playOrPause();
                            next = pos++;
                            change(((SongName)(list.get(next))).SongPath());
                        }
                        break;
                }*/
                switch (mode){
                    case RANDOM:
                        pos = (int)(Math.random()*(list.size()-1));
                        openFile(((SongName)(list.get(pos))).SongPath());
                        playOrPause();
                        break;
                    case LISTLOOP:
                        if(pos>=list.size()-1){
                            pos = 0;
                        }else {
                            pos++;
                        }
                        openFile(((SongName)(list.get(pos))).SongPath());
                        playOrPause();
                        break;
                    case SINGLESONG:
                        openFile(((SongName)(list.get(pos))).SongPath());
                        playOrPause();
                        break;
                }
                for (OnProgressListener value : listeners.values()) {
                    value.onCurrentSong(list.get(pos));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Object getCurrentSong() {
        try {
            return list.get(pos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static List<?> getList() {
        return list;
    }

    public static int getPos() {
        return pos;
    }

    public static boolean isPlaying() {
        if (mService != null) {
            try {
                return mService.isPlaying();
            } catch (final RemoteException ignored) {
            }
        }
        return false;
    }

    public static void seek(final long position) {
        if (mService != null) {
            try {
                mService.seek(position);
            } catch (final RemoteException ignored) {
            }
        }
    }

    public static long position() {
        if (mService != null) {
            try {
                return mService.position();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ex) {

            }
        }
        return 0;
    }

    public static long duration() {
        if (mService != null) {
            try {
                return mService.duration();
            } catch (final RemoteException ignored) {
            } catch (final IllegalStateException ignored) {

            }
        }
        return 0;
    }

    public static void openFile(final String path) {
        if (mService != null) {
            try {
                SongName song = (SongName)getCurrentSong();
                mService.openFileSong(path,song.SongName(),song.SongAuthor());
            } catch (final RemoteException ignored) {
            }
        }
    }
    public static void change(final String path) {
        if (mService != null) {
            try {
                mService.change(path);
            } catch (final RemoteException ignored) {
            }
        }
    }
    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static void setMode(PlaybackMode mode) {
        MusicPlayer.mode = mode;
    }

    public static PlaybackMode getMode() {
        return mode;
    }
    public static void changeMode() {
        switch (mode){
            case LISTLOOP:
                mode = PlaybackMode.RANDOM;
                break;
            case RANDOM:
                mode = PlaybackMode.SINGLESONG;
                break;
            case SINGLESONG:
                mode = PlaybackMode.LISTLOOP;
                break;
        }
    }
    public static void setList(List<?> list, int current) {
        MusicPlayer.list = list;
        pos = current;
        if(mService!=null){
            readyMusic();
        }
    }
    public static void setMusicFilePath(String path) {
        try {
            MusicFileUtils.setFilePath(path);
        } catch (Exception ignored) {
        }
    }
    public static void setMusicIcon(Bitmap icon) {
        try {
           MusicFileUtils.setMusic_ico(icon);
        } catch (Exception ignored) {
        }
    }

}
