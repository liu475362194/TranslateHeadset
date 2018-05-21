package com.example.liu.translateheadset.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.baidu.speech.EventListener;
import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

public class BaiduWakeUpService extends Service implements EventListener {

    private EventManager wakeUp;
    private WakeUp wakeUpBinder = new WakeUp();

    private static final String TAG = "BaiduWakeUpService";

    private WakeUpListener mWakeUpListener;

    public interface WakeUpListener{
        void SuccessWakeup(String string);
    }

    @Override
    public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {
        Log.d(TAG, "onEvent:wakeup s = " + s + " s1 = " + s1 +" byte = " + bytes + " i = " + i + " i1 = " + i1);
        if (s1 != null)
        mWakeUpListener.SuccessWakeup(s1);
    }

    public class WakeUp extends Binder {
        public void init(Context context,WakeUpListener wakeUpListener) {
            initWakeUp(context);
            mWakeUpListener = wakeUpListener;
            start();
        }

        public void startWakeUp(){
            start();
        }

        public void stopWakeUp(){
            stop();
        }

    }

    public BaiduWakeUpService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return wakeUpBinder;
    }

    private void initWakeUp(Context context) {
        wakeUp = EventManagerFactory.create(context, "wp");
        wakeUp.registerListener(this);
    }

    private void start() {
        Map<String, Object> params = new LinkedHashMap<String, Object>();

        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, false);
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        String json = null; // 这里可以替换成你需要测试的json
        json = new JSONObject(params).toString();
        wakeUp.send(SpeechConstant.WAKEUP_START, json, null, 0, 0);
    }

    private void stop(){
        wakeUp.send(SpeechConstant.WAKEUP_STOP, "{}", null, 0, 0);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeUp.send(SpeechConstant.WAKEUP_STOP, "{}", null, 0, 0);
    }
}
