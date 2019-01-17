package com.zyf.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.zyf.music.adapter.SongListAdapter;
import com.zyf.music.model.Song;
import com.zyf.music.musiclibrary.listener.OnProgressListener;
import com.zyf.music.musiclibrary.utils.MusicFileUtils;
import com.zyf.music.musiclibrary.utils.MusicPlayer;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class AlbumActivity extends AppCompatActivity {
    private ImageView topImage;
    private RecyclerView recycler;
    private ImageView albumImage;
    private LinearLayout topBg;
    private TextView title;
    private SongListAdapter adapter;
    MusicPlayer.ServiceToken token;

    private MusicBroadcastReceiver broadcastReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        token = MusicPlayer.bindToService(this,null);
        topImage = findViewById(R.id.top_image);
        albumImage = findViewById(R.id.album_image);
        recycler = findViewById(R.id.recycler);
        topBg = findViewById(R.id.top_bg);
        topBg.setPadding(0, getStatusBarHeight(), 0, 0);
        title = findViewById(R.id.tv_title);
        title.setTextColor(0xFFFFFFFF);
        title.setText("页面名称");
        recycler.setLayoutManager(new LinearLayoutManager(this));

        broadcastReceiver = new MusicBroadcastReceiver();
        IntentFilter filter = new IntentFilter(MusicFileUtils.MESSAGECILCK);
        registerReceiver(broadcastReceiver, filter);

        List<Song> data = new ArrayList<>();
        Song song1 = new Song(); //最美情侣
        song1.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112002239.mp3");
        song1.setName("最美情侣");
        song1.setAuthor("白小白");
        song1.setTime("04:01");
        song1.setLrc(getResources().getString(R.string.song1));
        data.add(song1);


        Song song2 = new Song(); //红昭愿
        song2.setPath("http://sc1.111ttt.cn/2018/1/03/13/396131227447.mp3");
        song2.setName("红昭愿");
        song2.setAuthor("未知歌手");
        song2.setTime("02:53");
        song2.setLrc(getResources().getString(R.string.song2));
        data.add(song2);


        Song song3 = new Song(); //追光者
        song3.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112002347.mp3");
        song3.setName("追光者");
        song3.setAuthor("岑宁儿");
        song3.setTime("03:55");
        song3.setLrc(getResources().getString(R.string.song3));
        data.add(song3);

        Song song4 = new Song(); //远走高飞
        song4.setPath("http://sc1.111ttt.cn/2017/1/05/09/298092036393.mp3");
        song4.setName("远走高飞");
        song4.setAuthor("金志文");
        song4.setTime("03:55");
        song4.setLrc(getResources().getString(R.string.song4));
        data.add(song4);

        Song song5 = new Song(); //带你去旅行
        song5.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112004168.mp3");
        song5.setName("带你去旅行");
        song5.setAuthor("校长");
        song5.setTime("03:45");
        song5.setLrc(getResources().getString(R.string.song5));
        data.add(song5);


        Song song6 = new Song(); //9420
        song6.setPath("http://sc1.111ttt.cn/2018/1/03/13/396131225385.mp3");
        song6.setName("9420");
        song6.setAuthor("麦小兜");
        song6.setLrc("暂无歌词");
        song6.setTime("03:49");
        data.add(song6);

        Song song7 = new Song();
        song7.setPath("http://www.ytmp3.cn/down/50491.mp3");
        song7.setName("年少有为");
        song7.setAuthor("李荣浩");
        song7.setLrc("暂无歌词");
        song7.setTime("04:39");
        data.add(song7);

        Song song8 = new Song();
        song8.setPath("http://sc1.111ttt.cn/2017/1/05/09/298092041338.mp3");
        song8.setName("我不相信");
        song8.setAuthor("庄心妍");
        song8.setLrc("暂无歌词");
        song8.setTime("04:24");
        data.add(song8);
        recycler.setAdapter(adapter = new SongListAdapter(data));
        MusicPlayer.setMusicIcon(BitmapFactory.decodeResource(getResources(), R.drawable.lrc_play));
        adapter.setOnItemClickListener((adapter, view, position) -> {
            Intent intent = new Intent(AlbumActivity.this,MainActivity.class);
            if(MusicPlayer.isPlaying()&&MusicPlayer.getPos()==position){
                intent.putExtra("play",false);
            }else {
                MusicPlayer.setList(adapter.getData(),position);
                intent.putExtra("play",true);
            }
            startActivity(intent);
        });
        MusicPlayer.setListener(this.getClass(), new OnProgressListener() {
            @Override
            public void onLongProgress(long duration, long current) {

            }

            @Override
            public void onStringProgress(String duration, String current) {

            }

            @Override
            public void onBufferingUpdate(int percent) {

            }

            @Override
            public void onCurrentSong(Object song) {
                if(adapter!=null){
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onError() {

            }

            @Override
            public void onStart() {

            }
        });
        Glide.with(this).load(R.mipmap.default_music_icon).apply(RequestOptions.bitmapTransform(new BlurTransformation(4, 15))).into(topImage);
       /* Glide.with(this).load(R.mipmap.default_music_icon).bitmapTransform(new BlurTransformation(this, 15)).into(topImage);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.notifyDataSetChanged();
        }
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
        MusicPlayer.unbindFromService(token);
        token = null;
        android.os.Process.killProcess(android.os.Process.myPid());
        unregisterReceiver(broadcastReceiver);
    }
    public class MusicBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            if(MusicPlayer.getCurrentSong()!=null){
                startActivity(new Intent(AlbumActivity.this,MainActivity.class));
            }
        }
    }
}
