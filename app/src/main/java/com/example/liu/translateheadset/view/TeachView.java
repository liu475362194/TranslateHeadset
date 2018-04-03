package com.example.liu.translateheadset.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.example.liu.translateheadset.util.ScreenUtils;

/**
 * 说明界面
 * Created by liu on 2018/3/9.
 */

public class TeachView {

    private static final Object mLock = new Object();
    private static TeachView mInstance;
    private Context context;

    public TeachView setImageId(int imageId) {
        this.imageId = imageId;
        return mInstance;
    }

    private int imageId;

    public TeachView(Context context) {
        this.context = context;
    }

    public static TeachView getInstance(Context context) {
        synchronized (mLock) {
            if (mInstance == null) {
                mInstance = new TeachView(context.getApplicationContext());
            }
            return mInstance;
        }
    }


    //动态初始化图层
    private ImageView initStudyImg(){
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setImageResource(imageId);
        return imageView;
    }

    //配置LayoutParams参数
    private WindowManager.LayoutParams setLayoutParams(){
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        //设置显示的类型，TYPE_PHONE指的是来电话的时候会被覆盖，其他时候会在最前端，显示位置在stateBar下面。
        params.type = WindowManager.LayoutParams.TYPE_APPLICATION_STARTING;
        //设置显示格式
        params.format = PixelFormat.RGBA_8888;
        //设置对其方式
        params.gravity = Gravity.LEFT | Gravity.TOP;
        //设置宽高
        params.width = ScreenUtils.getScreenWidth(context);
        params.height = ScreenUtils.getScreenHeight(context);
        return params;
    }

    //应用教程窗口
    public void initStudyWindow(){

        final WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final ImageView imageView = initStudyImg();
        windowManager.addView(imageView, setLayoutParams());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(imageView);
            }
        });
    }
}
