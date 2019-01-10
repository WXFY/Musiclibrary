package com.zyf.music;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.zyf.music.model.Song;
import com.zyf.music.musiclibrary.listener.OnProgressListener;
import com.zyf.music.musiclibrary.utils.MusicPlayer;
import com.zyf.music.musiclibrary.utils.PlaybackMode;
import com.zyf.music.widget.LrcView;

import java.io.File;


public class MainActivity extends AppCompatActivity {

    AppCompatSeekBar seekbar;
    TextView startTime;
    TextView endTime;
    TextView name;
    Button mode;
    LrcView lrc;
    LrcView lrcFull;
    private boolean lrcSingen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.play).setOnClickListener((v)->{
            MusicPlayer.playOrPause();
        });
        findViewById(R.id.last).setOnClickListener((v)->{
            MusicPlayer.previous();
        });
        findViewById(R.id.next).setOnClickListener((v)->{
            MusicPlayer.next();
        });
        name = findViewById(R.id.name);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        seekbar = findViewById(R.id.seekbar);
        mode = findViewById(R.id.mode);
        lrc = findViewById(R.id.lrc_sing);
        lrcFull = findViewById(R.id.lrc_full);
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
                lrcFull.setVisibility(View.GONE);
            }
        });
        lrc.setOnTouchListener((v, event) -> {
            lrc.setVisibility(View.GONE);
            lrcFull.setVisibility(View.VISIBLE);
            return false;
        });
        boolean play = getIntent().getBooleanExtra("play",false);
        if(play){
            MusicPlayer.playOrPause();
        }

       /* List<Song> data = new ArrayList<>();
        Song song1 = new Song(); //最美情侣
        song1.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112002239.mp3");
        song1.setName("最美情侣");
        data.add(song1);


        Song song2 = new Song(); //红昭愿
        song2.setPath("http://sc1.111ttt.cn/2018/1/03/13/396131227447.mp3");
        song2.setName("红昭愿");
        data.add(song2);


        Song song3 = new Song(); //追光者
        song3.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112002347.mp3");
        song3.setName("追光者");
        data.add(song3);

        Song song4 = new Song(); //远走高飞
        song4.setPath("http://sc1.111ttt.cn/2017/1/05/09/298092036393.mp3");
        song4.setName("远走高飞");
        data.add(song4);

        Song song5 = new Song(); //带你去旅行
        song5.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112004168.mp3");
        song5.setName("带你去旅行");
        data.add(song5);
        MusicPlayer.setList(data,4);*/
        name.setText(((Song)MusicPlayer.getCurrentSong()).getName());
        showLyric(((Song)MusicPlayer.getCurrentSong()).getLrc());
        mode.setText(currentSongMode(MusicPlayer.getMode()));
        mode.setOnClickListener(v -> {
            MusicPlayer.changeMode();
            mode.setText(currentSongMode(MusicPlayer.getMode()));
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
                    name.setText(((Song)song).getName());
                    showLyric(((Song)song).getLrc());
                }
            }
        });

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
    private String currentSongMode(PlaybackMode mode) {
        String modeStr = "列表顺序";
        switch (mode){
            case SINGLESONG:
                modeStr = "单曲循环";
                break;
            case RANDOM:
                modeStr = "随机播放";
                break;
            case LISTLOOP:
                modeStr = "列表循环";
                break;
            case LISTORDER:
                modeStr = "列表顺序";
                break;
        }
        return modeStr;
    }

}
