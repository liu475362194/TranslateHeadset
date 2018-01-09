package com.example.liu.translateheadset.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.liu.translateheadset.R;
import com.example.liu.translateheadset.activity.ActivityCollector;
import com.example.liu.translateheadset.activity.BLEConnect2Activity;
import com.example.liu.translateheadset.activity.BaseActivity;
import com.example.liu.translateheadset.activity.MainActivity;
import com.example.liu.translateheadset.services.BLEService;
import com.example.liu.translateheadset.util.TimeStart2Stop;

/**
 * Created by pzbz025 on 2017/12/27.
 */

public class LayoutTitleBar extends RelativeLayout {
    private Button titleBarLeftBtn;
    private ImageView titleBarRightBtn;
    private TextView titleBarTitle;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private static final String TAG = "LayoutTitleBar";

    public LayoutTitleBar(Context context) {
        super(context);
    }

    @SuppressLint("ResourceAsColor")
    public LayoutTitleBar(final Context context, AttributeSet attrs) {
        super(context, attrs);
        long last = TimeStart2Stop.timeNeed(context,"LayoutTitleBar",-1);
        LayoutInflater.from(context).inflate(R.layout.layout_title_bar, this, true);
        titleBarLeftBtn = (Button) findViewById(R.id.title_bar_left);
        titleBarRightBtn = findViewById(R.id.title_bar_right);
        titleBarTitle = (TextView) findViewById(R.id.title_bar_title);

        titleBarLeftBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG, "onClick: " + context.getClass().getSimpleName());
                ((AppCompatActivity)context).finish();
            }
        });

        titleBarRightBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, BLEConnect2Activity.class));
            }
        });

        localBroadcastManager = LocalBroadcastManager.getInstance(context);


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.CONNECTED");
        ConnectReceiver connectReceiver = new ConnectReceiver();
        localBroadcastManager.registerReceiver(connectReceiver, intentFilter);

        IntentFilter disconnectIntentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.DISCONNECT");
        DisconnectReceiver disconnectReceiver = new DisconnectReceiver();
        localBroadcastManager.registerReceiver(disconnectReceiver, disconnectIntentFilter);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LayoutTitleBar);
        if (typedArray != null) {
            //titleBar背景色
            int titleBarBackGround = typedArray.getResourceId(R.styleable.LayoutTitleBar_title_background_color, Color.BLACK);
            setBackgroundColor(titleBarBackGround);

            //获取是否要显示左边按钮
            boolean leftButtonVisible = typedArray.getBoolean(R.styleable.LayoutTitleBar_left_button_visible, true);
            if (leftButtonVisible) {
                titleBarLeftBtn.setVisibility(View.VISIBLE);
            } else {
                titleBarLeftBtn.setVisibility(View.INVISIBLE);
            }
            //设置左边按钮的文字
            String leftButtonText = typedArray.getString(R.styleable.LayoutTitleBar_left_button_text);
            if (!TextUtils.isEmpty(leftButtonText)) {
                titleBarLeftBtn.setText(leftButtonText);
                //设置左边按钮文字颜色
                int leftButtonTextColor = typedArray.getColor(R.styleable.LayoutTitleBar_left_button_text_color, Color.WHITE);
                titleBarLeftBtn.setTextColor(leftButtonTextColor);
            } else {
                //设置左边图片icon 这里是二选一 要么只能是文字 要么只能是图片
                int leftButtonDrawable = typedArray.getResourceId(R.styleable.LayoutTitleBar_left_button_drawable, R.drawable.back);
                if (leftButtonDrawable != -1) {
                    titleBarLeftBtn.setBackgroundResource(leftButtonDrawable);
                }
            }

            //先获取标题是否要显示图片icon
            int titleTextDrawable = typedArray.getResourceId(R.styleable.LayoutTitleBar_title_text_drawable, -1);
            if (titleTextDrawable != -1) {
                titleBarTitle.setBackgroundResource(titleTextDrawable);
            } else {
                //如果不是图片标题 则获取文字标题
                String titleText = typedArray.getString(R.styleable.LayoutTitleBar_title_text);
                if (!TextUtils.isEmpty(titleText)) {
                    titleBarTitle.setText(titleText);
                }
                //获取标题显示颜色
                int titleTextColor = typedArray.getColor(R.styleable.LayoutTitleBar_title_text_color, Color.WHITE);
                titleBarTitle.setTextColor(titleTextColor);
            }

            //获取是否要显示右边按钮
            boolean rightButtonVisible = typedArray.getBoolean(R.styleable.LayoutTitleBar_right_button_visible, true);
            if (rightButtonVisible) {
                titleBarRightBtn.setVisibility(View.VISIBLE);
            } else {
                titleBarRightBtn.setVisibility(View.INVISIBLE);
            }
//            //设置右边按钮的文字
//            String rightButtonText = typedArray.getString(R.styleable.LayoutTitleBar_right_button_text);
//            if (!TextUtils.isEmpty(rightButtonText)) {
//                titleBarRightBtn.setText(rightButtonText);
//                //设置右边按钮文字颜色
//                int rightButtonTextColor = typedArray.getColor(R.styleable.LayoutTitleBar_right_button_text_color, Color.BLUE);
//                titleBarRightBtn.setTextColor(rightButtonTextColor);
//            } else {
            //设置右边图片icon 这里是二选一 要么只能是文字 要么只能是图片
            int rightButtonDrawable = typedArray.getResourceId(R.styleable.LayoutTitleBar_right_button_src, -1);
            if (!BLEService.isBleSuccess())
                titleBarRightBtn.setImageDrawable(getResources().getDrawable(R.drawable.home_disconnected_pressed));
            else
                titleBarRightBtn.setImageDrawable(getResources().getDrawable(R.drawable.home_connect_pressed));
            if (rightButtonDrawable != -1) {
                titleBarRightBtn.setImageDrawable(getResources().getDrawable(rightButtonDrawable));
            }
//            }
            typedArray.recycle();
        }

        TimeStart2Stop.timeNeed(context,"LayoutTitleBar",last);
    }

    class ConnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            titleBarRightBtn.setImageDrawable(getResources().getDrawable(R.drawable.home_connect_pressed));
        }
    }

    class DisconnectReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            titleBarRightBtn.setImageDrawable(getResources().getDrawable(R.drawable.home_disconnected_pressed));
        }
    }

    public void setTitleClickListener(OnClickListener onClickListener) {
        if (onClickListener != null) {
            titleBarLeftBtn.setOnClickListener(onClickListener);
            titleBarRightBtn.setOnClickListener(onClickListener);
        }
    }

    public Button getTitleBarLeftBtn() {
        return titleBarLeftBtn;
    }

    public ImageView getTitleBarRightBtn() {
        return titleBarRightBtn;
    }

    public TextView getTitleBarTitle() {
        return titleBarTitle;
    }
}
