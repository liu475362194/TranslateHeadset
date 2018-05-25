package com.example.liu.translate.services;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import com.baidu.speech.EventManager;
import com.baidu.speech.EventManagerFactory;
import com.baidu.speech.asr.SpeechConstant;
import com.example.liu.translate.TranslateActivity;
import com.example.liu.translate.activity.CameraActivity;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;


public class BaiDuSpeekService extends Service implements com.baidu.speech.EventListener{
    private EventManager eventManager;
    private SpeekBinder speekBinder = new SpeekBinder();
    private SpeekResultListener mSpeekResultListener;
    private int who;
//    private Context context;

    public interface SpeekResultListener{
        void startSpeek(int who);
        void showResult(String string);
        void successSpeek(String string);
        void volumeChange(String string);
    }

    public void setSpeekResultListener(SpeekResultListener speekResultListener){
        mSpeekResultListener = speekResultListener;
    }

    public class SpeekBinder extends Binder {
        public void start(String string){
            Log.d(TAG, "onEvent:speek start()");
            startSpeek(string);
        }
        public void init(Context context, SpeekResultListener speekResultListener){
            initSpeek(context);
            mSpeekResultListener = speekResultListener;
        }

        public void takePhoto(){
            CameraActivity.getInstance().takePhoto();
        }

    }

    private void startSpeek(String string){
        Map<String, Object> params = new LinkedHashMap<String, Object>();
        String event = null;
        event = SpeechConstant.ASR_START; // 替换成测试的event

//        params.put(SpeechConstant)
        if (string.equals("zh")){
            params.put(SpeechConstant.PID,1536);
            who = 0;
        }else{
            params.put(SpeechConstant.PID,1736);
            who = 1;
        }
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        params.put(SpeechConstant.VAD,SpeechConstant.VAD_DNN);

        String json = null; //可以替换成自己的json
        json = new JSONObject(params).toString(); // 这里可以替换成你需要测试的json
        eventManager.send(event, json, null, 0, 0);
    }

    public BaiDuSpeekService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return speekBinder;
    }

    private void initSpeek(Context context){
        eventManager = EventManagerFactory.create(context,"asr");
        eventManager.registerListener(this);
    }

    @Override
    public void onEvent(String s, String s1, byte[] bytes, int i, int i1) {
        Log.d(TAG, "onEvent:speek s " + s + " s1 " + s1);
        if (s.equals(SpeechConstant.CALLBACK_EVENT_ASR_VOLUME)){
            mSpeekResultListener.volumeChange(s1);
        }
        if (s.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)){
            mSpeekResultListener.showResult(s1);
        }
        if(s.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)){
            // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
            Log.d(TAG, "onEvent:speek " + s);
            mSpeekResultListener.startSpeek(who);
        }
        if(s.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)){
            // 识别结束
            mSpeekResultListener.successSpeek(s1);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }
}
