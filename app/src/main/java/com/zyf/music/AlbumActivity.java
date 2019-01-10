package com.zyf.music;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zyf.music.adapter.SongListAdapter;
import com.zyf.music.model.Song;
import com.zyf.music.musiclibrary.utils.MusicPlayer;

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {
    private ImageView topImage;
    private RecyclerView recycler;
    private ImageView albumImage;
    private LinearLayout topBg;
    private TextView title;
    private SongListAdapter adapter;
    MusicPlayer.ServiceToken token;
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
        title.setTextColor(0xFFFFFF);
        title.setText("页面名称");
        recycler.setLayoutManager(new LinearLayoutManager(this));
        List<Song> data = new ArrayList<>();
        Song song1 = new Song(); //最美情侣
        song1.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112002239.mp3");
        song1.setName("最美情侣");
        song1.setLrc(getResources().getString(R.string.song1));
        data.add(song1);


        Song song2 = new Song(); //红昭愿
        song2.setPath("http://sc1.111ttt.cn/2018/1/03/13/396131227447.mp3");
        song2.setName("红昭愿");
        song2.setLrc(getResources().getString(R.string.song2));
        data.add(song2);


        Song song3 = new Song(); //追光者
        song3.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112002347.mp3");
        song3.setName("追光者");
        song3.setLrc(getResources().getString(R.string.song3));
        data.add(song3);

        Song song4 = new Song(); //远走高飞
        song4.setPath("http://sc1.111ttt.cn/2017/1/05/09/298092036393.mp3");
        song4.setName("远走高飞");
        song4.setLrc(getResources().getString(R.string.song4));
        data.add(song4);

        Song song5 = new Song(); //带你去旅行
        song5.setPath("http://sc1.111ttt.cn/2017/1/11/11/304112004168.mp3");
        song5.setName("带你去旅行");
        song5.setLrc(getResources().getString(R.string.song5));
        data.add(song5);


        recycler.setAdapter(adapter = new SongListAdapter(data));

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
    }
}
