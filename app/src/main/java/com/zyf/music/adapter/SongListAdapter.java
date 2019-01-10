package com.zyf.music.adapter;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyf.music.R;
import com.zyf.music.model.Song;
import com.zyf.music.musiclibrary.utils.MusicPlayer;
import com.zyf.music.widget.MusicIndicator;

import java.util.List;

public class SongListAdapter extends BaseQuickAdapter<Song,BaseViewHolder> {
    @Override
    protected void convert(BaseViewHolder helper, Song item) {
        helper.setText(R.id.tv_name,item.getName());
    }

    public SongListAdapter(@Nullable List<Song> data) {
        super(R.layout.songlist_item,data);
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.setText(R.id.tv_sore,(position+1)+"");
        MusicIndicator indicator = holder.getView(R.id.indicator);
        TextView tvTime = holder.getView(R.id.tv_time);
        if(position== MusicPlayer.getPos()){
            indicator.setVisibility(View.VISIBLE);
            tvTime.setVisibility(View.GONE);
        }else {
            indicator.setVisibility(View.GONE);
            tvTime.setVisibility(View.VISIBLE);
        }
    }
}
