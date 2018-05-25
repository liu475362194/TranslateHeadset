package com.example.liu.translate.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.example.liu.translate.listener.MessageListener;
import com.example.liu.translate.util.MeizuClassicBluetooth;

public class BaiDuTTSService extends Service {

    public static final String APP_ID = "10367138";
    public static final String APP_KEY = "iWnr987Eee4iV7mE5tgWSTw3";
    public static final String APP_SECRET = "CiyNSiFrjwZ8lv60jFRTOmCbB0hMu8S2";

    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;

    private static final String TAG = "BaiDuTTSService";

//    protected Handler mainHandler;

    // ===============初始化参数设置完毕，更多合成参数请至getParams()方法中设置 =================
    protected SpeechSynthesizer mSpeechSynthesizer;

    private GetTts getTtsBinder = new GetTts();

    public BaiDuTTSService() {

    }

    public class GetTts extends Binder {
        //开始朗读
        public void speak(String text) {
                mSpeechSynthesizer.synthesize(text);
        }

        public void mInitTts(Context context) {
            initTts(context);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return getTtsBinder;
    }


    /**
     * 初始化语音合成
     */
    private void initTts(Context context) {
        SpeechSynthesizerListener listener = new MessageListener();
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(this);
        mSpeechSynthesizer.setSpeechSynthesizerListener(listener);
        try {
            ApplicationInfo info = context.getPackageManager()
                    .getApplicationInfo(getPackageName(),
                            PackageManager.GET_META_DATA);

            if (info.metaData.getInt("com.baidu.speech.APP_ID") != 0) {
                String appId = String.valueOf(info.metaData.getInt("com.baidu.speech.APP_ID"));
                String appKey = info.metaData.getString("com.baidu.speech.API_KEY");
                String appSecret = info.metaData.getString("com.baidu.speech.SECRET_KEY");
                Log.d(TAG, "initTts:APP_ID " + appId + " , " + appKey + " , " + appSecret);
                mSpeechSynthesizer.setAppId(appId);
                mSpeechSynthesizer.setApiKey(appKey, appSecret);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        // 以下setParam 参数选填。不填写则默认值生效
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "2"); // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9"); // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");// 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");// 设置合成的语调，0-9 ，默认 5

//        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        mSpeechSynthesizer.initTts(ttsMode);
    }


    @Override
    public void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
//            print("释放资源成功");
        }
        super.onDestroy();
    }
}
