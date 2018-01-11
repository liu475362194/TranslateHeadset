package com.example.liu.translateheadset.activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.liu.translateheadset.Constant;
import com.example.liu.translateheadset.R;
import com.example.liu.translateheadset.baidutranslate.TranslateHelper;
import com.example.liu.translateheadset.gson.Error;
import com.example.liu.translateheadset.gson.Message;
import com.example.liu.translateheadset.gson.Speak;
import com.example.liu.translateheadset.gson.Valume;
import com.example.liu.translateheadset.services.BLEService;
import com.example.liu.translateheadset.services.BaiDuSpeekService;
import com.example.liu.translateheadset.services.BaiDuTTSService;
import com.example.liu.translateheadset.util.EaseCommonUtils;
import com.example.liu.translateheadset.util.TimeStart2Stop;
import com.google.gson.Gson;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMTextMessageBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ChatActivity extends BaseActivity {
    private ListView listView;
    private int chatType = 1;
    private String toChatUsername;
    private Button btn_send;
    private Button btn_speak;
    private EditText et_content;
    private List<EMMessage> msgList;
    private List<Message> messagesWithTranslate;
    MessageAdapter adapter;
    private EMConversation conversation;
    protected int pagesize = 20;
    private static final String TAG = "ChatActivity";
    private Intent intentTts, intentSpeek, intentWakeUp;
    private BaiDuTTSService.GetTts getTtsBinder;
    private Gson gson = new Gson();
    private AudioManager mAudioManager;
    private LocalBroadcastManager localBroadcastManager;
    private NotifyReceiver notifyReceiver;
    private ServiceConnection connectionTts = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            getTtsBinder = (BaiDuTTSService.GetTts) iBinder;
            getTtsBinder.mInitTts(ChatActivity.this);
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
            speekBinder.init(ChatActivity.this, speekResultListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private UUID uuid1 = UUID.fromString("65786365-6c70-6f69-6e74-2e636f810000");
    private UUID uuid2 = UUID.fromString("65786365-6c70-6f69-6e74-2e636f810001");
    private BluetoothGattCharacteristic gattCharacteristics;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "onCreate", -1);
        setContentView(R.layout.activity_chat);

        toChatUsername = this.getIntent().getStringExtra("username");
        TextView tv_toUsername = this.findViewById(R.id.title_bar_title);
        tv_toUsername.setText(toChatUsername);
        listView = (ListView) this.findViewById(R.id.listView);
        btn_send = (Button) this.findViewById(R.id.btn_send);
        btn_speak = this.findViewById(R.id.btn_speak);
        et_content = (EditText) this.findViewById(R.id.et_content);
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        getAllMessage();
        initList();
        btn_send.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String content = et_content.getText().toString().trim();
                if (TextUtils.isEmpty(content)) {

                    return;
                }
                setMesaage(content);
//
            }


        });

        btn_speak.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
//                speekBinder.start("zh");
                if (!BLEService.isBleSuccess()) {
                    speekBinder.start("zh");
                } else {
                    startRecording();
                }
            }
        });
        EMClient.getInstance().chatManager().addMessageListener(msgListener);
        initService();
        initBinder();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.broadcasttest.NOTIFY");
        notifyReceiver = new NotifyReceiver();
        localBroadcastManager.registerReceiver(notifyReceiver, intentFilter);
        TimeStart2Stop.timeNeed(ChatActivity.this, "onCreate", last);
    }

    /**
     * 收到指令广播的广播接收器
     */
    class NotifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive: isBleSuccess" + BLEService.isBleSuccess());
            startRecording();

        }
    }

    /**
     * 初始化list
     *
     */
    private void initList() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "initList", -1);
        msgList = conversation.getAllMessages();
        messagesWithTranslate = new ArrayList<>();

        for (EMMessage message : msgList) {
            addMes(message);
        }
        translateMes(false);
        adapter = new MessageAdapter(messagesWithTranslate, ChatActivity.this);
        listView.setAdapter(adapter);
        listView.setSelection(listView.getCount() - 1);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(ChatActivity.this, "已点击item", Toast.LENGTH_SHORT).show();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(et_content.getWindowToken(), 0);
            }
        });
        TimeStart2Stop.timeNeed(ChatActivity.this, "initList", last);
    }

    /**
     * 翻译所有未翻译的消息
     * @param isNow
     */
    private void translateMes(final boolean isNow) {
        final TranslateHelper helper = new TranslateHelper();
        for (int i = 0; i < messagesWithTranslate.size(); i++) {
            final Message message = messagesWithTranslate.get(i);
            final int finalI = i;
            if (null != messagesWithTranslate.get(i).getTranslate()) {
                continue;
            }
            helper.startTranslate(message.getMessage(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String text = helper.responseTrans(response);
                    message.setTranslate(text);
                    messagesWithTranslate.set(finalI, message);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                    Log.d(TAG, "onResponse: " + messagesWithTranslate.get(finalI).getType());
                    if (isNow) {
                        getTtsBinder.speak(text);
                        Log.d(TAG, "onResponse: start speak " + helper.responseTrans(response));
                    }
                    Log.d(TAG, "onResponse: " + helper.responseTrans(response));
                }
            });
        }
    }

    /**
     * 往消息list中添加一条新信息
     * @param message
     */
    private void addMes(EMMessage message) {
        Message mMessage = new Message();
        String text = ((EMTextMessageBody) message.getBody()).getMessage();
        mMessage.setMessage(text);
        if (message.direct() == EMMessage.Direct.RECEIVE) {
            mMessage.setType(0);
        } else {
            mMessage.setType(1);
        }
        messagesWithTranslate.add(mMessage);
    }

    /**
     * 启动服务
     */
    private void initService() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "initService", -1);
        intentTts = new Intent(this, BaiDuTTSService.class);
        startService(intentTts);
        intentSpeek = new Intent(this, BaiDuSpeekService.class);
        startService(intentSpeek);
//        intentWakeUp = new Intent(this, BaiduWakeUpService.class);
//        startService(intentWakeUp);
        TimeStart2Stop.timeNeed(ChatActivity.this, "initService", last);
    }

    /**
     * 初始化Binder服务
     */
    private void initBinder() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "initBinder", -1);
        Intent intentTts = new Intent(this, BaiDuTTSService.class);
        bindService(intentTts, connectionTts, BIND_AUTO_CREATE);
//        BaiDuSpeekService baiDuSpeekService = new BaiDuSpeekService(this);

        Intent intentSpeek = new Intent(this, BaiDuSpeekService.class);
        bindService(intentSpeek, connectionSpeek, BIND_AUTO_CREATE);
//        speekBinder.init(this);
//
//        Intent intentWakeUp = new Intent(this, BaiduWakeUpService.class);
//        bindService(intentWakeUp, connectionWakeUp, BIND_AUTO_CREATE);
        TimeStart2Stop.timeNeed(ChatActivity.this, "initBinder", last);
    }

    /**
     * 获取全部聊天记录
     */
    protected void getAllMessage() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "getAllMessage", -1);
        // 获取当前conversation对象

        conversation = EMClient.getInstance().chatManager().getConversation(toChatUsername,
                EaseCommonUtils.getConversationType(chatType), true);
        // 把此会话的未读数置为0
        conversation.markAllMessagesAsRead();
        // 初始化db时，每个conversation加载数目是getChatOptions().getNumberOfMessagesLoaded
        // 这个数目如果比用户期望进入会话界面时显示的个数不一样，就多加载一些
        final List<EMMessage> msgs = conversation.getAllMessages();
        int msgCount = msgs != null ? msgs.size() : 0;
        if (msgCount < conversation.getAllMsgCount() && msgCount < pagesize) {
            String msgId = null;
            if (msgs != null && msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            conversation.loadMoreMsgFromDB(msgId, pagesize - msgCount);
        }
        TimeStart2Stop.timeNeed(ChatActivity.this, "getAllMessage", last);

    }

    /**
     * 发送一条信息
     * @param content 聊天信息内容
     */
    private void setMesaage(String content) {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "setMessage", -1);

        // 创建一条文本消息，content为消息文字内容，toChatUsername为对方用户或者群聊的id，后文皆是如此
        EMMessage message = EMMessage.createTxtSendMessage(content, toChatUsername);
        // 如果是群聊，设置chattype，默认是单聊
        if (chatType == Constant.CHATTYPE_GROUP)
            message.setChatType(ChatType.GroupChat);
        // 发送消息
        EMClient.getInstance().chatManager().sendMessage(message);
        msgList.add(message);
        addMes(message);
        translateMes(false);
//        adapter.notifyDataSetChanged();
        if (msgList.size() > 0) {
            listView.setSelection(listView.getCount() - 1);
        }
        et_content.setText("");
        et_content.clearFocus();
        TimeStart2Stop.timeNeed(ChatActivity.this, "setMessage", last);
    }

    /**
     * 信息收发监听
     */
    EMMessageListener msgListener = new EMMessageListener() {

        @Override
        public void onMessageReceived(List<EMMessage> messages) {
            long last = TimeStart2Stop.timeNeed(ChatActivity.this, "onMessageReceived", -1);

            for (EMMessage message : messages) {
                String username = null;
                // 群组消息
                if (message.getChatType() == ChatType.GroupChat || message.getChatType() == ChatType.ChatRoom) {
                    username = message.getTo();
                } else {
                    // 单聊消息
                    username = message.getFrom();
                }
                // 如果是当前会话的消息，刷新聊天页面
                if (username.equals(toChatUsername)) {
                    msgList.addAll(messages);
                    addMes(message);
                    translateMes(true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                    if (msgList.size() > 0) {
                        et_content.setSelection(listView.getCount() - 1);
                    }

                }
            }
            conversation.markAllMessagesAsRead();

            TimeStart2Stop.timeNeed(ChatActivity.this, "onMessageReceived", last);
            // 收到消息
        }

        @Override
        public void onCmdMessageReceived(List<EMMessage> messages) {
            // 收到透传消息
        }

        @Override
        public void onMessageReadAckReceived(List<EMMessage> messages) {
            // 收到已读回执
        }

        @Override
        public void onMessageDeliveryAckReceived(List<EMMessage> message) {
            // 收到已送达回执
        }

        @Override
        public void onMessageChanged(EMMessage message, Object change) {
            // 消息状态变动
        }
    };


    @Override
    protected void onDestroy() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "onDestroy", -1);
        super.onDestroy();
        EMClient.getInstance().chatManager().removeMessageListener(msgListener);
        closeService();
        if (notifyReceiver != null)
            localBroadcastManager.unregisterReceiver(notifyReceiver);
        TimeStart2Stop.timeNeed(ChatActivity.this, "onDestroy", last);
    }

    /**
     * 关闭服务
     */
    private void closeService() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "closeService", -1);
        if (connectionTts != null)
            unbindService(connectionTts);
        if (connectionSpeek != null)
            unbindService(connectionSpeek);
//        if (connectionWakeUp != null)
//            unbindService(connectionWakeUp);
        if (intentSpeek != null)
            stopService(intentSpeek);
        if (intentTts != null)
            stopService(intentTts);
//        if (intentWakeUp != null)
//            stopService(intentWakeUp);
        TimeStart2Stop.timeNeed(ChatActivity.this, "closeService", last);
    }

    //    public void startSCOListening() {
//        if(recorder == null){
//            recorder = new RawAudioRecorder(AUDIO_RATE);
//        }
//        recorder.start();
//        alexaManager.sendAudioRequest(requestBody, getRequestCallback());
//    }

    /**
     * 关闭SCO端口
     */
    private void stopSCO() {
        if (mAudioManager.isBluetoothScoOn()) {
//            mAudioManager.setMode(AudioManager.MODE_NORMAL);
            mAudioManager.setBluetoothScoOn(false);
            mAudioManager.stopBluetoothSco();
        }

        Log.e(TAG, "stopSCO: startListening: recorder.stop()");
    }

    /**
     * 打开SCO端口，使用耳机端录音
     */
    public void startRecording() {
        long last = TimeStart2Stop.timeNeed(ChatActivity.this, "startRecording", -1);
        mAudioManager.setMode(AudioManager.MODE_IN_CALL);
        //蓝牙录音的关键，启动SCO连接，耳机话筒才起作用
        Log.e(TAG, "startRecording 启动SCO连接");
//        isStartVoice = true;
        mAudioManager.startBluetoothSco();
        //蓝牙SCO连接建立需要时间，连接建立后会发出ACTION_SCO_AUDIO_STATE_CHANGED消息，通过接收该消息而进入后续逻辑。
        //也有可能此时SCO已经建立，则不会收到上述消息，可以startBluetoothSco()前先stopBluetoothSco()
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, -1);
                Log.e(TAG, "state = " + state);
                if (AudioManager.SCO_AUDIO_STATE_CONNECTED == state) {
                    mAudioManager.setBluetoothScoOn(true);  //打开SCO
                    mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, 0);
                    Log.e(TAG, "isBluetoothScoOn = " + mAudioManager.isBluetoothScoOn());
                    if (mAudioManager.isBluetoothScoOn()) {

                        //  recorder.start();//开始录音
//                        startSCOListening();

                        speekBinder.start("zh");
                    }

                    unregisterReceiver(this);  //别遗漏
                } else {//等待一秒后再尝试启动SCO
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            mAudioManager.startBluetoothSco();
                        }
                    }).start();

                }
            }
        }, new IntentFilter(AudioManager.ACTION_SCO_AUDIO_STATE_CHANGED));
        TimeStart2Stop.timeNeed(ChatActivity.this, "startRecording", last);
    }


    @SuppressLint("InflateParams")
    class MessageAdapter extends BaseAdapter {
        private List<Message> msgs;
        private Context context;
        private LayoutInflater inflater;


        public MessageAdapter(List<Message> msgs, Context context_) {
            this.msgs = msgs;
            this.context = context_;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return msgs.size();
        }

        @Override
        public Message getItem(int position) {
            return msgs.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
            return message.getType();
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            EMMessage message = getItem(position);
            int viewType = getItemViewType(position);
            if (convertView == null) {
                if (viewType == 0) {
                    convertView = inflater.inflate(R.layout.item_message_received, parent, false);
                } else {
                    convertView = inflater.inflate(R.layout.item_message_sent, parent, false);
                }
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder();
                holder.tv = (TextView) convertView.findViewById(R.id.tv_chatcontent);
                holder.tvTranslate = convertView.findViewById(R.id.tv_chatcontent_translate);
                holder.layout = convertView.findViewById(R.id.layout);
                convertView.setTag(holder);
            }

//            EMTextMessageBody txtBody = (EMTextMessageBody) message.getBody();
            holder.tv.setText(messagesWithTranslate.get(position).getMessage());
            holder.tvTranslate.setText(messagesWithTranslate.get(position).getTranslate());

            return convertView;
        }
    }

    public static class ViewHolder {

        TextView tv;
        TextView tvTranslate;
        LinearLayout layout;

    }

    /**
     * 百度语音识别处理监听
     */
    private BaiDuSpeekService.SpeekResultListener speekResultListener = new BaiDuSpeekService.SpeekResultListener() {
        @Override
        public void startSpeek(int who) {

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

            Speak speak = gson.fromJson(string, Speak.class);
            if (speak.getError() > 0) {
                stopSCO();
                Error error = gson.fromJson(string, Error.class);
                switch (error.getError()) {
                    case 1:
                        Toast.makeText(ChatActivity.this, "网络超时", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(ChatActivity.this, "网络连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(ChatActivity.this, "音频错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(ChatActivity.this, "协议错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(ChatActivity.this, "客户端调用错误", Toast.LENGTH_SHORT).show();
                        break;
                    case 6:
                        Toast.makeText(ChatActivity.this, "超时", Toast.LENGTH_SHORT).show();
                        break;
                    case 7:
                        Toast.makeText(ChatActivity.this, "没有识别结果", Toast.LENGTH_SHORT).show();
                        break;
                    case 8:
                        Toast.makeText(ChatActivity.this, "引擎忙", Toast.LENGTH_SHORT).show();
                        break;
                    case 9:
                        Toast.makeText(ChatActivity.this, "缺少权限", Toast.LENGTH_SHORT).show();
                        break;
                    case 10:
                        Toast.makeText(ChatActivity.this, "其它错误", Toast.LENGTH_SHORT).show();
                        break;
                }
                btn_speak.setText(String.valueOf("识别"));
            }

        }

        @Override
        public void volumeChange(final String string) {
            final Valume valume = gson.fromJson(string, Valume.class);
            Log.d(TAG, "volumeChange: " + valume.getVolumepercent());
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn_speak.setText(String.valueOf(valume.getVolumepercent()));
                }
            });
        }
    };

    /**
     * 语音识别结果处理
     * @param string
     * @throws IOException
     */
    private void responseSpeak(String string) throws IOException {
        Log.d(TAG, "respinseSpeek: " + string);
        Speak speak = gson.fromJson(string, Speak.class);
        String result;
        if (speak.getResult_type().equals("partial_result")) {
            result = speak.getResults_recognition().get(0);
            et_content.setText(result);
        } else if (speak.getResult_type().equals("final_result")) {
            stopSCO();
            result = speak.getResults_recognition().get(0);
//            btn_send.setText("发送");
            et_content.setText(result);
            setMesaage(result);
            btn_speak.setText(String.valueOf("识别"));
        }
    }
}
