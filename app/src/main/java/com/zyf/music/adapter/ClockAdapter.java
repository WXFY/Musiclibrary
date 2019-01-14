package com.zyf.music.adapter;

import android.support.annotation.Nullable;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zyf.music.R;
import com.zyf.music.utils.ClockSongUtils;

import java.util.List;


public class ClockAdapter extends BaseQuickAdapter<String,BaseViewHolder> {
    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.title,item);
        /*if(item.isOpen()){
            helper.setImageResource(R.id.checkbox,R.mipmap.check);
        }else {
            helper.setImageResource(R.id.checkbox,R.mipmap.no_check);
        }*/
    }

    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if(position==0){
            holder.getView(R.id.checkbox).setVisibility(View.GONE);
            return;
        }
        if(position == ClockSongUtils.pos){
            holder.setImageResource(R.id.checkbox,R.mipmap.check);
        }else {
            holder.setImageResource(R.id.checkbox,R.mipmap.no_check);
        }
    }

    public ClockAdapter(@Nullable List<String> data) {
        super(R.layout.week_item,data);
    }
}
