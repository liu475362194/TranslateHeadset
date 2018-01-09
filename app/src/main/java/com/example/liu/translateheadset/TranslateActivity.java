package com.example.liu.translateheadset;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.example.liu.translateheadset.adapter.TalkAdapter;
import com.example.liu.translateheadset.baidutranslate.TransApi;
import com.example.liu.translateheadset.gson.Error;
import com.example.liu.translateheadset.gson.Speak;
import com.example.liu.translateheadset.gson.TalkAll;
import com.example.liu.translateheadset.gson.Translate;
import com.example.liu.translateheadset.gson.Valume;
import com.example.liu.translateheadset.gson.WakeUp;
import com.example.liu.translateheadset.services.BaiDuSpeekService;
import com.example.liu.translateheadset.services.BaiDuTTSService;
import com.example.liu.translateheadset.services.BaiduWakeUpService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class TranslateActivity extends AppCompatActivity {
    private List<TalkAll> talkAlls = new ArrayList<>();
    private EditText editText;
    private Button startTransZh, startTransEn;
    private Button send;
    private TextView showTrans;
    private static final String APP_ID = "20171114000095150";
    private static final String SECURITY_KEY = "iKpMRhTW0IOBXJXbhNpw";
    private static final String TAG = "TranslateActivity";
    private int mWho;
    private String yuanWen;
    private RecyclerView recyclerView;
    private TalkAdapter adapter;
    private Gson gson;
    private TransApi transApi;
    private Intent intentTts,intentSpeek,intentWakeUp;
    private BaiDuSpeekService.SpeekResultListener speekResultListener = new BaiDuSpeekService.SpeekResultListener() {
        @Override
        public void startSpeek(int who) {
            startTransZh.setClickable(false);
            startTransEn.setClickable(false);
            mWho = who;
        }

        @Override
        public void showResult(String string) {
            try {
                responseSpeak(string);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void successSpeek(String string) {

            startTranslate(transApi);
            startTransZh.setClickable(true);
            startTransZh.setText(R.string.translate_zh);
            startTransEn.setClickable(true);
            startTransEn.setText(R.string.translate_en);
            Speak speak = gson.fromJson(string, Speak.class);
            if (speak.getError() > 0) {
                Error error = gson.fromJson(string, Error.class);
                switch (error.getError()) {
                    case 1:
                        Toast.makeText(TranslateActivity.this, "网络超时", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(TranslateActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(TranslateActivity.this, "音频错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(TranslateActivity.this, "协议错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(TranslateActivity.this, "客户端调用错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(TranslateActivity.this, "超时", Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(TranslateActivity.this, "没有识别结果", Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(TranslateActivity.this, "引擎忙", Toast.LENGTH_SHORT).show();
                        break;
                    case 9:
                        Toast.makeText(TranslateActivity.this, "缺少权限", Toast.LENGTH_SHORT).show();
                        break;
                    case 10:
                        Toast.makeText(TranslateActivity.this, "其它错误", Toast.LENGTH_SHORT).show();
                        break;

                }
            }

        }

        @Override
        public void volumeChange(final String string) {
            final Valume valume = gson.fromJson(string, Valume.class);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mWho == 0) {
                        startTransZh.setText(String.valueOf("声音大小： " + valume.getVolumepercent()));
                    } else {
                        startTransEn.setText(String.valueOf("声音大小： " + valume.getVolumepercent()));
                    }
                }
            });
        }
    };

    private BaiduWakeUpService.WakeUpListener wakeUpListener = new BaiduWakeUpService.WakeUpListener() {
        @Override
        public void SuccessWakeup(String string) {
            WakeUp wakeUp = gson.fromJson(string, WakeUp.class);
            if (wakeUp.getWord().equals("翻译中文")) {
                speekBinder.start("zh");
            } else {
                speekBinder.start("en");
            }
        }
    };

    private BaiDuTTSService.GetTts getTtsBinder;
    private ServiceConnection connectionTts = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            getTtsBinder = (BaiDuTTSService.GetTts) iBinder;
            getTtsBinder.mInitTts(TranslateActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private BaiDuSpeekService.SpeekBinder speekBinder;
    private ServiceConnection connectionSpeek = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            speekBinder = (BaiDuSpeekService.SpeekBinder) iBinder;
            speekBinder.init(TranslateActivity.this, speekResultListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private BaiduWakeUpService.WakeUp wakeUp;
    private ServiceConnection connectionWakeUp = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            wakeUp = (BaiduWakeUpService.WakeUp) iBinder;
            wakeUp.init(TranslateActivity.this, wakeUpListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate);

        initView();
        initPermission();


    }

    /**
     * 启动服务
     */
    private void initService() {
        intentTts = new Intent(TranslateActivity.this, BaiDuTTSService.class);
        startService(intentTts);
        intentSpeek = new Intent(this, BaiDuSpeekService.class);
        startService(intentSpeek);
        intentWakeUp = new Intent(this, BaiduWakeUpService.class);
        startService(intentWakeUp);
    }

    /**
     * 初始化Binder服务
     */
    private void initBinder() {
        Intent intentTts = new Intent(this, BaiDuTTSService.class);
        bindService(intentTts, connectionTts, BIND_AUTO_CREATE);
//        BaiDuSpeekService baiDuSpeekService = new BaiDuSpeekService(this);

        Intent intentSpeek = new Intent(this, BaiDuSpeekService.class);
        bindService(intentSpeek, connectionSpeek, BIND_AUTO_CREATE);
//        speekBinder.init(this);

        Intent intentWakeUp = new Intent(this, BaiduWakeUpService.class);
        bindService(intentWakeUp, connectionWakeUp, BIND_AUTO_CREATE);
    }

    /**
     * 关闭服务
     */
    private void closeService() {

        if (connectionTts != null)
            unbindService(connectionTts);
        if (connectionSpeek != null)
            unbindService(connectionSpeek);
        if (connectionWakeUp != null)
            unbindService(connectionWakeUp);
        if (intentSpeek != null)
            stopService(intentSpeek);
        if (intentTts != null)
            stopService(intentTts);
        if (intentWakeUp != null)
            stopService(intentWakeUp);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        editText = findViewById(R.id.edit_text);
        startTransZh = findViewById(R.id.start_trans_zh);
        startTransEn = findViewById(R.id.start_trans_en);
//        showTrans = findViewById(R.id.show_trans);
        recyclerView = findViewById(R.id.recycler);
        adapter = new TalkAdapter(talkAlls);
        send = findViewById(R.id.send);

        adapter.setmItemClickListener(new TalkAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                getTtsBinder.speak(talkAlls.get(position).getText());
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        transApi = new TransApi(APP_ID, SECURITY_KEY);
        gson = new Gson();
    }


    private void startTranslate(TransApi transApi) {

        final String query = editText.getText().toString();
        yuanWen = query;

        if (query.equals("")) {
            return;
        }
        final String toText = isEnglish(query);
        Log.d(TAG, "startTrans: " + query);
        transApi.getTransResult(query, "auto", toText, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                responseTts(response);
            }
        });
    }

    private void responseTts(Response response) throws IOException {
        String result = response.body().string();
        Translate translate = gson.fromJson(result, Translate.class);
        if (null == translate.getTrans_result()) {
            return;
        }
        final String translateResult = translate.getTrans_result().get(0).getDst();
        TalkAll talkAll = new TalkAll();
        talkAll.setText(translateResult);
        talkAll.setWho(mWho);
        talkAll.setYuanWen(yuanWen);
        talkAlls.add(talkAll);
        talkAlls.size();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//                showTrans.setText(translateResult);
            }
        });
        getTtsBinder.speak(translateResult);
    }

    private void responseSpeak(String string) throws IOException {
        Log.d(TAG, "respinseSpeek: " + string);
        Speak speak = gson.fromJson(string, Speak.class);
        String result;
        if (speak.getResult_type().equals("partial_result")) {
            result = speak.getResults_recognition().get(0);
            editText.setText(result);
        } else if (speak.getResult_type().equals("final_result")) {
            result = speak.getResults_recognition().get(0);
            editText.setText(result);

        }
    }

    /**
     * 判断需要翻译的内容是中文还是英文
     *
     * @param text
     * @return
     */
    private String isEnglish(String text) {
        int n = 0, e = 0, c = 0;
        Log.d(TAG, "isEnglish: " + text.length());
        for (int i = 0; i < text.length(); i++) {
            String str = String.valueOf(text.charAt(i));
            Log.d(TAG, "isEnglish: str" + str);
            Pattern p = Pattern.compile("[0-9]*");
            Matcher m = p.matcher(str);
            if (m.matches()) {
                Log.d(TAG, "isEnglish: 识别为数字");
                n++;
            }
            p = Pattern.compile("[a-zA-Z]");
            m = p.matcher(str);
            if (m.matches()) {
                Log.d(TAG, "isEnglish: 识别为英文");
                e++;
            }
            p = Pattern.compile("[\u4e00-\u9fa5]");
            m = p.matcher(str);
            if (m.matches()) {
                Log.d(TAG, "isEnglish: 识别为中文");
                c++;
            }
        }
        Log.d(TAG, "isEnglish: n = " + n + " e = " + e + " c = " + c);
        if (e > 0 && c == 0) {
            mWho = 1;
            return "zh";
        } else if (c > 0) {
            mWho = 0;
            return "en";
        }
        return "en";
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        } else {
            initSpeek();
            initService();
            initBinder();
        }
    }

    /**
     * 请求权限回调
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initSpeek();
                initService();
                initBinder();
            } else {
                Toast.makeText(TranslateActivity.this, "未获取到权限，请重新打开！", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeService();
    }

    private void initSpeek() {
        startTransZh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speekBinder.start("zh");
            }
        });
        startTransEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speekBinder.start("en");
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTranslate(transApi);
            }
        });
    }
}
