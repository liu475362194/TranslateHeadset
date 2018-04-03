package com.example.liu.translateheadset.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

/**
 * 屏幕帮助类
 *
 * Created by liu on 2018/3/9.
 */

public class ScreenUtils {
    private static final String TAG = "ScreenUtils";

    /**
     * 获取屏幕宽度
     * @param context
     * @return
     */
//    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        Log.d(TAG, "获取到屏幕宽度为: " + dm.widthPixels);
        return dm.widthPixels;
//        return ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
//                .getWidth();

    }

    /**
     * 获取屏幕高度
     * @param context
     * @return
     */
//    @SuppressWarnings("deprecation")
    public static int getScreenHeight(Context context){
        DisplayMetrics dm = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(dm);
        Log.d(TAG, "获取到屏幕高度为: " + dm.heightPixels);

        //获取status_bar_height资源的ID
        int statusBarHeight = 0;
        int resourcesId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourcesId > 0){
            statusBarHeight = context.getResources().getDimensionPixelSize(resourcesId);
            Log.d(TAG, "状态栏高度: " + statusBarHeight);
        }
        return dm.heightPixels - statusBarHeight;
//        return ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay()
//                .getHeight();
    }

}
