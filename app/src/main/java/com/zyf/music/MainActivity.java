package com.zyf.music;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.request.RequestOptions;
import com.zyf.music.model.Song;
import com.zyf.music.musiclibrary.listener.OnProgressListener;
import com.zyf.music.musiclibrary.utils.MusicPlayer;
import com.zyf.music.musiclibrary.utils.PlaybackMode;
import com.zyf.music.widget.ClockSelectorPicker;
import com.zyf.music.widget.LrcView;
import com.zyf.music.widget.SongSelectorPicker;

import java.io.File;
import java.security.MessageDigest;
import java.util.List;

import me.bogerchan.niervisualizer.NierVisualizerManager;
import me.bogerchan.niervisualizer.renderer.IRenderer;
import me.bogerchan.niervisualizer.renderer.circle.CircleBarRenderer;
import me.bogerchan.niervisualizer.util.NierAnimator;

import static com.zyf.music.utils.ClockSongUtils.isClock;


public class MainActivity extends AppCompatActivity {

    AppCompatSeekBar seekbar;
    TextView startTime;
    TextView endTime;
    ImageView mode;
    LrcView lrc;
    LrcView lrcFull;

    TextView title;
    LinearLayout topBg;
    ImageView ivBack;
    ImageView playIcon;
    ImageView playAlbumIcon;
    ImageView clock;

    SurfaceView sv_wave;

    int mediaPlayerId;
    private ValueAnimator rotateAnimator;
    final NierVisualizerManager visualizerManager = new NierVisualizerManager();

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playIcon = findViewById(R.id.play);
        sv_wave = findViewById(R.id.sv_wave);
        sv_wave. setZOrderOnTop(true);
        sv_wave.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        playIcon.setOnClickListener((v)->{
            MusicPlayer.playOrPause();
            if(MusicPlayer.isPlaying()){
                rotateAnimator.resume();
                playIcon.setImageResource(R.mipmap.pause_icon);
            }else {
                rotateAnimator.pause();
                playIcon.setImageResource(R.mipmap.play_icon);
            }
        });
        findViewById(R.id.last).setOnClickListener((v)->{
            MusicPlayer.previous();
            resetTime();
        });
        findViewById(R.id.next).setOnClickListener((v)->{
            MusicPlayer.next();
            resetTime();
        });
        topBg = findViewById(R.id.top_bg);
        topBg.setPadding(0, getStatusBarHeight(), 0, 0);

        title = findViewById(R.id.tv_title);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        seekbar = findViewById(R.id.seekbar);
        mode = findViewById(R.id.mode);
        lrc = findViewById(R.id.lrc_sing);
        ivBack = findViewById(R.id.iv_back);
        lrcFull = findViewById(R.id.lrc_full);
        ivBack.setImageResource(R.mipmap.black_back);
        playAlbumIcon = findViewById(R.id.play_icon);
        clock = findViewById(R.id.clock);
        ivBack.setOnClickListener(v -> {
            finish();
        });
        clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Glide.with(this).load(R.mipmap.default_music_icon).thumbnail(0.1f).apply(RequestOptions.bitmapTransform(
                new BitmapTransformation() {
                    @Override
                    public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {

                    }

                    @Override
                    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
                        if(toTransform == null){
                            return null;
                        }
                        int size = Math.min(toTransform.getWidth(), toTransform.getHeight());
                        int x = (toTransform.getWidth() - size) / 2;
                        int y = (toTransform.getHeight() - size) / 2;

                        // TODO this could be acquired from the pool too
                        Bitmap squared = Bitmap.createBitmap(toTransform, x, y, size, size);

                        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
                        if (result == null) {
                            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
                        }
                        Canvas canvas = new Canvas(result);
                        Paint paint = new Paint();
                        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
                        paint.setAntiAlias(true);
                        float r = size / 2f;
                        canvas.drawCircle(r, r, r, paint);
                        return result;
                    }
                }
        )).into(playAlbumIcon);
//        showLyric(new File(Environment.getExternalStorageDirectory(), "最美情侣.lrc"));
        lrcFull.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
            @Override
            public boolean onPlayClick(long time) {
                MusicPlayer.seek(time);
                return true;
            }

            @Override
            public void onOutClick() {
                lrc.setVisibility(View.VISIBLE);
                lrcFull.setVisibility(View.INVISIBLE);
                playAlbumIcon.setVisibility(View.VISIBLE);
            }
        });
        lrc.setOnTouchListener((v, event) -> {
            lrc.setVisibility(View.INVISIBLE);
            lrcFull.setVisibility(View.VISIBLE);
            playAlbumIcon.setVisibility(View.INVISIBLE);
            return false;
        });
        boolean play = getIntent().getBooleanExtra("play",false);
        if(play){
            MusicPlayer.playOrPause();
        }
        openAnimation();
        initSong();
        resetTime();
        mode.setImageResource(currentSongMode(MusicPlayer.getMode()));
        mode.setOnClickListener(v -> {
            MusicPlayer.changeMode();
            mode.setImageResource(currentSongMode(MusicPlayer.getMode()));
        });
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    MusicPlayer.seek(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        MusicPlayer.setListener(this.getClass(), new OnProgressListener() {
            @Override
            public void onLongProgress(long duration, long current) {
                seekbar.setMax((int)duration);
                seekbar.setProgress((int)current);
                lrc.updateTime(current);
                lrcFull.updateTime(current);
            }

            @Override
            public void onStringProgress(String duration, String current) {
                startTime.setText(current);
                endTime.setText(duration);
            }

            @Override
            public void onBufferingUpdate(int percent) {
                seekbar.setSecondaryProgress(percent);
            }

            @Override
            public void onCurrentSong(Object song) {
                if(song!=null&&song instanceof Song){
                    showLyric(((Song)song).getLrc());
                    title.setText(((Song)song).getName());
                }
            }

            @Override
            public void onError() {
                runOnUiThread(()->{
                    Toast.makeText(MainActivity.this,"歌曲加载失败",Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onStart() {
                runOnUiThread(()->{
                    try {
                        Thread.sleep(50);
                        initSong();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
            @Override
            public void playerId(int playerId) {
                mediaPlayerId = playerId;
                final int state = visualizerManager.init(playerId);
                if (NierVisualizerManager.SUCCESS != state) {
                    // do something...
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setStrokeWidth(10f);
                    paint.setColor(Color.parseColor("#FEAEC9"));

                    visualizerManager.start(sv_wave, new IRenderer[]{new CircleBarRenderer(paint,4, CircleBarRenderer.Type.TYPE_A,
                            0.4f,1f,new NierAnimator(new LinearInterpolator(),10000,new float[]{0f,360f},true))});
                }
            }
        });
        findViewById(R.id.list_song).setOnClickListener(v->{
            SongSelectorPicker picker = new SongSelectorPicker(MainActivity.this,(pos)->{
                MusicPlayer.setList(MusicPlayer.getList(),pos);
                MusicPlayer.playOrPause();
                initSong();
                resetTime();
            },(List<Song>) (MusicPlayer.getList()));
            picker.show();
        });
        clock.setOnClickListener(v->{
            ClockSelectorPicker picker = new ClockSelectorPicker(MainActivity.this,(pos)->{
                if(!pos){
                    clock.setImageResource(R.mipmap.timing_icon);
                }else {
                    clock.setImageResource(R.mipmap.timing_select_icon);
                }
            });
            picker.show();
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initSong(){
        if(MusicPlayer.isPlaying()){
            rotateAnimator.resume();
            playIcon.setImageResource(R.mipmap.pause_icon);
        }else {
            rotateAnimator.pause();
            playIcon.setImageResource(R.mipmap.play_icon);
        }
        if(!isClock){
            clock.setImageResource(R.mipmap.timing_icon);
        }else {
            clock.setImageResource(R.mipmap.timing_select_icon);
        }
    }

    private void resetTime() {
        title.setText(((Song) MusicPlayer.getCurrentSong()).getName());
        showLyric(((Song)MusicPlayer.getCurrentSong()).getLrc());
        seekbar.setProgress(0);
        startTime.setText("00:00");
        endTime.setText("00:00");
    }

    private void openAnimation() {
        rotateAnimator = ObjectAnimator.ofFloat(playAlbumIcon,"rotation",0f,360f);
        rotateAnimator.setInterpolator(new LinearInterpolator());
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);
        rotateAnimator.setDuration(10000);
        rotateAnimator.start();
    }

    public void showLyric(File file) {
        if (file == null) {
            lrc.setLabel("暂无歌词");
            lrcFull.setLabel("暂无歌词");
        } else {
            lrc.loadLrc(file);
            lrcFull.loadLrc(file);

        }
    }
    public void showLyric(String lrcStr) {
        if (lrcStr == null) {
            lrc.setLabel("暂无歌词");
            lrcFull.setLabel("暂无歌词");
        } else {
            lrc.loadLrc(lrcStr);
            lrcFull.loadLrc(lrcStr);

        }
    }
    private int currentSongMode(PlaybackMode mode) {
        int modeRes = R.mipmap.list_loop_icon;
        switch (mode){
            case SINGLESONG:
                modeRes = R.mipmap.singensong_icon;
                break;
            case RANDOM:
                modeRes = R.mipmap.random_icon;
                break;
            case LISTLOOP:
                modeRes = R.mipmap.list_loop_icon;
                break;
        }
        return modeRes;
    }
    /**
     * 获取当前设备状态栏高度
     *
     * @return
     */
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(rotateAnimator!=null&&rotateAnimator.isRunning()){
            rotateAnimator.cancel();
            playAlbumIcon.clearAnimation();
        }
        visualizerManager.release();
    }

    @Override
    protected void onStop() {
        super.onStop();
        visualizerManager.stop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        visualizerManager.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        visualizerManager.resume();
    }
}
