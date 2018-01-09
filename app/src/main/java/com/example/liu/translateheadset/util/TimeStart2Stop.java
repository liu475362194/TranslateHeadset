package com.example.liu.translateheadset.util;

import android.content.Context;
import android.util.Log;

/**
 * Created by pzbz025 on 2018/1/8.
 */

public class TimeStart2Stop {
    private static final String TAG = "TimeStart2Stop";

    //求时间戳
    public static long timeNeed(Context context, String address, long lastTime) {
        boolean isRun = false;
        if (isRun) {
            if (lastTime == -1) {
                Log.d(TAG, context.getClass().getSimpleName() + "--" + address + " 计时开始...");
                return System.currentTimeMillis();
            } else {
                long time = System.currentTimeMillis() - lastTime;
                Log.d(TAG, context.getClass().getSimpleName() + "--" + address + " 总共耗时: " + time);
                return time;
            }
        }
        return -1;
    }
}
