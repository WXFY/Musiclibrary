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
import com.zyf.music.adapter.SongListAdapter;
import com.zyf.music.model.Song;


import java.util.ArrayList;
import java.util.List;


public class SongSelectorPicker {
    private Dialog datePickerDialog;
    private SongListAdapter adapter;

    /**
     * 定义结果回调接口
     */
    public interface ResultHandler {
        void handle(int pos);
    }

    private ResultHandler handler;
    private Context context;

    private TextView tvTitle;
    ImageView tv_cancle;

    private RecyclerView recycler;
    private List<Song> data;
    public SongSelectorPicker(Context context, ResultHandler handler,List<Song> data) {
        this.handler = handler;
        this.context = context;
        this.data = data;
        initDialog();
        initView();
    }

    private void initDialog() {
        if (datePickerDialog == null) {
            datePickerDialog = new Dialog(context, R.style.time_dialog);
            datePickerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            datePickerDialog.setContentView(R.layout.song_selector_packer);
            Window window = datePickerDialog.getWindow();
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
        tv_cancle = datePickerDialog.findViewById(R.id.tv_cancle);
        tvTitle =  datePickerDialog.findViewById(R.id.tv_title);
        tvTitle.setText("专辑名称");
        tv_cancle.setOnClickListener(view -> datePickerDialog.dismiss());
        recycler = datePickerDialog.findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(context));
        recycler.setAdapter(adapter = new SongListAdapter(data));
        adapter.setOnItemClickListener((BaseQuickAdapter adapter, View view, int position)->{
            if(handler!=null){
                handler.handle(position);
            }
            datePickerDialog.dismiss();
        });
    }

    public void show() {
        datePickerDialog.show();
    }

}
