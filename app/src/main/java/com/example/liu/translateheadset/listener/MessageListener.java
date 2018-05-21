package com.example.liu.translateheadset.listener;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.example.liu.translateheadset.DemoApplication;
import com.example.liu.translateheadset.util.MainHandlerConstant;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * SpeechSynthesizerListener 简单地实现，仅仅记录日志
 * Created by fujiayi on 2017/5/19.
 */

public class MessageListener implements SpeechSynthesizerListener, MainHandlerConstant {
    private static final String TAG = "MessageListener";
    ByteArrayOutputStream byteArrayOutputStream;
    InputStream inputStream;


    /**
     * 播放开始，每句播放开始都会回调
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeStart(String utteranceId) {
        PlayThread();
        byteArrayOutputStream = new ByteArrayOutputStream();
        sendMessage("准备开始合成,序列号:" + utteranceId);
    }

    /**
     * 语音流 16K采样率 16bits编码 单声道 。
     *
     * @param utteranceId
     * @param bytes       二进制语音 ，注意可能有空data的情况，可以忽略
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法和合成到第几个字对应。
     */
    @Override
    public void onSynthesizeDataArrived(String utteranceId, byte[] bytes, int progress) {


        try {
            byteArrayOutputStream.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "合成进度回调, progress：" + progress + ";bytes:" + bytes + " :progress "+ progress);
    }

    /**
     * 合成正常结束，每句合成正常结束都会回调，如果过程中出错，则回调onError，不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSynthesizeFinish(String utteranceId) {
//        data = byteArrayOutputStream.toByteArray();
        play(byteArrayOutputStream.toByteArray());
        SharedPreferences sp = DemoApplication.getInstance().getSharedPreferences("channel", Context.MODE_PRIVATE);
        boolean left = sp.getBoolean("left",true);
        boolean right = sp.getBoolean("right",true);
        setChannel(left,right);
        sendMessage("合成结束回调, 序列号:" + utteranceId);
    }

    @Override
    public void onSpeechStart(String utteranceId) {
        sendMessage("播放开始回调, 序列号:" + utteranceId);
    }

    /**
     * 播放进度回调接口，分多次回调
     *
     * @param utteranceId
     * @param progress    如合成“百度语音问题”这6个字， progress肯定是从0开始，到6结束。 但progress无法保证和合成到第几个字对应。
     */
    @Override
    public void onSpeechProgressChanged(String utteranceId, int progress) {
        //  Log.i(TAG, "播放进度回调, progress：" + progress + ";序列号:" + utteranceId );
    }

    /**
     * 播放正常结束，每句播放正常结束都会回调，如果过程中出错，则回调onError,不再回调此接口
     *
     * @param utteranceId
     */
    @Override
    public void onSpeechFinish(String utteranceId) {
        sendMessage("播放结束回调, 序列号:" + utteranceId);
    }

    /**
     * 当合成或者播放过程中出错时回调此接口
     *
     * @param utteranceId
     * @param speechError 包含错误码和错误信息
     */
    @Override
    public void onError(String utteranceId, SpeechError speechError) {
        sendErrorMessage("错误发生：" + speechError.description + "，错误编码："
                + speechError.code + "，序列号:" + utteranceId);
    }

    private void sendMessage(String message) {
        sendMessage(message, false);
    }

    private void sendErrorMessage(String message) {
        sendMessage(message, true);
    }

    protected void sendMessage(String message, boolean isError) {
        if (isError) {
            Log.e(TAG, message);
        } else {
            Log.i(TAG, message);
        }

    }

    AudioTrack mAudioTrack;
    // 采样率
    private int mSampleRateInHz = 16000;
    // 单声道
    private int mChannelConfig = AudioFormat.CHANNEL_OUT_MONO;

    public void PlayThread() {
        int bufferSize = AudioTrack.getMinBufferSize(mSampleRateInHz, mChannelConfig, AudioFormat.ENCODING_PCM_16BIT);
        mAudioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                mSampleRateInHz,
                mChannelConfig,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);
    }

    private void play(final byte[] data){
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (null != mAudioTrack)
                    mAudioTrack.play();
//                setChannel(false,true);
                // 播放进度
                int playIndex = 0;
                // 是否缓冲完成
                boolean isLoaded = false;
                // 缓冲 + 播放
                while (null != mAudioTrack && AudioTrack.PLAYSTATE_STOPPED != mAudioTrack.getPlayState()) {
                    // 字符长度
                    int len;
//                if (-1 != (len = inputStream.read(buffer))) {
//                    byteArrayOutputStream.write(buffer, 0, len);
//                    data = byteArrayOutputStream.toByteArray();
//                    Log.i(TAG, "run: 已缓冲 : " + data.length);
//                } else {
//                    // 缓冲完成
//                    isLoaded = true;
//                }

                    if (AudioTrack.PLAYSTATE_PAUSED == mAudioTrack.getPlayState()) {
                        // TODO 已经暂停
                    }
                    if (AudioTrack.PLAYSTATE_PLAYING == mAudioTrack.getPlayState()) {
                        Log.i(TAG, "run: 开始从 " + playIndex + " 播放");
                        playIndex += mAudioTrack.write(data, playIndex, data.length - playIndex);
                        Log.i(TAG, "run: 播放到了 : " + playIndex);
                        if (playIndex == data.length) {
                            Log.i(TAG, "run: 播放完了");
                            mAudioTrack.stop();
                        }

                        if (playIndex < 0) {
                            Log.i(TAG, "run: 播放出错");
                            mAudioTrack.stop();
                            break;
                        }
                    }
                }
                Log.i(TAG, "run: play end");
            }
        }).start();

    }

    /**
     * 设置左右声道是否可用
     *
     * @param left  左声道
     * @param right 右声道
     */
    public void setChannel(boolean left, boolean right) {
        if (null != mAudioTrack) {
            mAudioTrack.setStereoVolume(left ? 1 : 0, right ? 1 : 0);
            mAudioTrack.play();
        }
    }
}
