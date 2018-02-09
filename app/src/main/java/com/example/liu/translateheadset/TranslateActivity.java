package com.example.liu.translateheadset;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Environment;
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


import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;
import com.example.liu.translateheadset.adapter.TalkAdapter;
import com.example.liu.translateheadset.translate.BaiduApi;
import com.example.liu.translateheadset.gson.Error;
import com.example.liu.translateheadset.gson.Speak;
import com.example.liu.translateheadset.gson.TalkAll;
import com.example.liu.translateheadset.gson.Translate;
import com.example.liu.translateheadset.gson.Valume;
import com.example.liu.translateheadset.gson.WakeUp;
import com.example.liu.translateheadset.services.BaiDuSpeekService;
import com.example.liu.translateheadset.services.BaiDuTTSService;
import com.example.liu.translateheadset.services.BaiduWakeUpService;
import com.example.liu.translateheadset.translate.GoogleApi;
import com.example.liu.translateheadset.util.TimeStart2Stop;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    private Button startTransZh, startTransEn, select;
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
    private BaiduApi baiduApi;
    private Intent intentTts, intentSpeek, intentWakeUp;
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

            startTranslate();
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
        long last = TimeStart2Stop.timeNeed(this, "onCreate", -1);
        initView();
        initPermission();
        Aria.download(this).register();
        TimeStart2Stop.timeNeed(this, "onCreate", last);
//        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    /**
     * 启动服务
     */
    private void initService() {
        long last = TimeStart2Stop.timeNeed(this, "initService", -1);
        intentTts = new Intent(TranslateActivity.this, BaiDuTTSService.class);
        startService(intentTts);
        intentSpeek = new Intent(this, BaiDuSpeekService.class);
        startService(intentSpeek);
        intentWakeUp = new Intent(this, BaiduWakeUpService.class);
        startService(intentWakeUp);
        TimeStart2Stop.timeNeed(this, "initService", last);
    }

    /**
     * 初始化Binder服务
     */
    private void initBinder() {
        long last = TimeStart2Stop.timeNeed(this, "initBinder", -1);

//        Intent intentTts = new Intent(this, BaiDuTTSService.class);
        bindService(intentTts, connectionTts, BIND_AUTO_CREATE);
//        BaiDuSpeekService baiDuSpeekService = new BaiDuSpeekService(this);

//        Intent intentSpeek = new Intent(this, BaiDuSpeekService.class);
        bindService(intentSpeek, connectionSpeek, BIND_AUTO_CREATE);
//        speekBinder.init(this);

//        Intent intentWakeUp = new Intent(this, BaiduWakeUpService.class);
        bindService(intentWakeUp, connectionWakeUp, BIND_AUTO_CREATE);

        TimeStart2Stop.timeNeed(this, "initBinder", last);
    }

    /**
     * 关闭服务
     */
    private void closeService() {
        long last = TimeStart2Stop.timeNeed(this, "closeService", -1);

        if (connectionTts != null) {
            unbindService(connectionTts);
            connectionTts = null;
        }
        if (connectionSpeek != null) {
            unbindService(connectionSpeek);
            connectionSpeek = null;
        }
        if (connectionWakeUp != null) {
            unbindService(connectionWakeUp);
            connectionWakeUp = null;
        }
        if (intentSpeek != null)
            stopService(intentSpeek);
        if (intentTts != null)
            stopService(intentTts);
        if (intentWakeUp != null)
            stopService(intentWakeUp);
        TimeStart2Stop.timeNeed(this, "closeService", last);
    }

    /**
     * 初始化界面
     */
    private void initView() {
        long last = TimeStart2Stop.timeNeed(this, "initView", -1);
        editText = findViewById(R.id.edit_text);
        startTransZh = findViewById(R.id.start_trans_zh);
        startTransEn = findViewById(R.id.start_trans_en);
//        showTrans = findViewById(R.id.show_trans);
        recyclerView = findViewById(R.id.recycler);
        adapter = new TalkAdapter(talkAlls);
        send = findViewById(R.id.send);
        select = findViewById(R.id.select);
        initClickListener();
        adapter.setmItemClickListener(new TalkAdapter.ItemClickListener() {
            @Override
            public void onClick(int position) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                getTtsBinder.speak(talkAlls.get(position).getTranslateText());
            }
        });
        LinearLayoutManager manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);
        baiduApi = new BaiduApi(APP_ID, SECURITY_KEY);
        gson = new Gson();
        TimeStart2Stop.timeNeed(this, "initView", last);
    }

    /**
     * 开始进行语音翻译
     */
    private void startTranslate() {

        final String query = editText.getText().toString();
        yuanWen = query;

        if (query.equals("")) {
            return;
        }
        final String toText = isEnglish(query);
        Log.d(TAG, "startTrans: " + query);

        if (select.getText().equals("百度")){
            baiduApi.getTransResult(query, "auto", toText, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    responseTts(response);
                }
            });
        } else {
            if (toText.equals("en")){
                GoogleApi.getInstance().setContext(TranslateActivity.this).getTransResult(query, "zh-CN", "en");
            }else{
                GoogleApi.getInstance().setContext(TranslateActivity.this).getTransResult(query, "en", "zh-CN");
            }
        }

    }

//    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            checkDownloadStatus();
//        }
//    };

    //检查下载状态
//    private void checkDownloadStatus() {
//        DownloadManager.Query query = new DownloadManager.Query();
//        query.setFilterById(GoogleApi.getInstance().mTaskId);//筛选下载任务，传入任务ID，可变参数
//        Cursor c = GoogleApi.getInstance().downloadManager.query(query);
//        if (c.moveToFirst()) {
//            int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
//            switch (status) {
//                case DownloadManager.STATUS_PAUSED:
//                    Log.d(TAG, ">>>下载暂停");
////                    MLog.i(">>>下载暂停");
//                case DownloadManager.STATUS_PENDING:
//                    Log.d(TAG, ">>>下载延迟");
////                    MLog.i(">>>下载延迟");
//                case DownloadManager.STATUS_RUNNING:
//                    Log.d(TAG, ">>>正在下载");
////                    MLog.i(">>>正在下载");
//                    break;
//                case DownloadManager.STATUS_SUCCESSFUL:
//
////                    创建文件对象，指向需要读取的文件
//                    StringBuffer sb = new StringBuffer();
//                    File file = new File(Environment.getExternalStorageDirectory() + "/GoogleTranslate/GoogleTranslate.txt");
//                    String line = "";
//                    try {
//                        InputStream instream = new FileInputStream(file);
////                        创建文件Reader对象，读取指定的文件
//                        BufferedReader br = new BufferedReader(new FileReader(file));
////                        创建一个line接受读取的文件内容，因为是文本文件，所以一行一行读
//                        while ((line = br.readLine()) != null) {
//                            sb.append(line);
//                        }
////                        关闭文件读取对象
//                        br.close();
//                        Log.d(TAG, ">>>下载完成" + sb.toString());
//                        Log.d(TAG, ">>>下载完成: " + splitGoogleTranslate(sb.toString()));
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//
////                    MLog.i(">>>下载完成");
//                    break;
//                case DownloadManager.STATUS_FAILED:
//                    Log.d(TAG, ">>>下载失败");
////                    MLog.i(">>>下载失败");
//                    break;
//            }
//        }
//    }

    private String splitGoogleTranslate(String result) {
        String[] temp;
        temp = result.split("\"");
        return temp[1];
    }

    /**
     * 处理翻译请求返回的数据，解析出结果，并加入聊天消息列表。
     *
     * @param response
     * @throws IOException
     */
    private void responseTts(Response response) throws IOException {
        String result = response.body().string();
        Translate translate = gson.fromJson(result, Translate.class);
        if (null == translate.getTrans_result()) {
            return;
        }
        final String translateResult = translate.getTrans_result().get(0).getDst();
        addTalkAllList(translateResult);
    }

    private void addTalkAllList(String translateResult) {
        TalkAll talkAll = new TalkAll();
        talkAll.setTranslateText(translateResult);
        talkAll.setWho(mWho);
        talkAll.setYuanWen(yuanWen);
        talkAlls.add(talkAll);
        talkAlls.size();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
//                showTrans.setTranslateText(translateResult);
            }
        });
        getTtsBinder.speak(translateResult);
    }

    /**
     * 处理语音识别请求返回的数据。
     *
     * @param string
     * @throws IOException
     */
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
        long last = TimeStart2Stop.timeNeed(this, "initPermission", -1);
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
            initService();
            initBinder();
        }
        TimeStart2Stop.timeNeed(this, "initPermission", last);
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
            long last = TimeStart2Stop.timeNeed(TranslateActivity.this, "onRequestPermissionsResult", -1);
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initService();
                initBinder();
            } else {
                Toast.makeText(TranslateActivity.this, "未获取到权限，请重新打开！", Toast.LENGTH_SHORT).show();
                finish();
            }
            TimeStart2Stop.timeNeed(TranslateActivity.this, "onRequestPermissionsResult", last);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeService();
    }

    private void initClickListener() {
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
                startTranslate();
                editText.setText("");
            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (select.getText().equals("百度")){
                    select.setText("谷歌");
                } else {
                    select.setText("百度");
                }
            }
        });
    }

    @Download.onTaskComplete
    void taskComplete(DownloadTask task) {
//        Toast.makeText(this, "下载完成", Toast.LENGTH_SHORT).show();

        String content = getGoogleContent();
        String [] splitContent = content.split("\"");
//        for (String str : splitContent){
//            Log.d(TAG, "taskComplete: 分割文本： " + str);
//        }
        yuanWen = splitContent[3];
        addTalkAllList(splitContent[1]);
    }

    private String getGoogleContent() {
        String content = "";
        try {
            InputStream instream = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/GoogleTranslate/f.txt"));
            if (instream != null) {
                InputStreamReader inputreader
                        =new InputStreamReader(instream, "UTF-8");
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line="";
                //分行读取
                while (( line = buffreader.readLine()) != null) {
                    content += line + "\n";
                }
                instream.close();       //关闭输入流
            }
        }
        catch (java.io.FileNotFoundException e) {
            Log.d("TestFile", "The File doesn't not exist.");
        }
        catch (IOException e)  {
            Log.d("TestFile", e.getMessage());
        }

        Log.d(TAG, "taskComplete:获取内容为 " + content);
        return content;
    }

    @Download.onTaskFail
    void taskFail(DownloadTask task) {
        Toast.makeText(this, "翻译失败", Toast.LENGTH_SHORT).show();
    }

}
