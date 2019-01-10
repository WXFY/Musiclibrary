package com.zyf.music.musiclibrary.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.danikula.videocache.HttpProxyCacheServer;
import com.danikula.videocache.file.FileNameGenerator;
import com.zyf.music.musiclibrary.utils.IMusicPlayerAidlInterface;

import java.io.File;
import java.io.IOException;

public class MusicPlayerService extends Service{
    private static final String TAG = "MusicPlayerService";
    public IMusicAidlInterface.Stub musicConnection = new IMusicAidlInterface.Stub() {
        @Override
        public void openFile(String path) {
            multiPlayer.setDataSource(path);
        }

        @Override
        public void stop() {
            multiPlayer.stop();
        }

        @Override
        public void pause()  {
            multiPlayer.pause();
        }

        @Override
        public void play() {
            multiPlayer.start();
        }

        @Override
        public long duration() {
            if(multiPlayer!=null){
                return multiPlayer.duration();
            }
           return 0;
        }

        @Override
        public long position() {
            return multiPlayer.position();
        }

        @Override
        public long seek(long pos) {
            return multiPlayer.seek(pos);
        }

        @Override
        public boolean isPlaying() {
            return multiPlayer.isPlay();
        }

        @Override
        public void change(String path) {
            multiPlayer.setNextDataSource(path);
        }

        @Override
        public int buffering() {
            return multiPlayer.buffer;
        }

        @Override
        public void registerLoginUser(IMusicPlayerAidlInterface listener)  {
            mCallbacks.register(listener);
        }

        @Override
        public void unRegisterLoginUser(IMusicPlayerAidlInterface listener) {
            mCallbacks.unregister(listener);
        }
    };

    private MultiPlayer multiPlayer;
    private AudioFocusManager manager;
    private int mServiceStartId = -1;
    public final RemoteCallbackList<IMusicPlayerAidlInterface> mCallbacks  = new RemoteCallbackList<IMusicPlayerAidlInterface>();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicConnection;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        multiPlayer = new MultiPlayer(this,mCallbacks); //当player接收到播放状态相关的回调时,发送信息给handler处理
        manager = new AudioFocusManager(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //可以进行保存当前播放歌曲信息 以便于下次进行继续播放
        stopSelf(mServiceStartId);
        return true;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        multiPlayer.release();
        multiPlayer = null;
    }

    private static class MultiPlayer implements MediaPlayer.OnErrorListener,MediaPlayer.OnCompletionListener,MediaPlayer.OnBufferingUpdateListener {
        private boolean mIsInitialized = false;
        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();
        private MediaPlayer mNextMediaPlayer;
        private MusicPlayerService service;
        RemoteCallbackList<IMusicPlayerAidlInterface> mCallbacks;
        private HttpProxyCacheServer proxy;
        private int buffer = 0;
        public MultiPlayer(final MusicPlayerService service,RemoteCallbackList<IMusicPlayerAidlInterface> mCallbacks) {
            this.service = service;
            this.mCallbacks = mCallbacks;
            mCurrentMediaPlayer.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK);
        }
        public HttpProxyCacheServer getProxy() {
            return this.proxy == null ? (this.proxy = this.newProxy()) : this.proxy;
        }
        private HttpProxyCacheServer newProxy() {
            return new HttpProxyCacheServer.Builder(service.getApplicationContext())
                    .cacheDirectory(new File(Environment.getExternalStorageDirectory(), "music-cache"))
                    .fileNameGenerator(new MyFileNameGenerator()).build();
        }
        public void setDataSource(final String path) {
            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
                setNextDataSource(null);
            }
        }

        private boolean setDataSourceImpl(final MediaPlayer player, final String path) {
            proxy = getProxy();
            try {
                player.reset();
                player.setOnPreparedListener(null);
                if (path.startsWith("content://")) {
                    player.setDataSource(service, Uri.parse(path));
                } else {
                    player.setDataSource(proxy.getProxyUrl(path));
                }
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);

                player.prepare();
            } catch (final IOException todo) {

                return false;
            } catch (final IllegalArgumentException todo) {

                return false;
            }
            player.setOnCompletionListener(this);
            player.setOnErrorListener(this);
            player.setOnBufferingUpdateListener(this);
            return true;
        }

        public void setNextDataSource(final String path) {
            try {
                mCurrentMediaPlayer.setNextMediaPlayer(null);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "下一个媒体播放器是当前的，继续");
            } catch (IllegalStateException e) {
                Log.e(TAG, "媒体播放器未初始化！");
                return;
            }
            if (mNextMediaPlayer != null) {
                mNextMediaPlayer.release();
                mNextMediaPlayer = null;
            }
            if (path == null) {
                return;
            }
            mNextMediaPlayer = new MediaPlayer();
            mNextMediaPlayer.setWakeMode(service, PowerManager.PARTIAL_WAKE_LOCK);
            mNextMediaPlayer.setAudioSessionId(getAudioSessionId());
            if (setDataSourceImpl(mNextMediaPlayer, path)) {
                mCurrentMediaPlayer.setNextMediaPlayer(mNextMediaPlayer);
            } else {
                if (mNextMediaPlayer != null) {
                    mNextMediaPlayer.release();
                    mNextMediaPlayer = null;
                }
            }
        }
        //播放完成
        @Override
        public void onCompletion(MediaPlayer mp) {
            int count = mCallbacks.beginBroadcast();
            if (mp == mCurrentMediaPlayer && mNextMediaPlayer != null) {
                mCurrentMediaPlayer.release();
                mCurrentMediaPlayer = mNextMediaPlayer;
                mNextMediaPlayer = null;
                for (int i = 0; i < count; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onCompletion(true);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }else {
                for (int i = 0; i < count; i++) {
                    try {
                        mCallbacks.getBroadcastItem(i).onCompletion(false);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }
            mCallbacks.finishBroadcast();

        }
        //播放错误
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onBufferingUpdate(MediaPlayer mp, int percent) {
            buffer = percent;
            int count = mCallbacks.beginBroadcast();
            for (int i = 0; i < count; i++) {
                try {
                    mCallbacks.getBroadcastItem(i).bufferingProgress(percent);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            mCallbacks.finishBroadcast();
        }

        public int getAudioSessionId() {
            return mCurrentMediaPlayer.getAudioSessionId();
        }

        public boolean isInitialized() {
            return mIsInitialized;
        }
        public void start() {
            mCurrentMediaPlayer.start();
        }

        public void release() {
            mCurrentMediaPlayer.release();
        }


        public void pause() {
            mCurrentMediaPlayer.pause();
        }


        public long duration() {
            if(!mIsInitialized){
                return 0;
            }
            return mCurrentMediaPlayer.getDuration();
        }

        public int getBuffer() {
            return buffer;
        }

        public long position() {
            if(!mIsInitialized){
                return 0;
            }
            return mCurrentMediaPlayer.getCurrentPosition();
        }


        public long seek(final long whereto) {
            mCurrentMediaPlayer.seekTo((int) whereto);
            return whereto;
        }

        public void setVolume(final float vol) {
            mCurrentMediaPlayer.setVolume(vol, vol);
        }

        public void stop() {
            mCurrentMediaPlayer.reset();
        }

        public boolean isPlay() {
            return mCurrentMediaPlayer.isPlaying();
        }
        public class MyFileNameGenerator implements FileNameGenerator {//缓存的命名规则
            public String generate(String url) {
                String audioId = url.substring(url.lastIndexOf("/"),url.lastIndexOf("."));
                return audioId + ".mp3";
            }
        }
    }

    public static  class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener {
        private MusicPlayerService service;
        private AudioManager mAudioManager;
        private boolean isPausedByFocusLossTransient;
        private int mVolumeWhenFocusLossTransientCanDuck;

        public AudioFocusManager(@NonNull MusicPlayerService service) {
            this.service = service;
            mAudioManager = (AudioManager) service.getSystemService(AUDIO_SERVICE);
        }

        /**
         * 播放音乐前先请求音频焦点
         */
        public boolean requestAudioFocus() {
            return mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                    == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        }

        /**
         * 退出播放器后不再占用音频焦点
         */
        public void abandonAudioFocus() {
            mAudioManager.abandonAudioFocus(this);
        }

        /**
         * 音频焦点监听回调
         */
        @Override
        public void onAudioFocusChange(int focusChange) {
            int volume;
            switch (focusChange) {
                // 重新获得焦点
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (!willPlay() && isPausedByFocusLossTransient) {
                        // 通话结束，恢复播放
                        service.multiPlayer.start();
                    }

                    volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (mVolumeWhenFocusLossTransientCanDuck > 0 && volume == mVolumeWhenFocusLossTransientCanDuck / 2) {
                        // 恢复音量
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }

                    isPausedByFocusLossTransient = false;
                    mVolumeWhenFocusLossTransientCanDuck = 0;
                    break;
                // 永久丢失焦点，如被其他播放器抢占
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (willPlay()) {
                        service.multiPlayer.stop();
                    }
                    break;
                // 短暂丢失焦点，如来电
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    if (willPlay()) {
                        service.multiPlayer.stop();
                        isPausedByFocusLossTransient = true;
                    }
                    break;
                // 瞬间丢失焦点，如通知
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // 音量减小为一半
                    volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (willPlay() && volume > 0) {
                        mVolumeWhenFocusLossTransientCanDuck = volume;
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mVolumeWhenFocusLossTransientCanDuck / 2,
                                AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    }
                    break;
            }
        }

        private boolean willPlay() {
            return service.multiPlayer.isPlay();
        }
    }
}
