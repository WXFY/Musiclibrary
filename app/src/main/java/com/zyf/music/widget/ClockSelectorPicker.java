package com.zyf.music.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.zyf.music.R;
import com.zyf.music.adapter.ClockAdapter;
import com.zyf.music.utils.ClockSongUtils;

import java.util.ArrayList;
import java.util.List;


public class ClockSelectorPicker {
    private Dialog pickerDialog;
    private ClockAdapter adapter;

    /**
     * 定义结果回调接口
     */
    public interface ResultHandler {
        void handle(boolean clock);
    }

    private ResultHandler handler;
    private Context context;

    private TextView tvTime;
    ImageView tv_cancle;

    private RecyclerView recycler;
    private List<String> data;
    public ClockSelectorPicker(Context context, ResultHandler handler) {
        this.handler = handler;
        this.context = context;
        data = new ArrayList<>();
        data.add("关闭定时");
        data.add("15分钟");
        data.add("30分钟");
        data.add("1小时");
        data.add("2小时");
        initDialog();
        initView();
    }

    private void initDialog() {
        if (pickerDialog == null) {
            pickerDialog = new Dialog(context, R.style.time_dialog);
            pickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            pickerDialog.setContentView(R.layout.clock_selector_packer);
            Window window = pickerDialog.getWindow();
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setGravity(Gravity.BOTTOM);
            WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            manager.getDefaultDisplay().getMetrics(dm);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = dm.widthPixels;
            window.setAttributes(lp);
        }
    }

    private void initView() {
        tv_cancle = pickerDialog.findViewById(R.id.tv_cancle);
        tvTime =  pickerDialog.findViewById(R.id.tv_time);
        tvTime.setText("暂未设定时长");
        ClockSongUtils.setLinsenter(new ClockSongUtils.OnClockLinsenter() {
            @Override
            public void onTick(String millisUntilFinished) {
                tvTime.setText(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                tvTime.setText("暂未设定时长");
                if(adapter!=null){
                    ClockSongUtils.pos = 0;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        tv_cancle.setOnClickListener(view -> pickerDialog.dismiss());
        recycler = pickerDialog.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapter = new ClockAdapter(data));
        adapter.setOnItemClickListener((BaseQuickAdapter adapters, View view, int position)->{
            ClockSongUtils.pos = position;
            if(position==0){
                ClockSongUtils.isClock = false;
                tvTime.setText("暂未设定时长");
                ClockSongUtils.stopTime();
                if(handler!=null){
                    handler.handle(false);
                }
            }else {
                if(handler!=null){
                    handler.handle(true);
                }
                ClockSongUtils.isClock = true;
                switch (position){
                    case 1:
                        ClockSongUtils.startTime(1*60*1000);
                        break;
                    case 2:
                        ClockSongUtils.startTime(30*60*1000);
                        break;
                    case 3:
                        ClockSongUtils.startTime(60*60*1000);
                        break;
                    case 4:
                        ClockSongUtils.startTime(120*60*1000);
                        break;
                }
                ClockSongUtils.start();
            }
            adapter.notifyDataSetChanged();
        });
    }

    public void show() {
        pickerDialog.show();
    }

}
