package com.example.liu.translateheadset.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by pzbz025 on 2017/12/26.
 */

public class ActivityCollector {
    /**
     * 存放activity的列表
     */
    public static HashMap<Class<?>, Activity> activities = new LinkedHashMap<>();

//    public static void addActivity(Activity activity){
//        activities.add(activity);
//    }
//
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }
//
//    public static void finishAll(){
//        for (Activity activity : activities){
//            if (!activity.isFinishing()){
//                activity.finish();
//            }
//        }
//        activities.clear();
//    }



    /**
     * 添加Activity
     *
     * @param activity
     */
    public static void addActivity(Activity activity, Class<?> clz) {
        activities.put(clz, activity);
    }

    /**
     * 判断一个Activity 是否存在
     *
     * @param clz
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static <T extends Activity> boolean isActivityExist(Class<T> clz) {
        boolean res;
        Activity activity = getActivity(clz);
        if (activity == null) {
            res = false;
        } else {
            if (activity.isFinishing() || activity.isDestroyed()) {
                res = false;
            } else {
                res = true;
                Log.d("111", "isActivityExist: ");
            }
        }

        return res;
    }

    /**
     * 移除所有的Activity
     */
    public static void removeAllActivity() {
        if (activities != null && activities.size() > 0) {
            Set<Map.Entry<Class<?>, Activity>> sets = activities.entrySet();
            for (Map.Entry<Class<?>, Activity> s : sets) {
                if (!s.getValue().isFinishing()) {
                    s.getValue().finish();
                }
            }
        }
        activities.clear();
    }


    /**
     * 获得指定activity实例
     *
     * @param clazz Activity 的类对象
     * @return
     */
    public static <T extends Activity> T getActivity(Class<T> clazz) {
        return (T) activities.get(clazz);
    }




//    public static boolean activityInForeground(Activity activity) {
//        return ((BaseActivity) activity).isForegroud();
//    }

//    public static boolean hasActivityInForeground(Activity activity){
//        return activityInForeground(activity);
//    }
}
