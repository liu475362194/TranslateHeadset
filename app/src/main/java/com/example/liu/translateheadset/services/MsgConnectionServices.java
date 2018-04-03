package com.example.liu.translateheadset.services;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.liu.translateheadset.DemoApplication;
import com.example.liu.translateheadset.activity.ActivityCollector;
import com.example.liu.translateheadset.activity.DialogActivity;
import com.example.liu.translateheadset.activity.LoginActivity;
import com.example.liu.translateheadset.activity.MainActivity;
import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMError;
import com.hyphenate.chat.EMClient;
import com.hyphenate.util.NetUtils;

public class MsgConnectionServices extends Service {
    private static final String TAG = "MsgConnectionServices";
    private Handler handler;

    public MsgConnectionServices() {
        //注册一个监听连接状态的listener
        EMClient.getInstance().addConnectionListener(new MyConnectionListener());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //实现ConnectionListener接口
    private class MyConnectionListener implements EMConnectionListener {
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected: 聊天服务器连接成功");
        }

        @Override
        public void onDisconnected(final int error) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (error == EMError.USER_REMOVED) {
                        // 显示帐号已经被移除
                        Log.d(TAG, "run: 显示帐号已经被移除");
                        Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
                        intent.putExtra("error_str","帐号已经被移除");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } else if (error == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                        // 显示帐号在其他设备登录
                        Log.d(TAG, "run: 显示帐号在其他设备登录");
//                        AlertDialog.Builder dialog = new AlertDialog.Builder(getApplicationContext());
//                        dialog.setTitle("警告");
//                        dialog.setMessage("你的账户在别处登录");
//                        dialog.setPositiveButton("重新登录", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                ActivityCollector.finishAll();
//                                Intent intent = new Intent(getApplication(), LoginActivity.class);
//                                startActivity(intent);
//                            }
//                        });
//                        dialog.setCancelable(false);
//                        dialog.show();


                        DemoApplication.getInstance().logout(false, new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                Intent intent = new Intent(getApplicationContext(), DialogActivity.class);
                                intent.putExtra("error_str","帐号在其他设备登录");
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            @Override
                            public void onError(int i, String s) {
                                showToash("连接不到聊天服务器");
                            }

                            @Override
                            public void onProgress(int i, String s) {


                            }
                        });


                    } else {
                        if (NetUtils.hasNetwork(MsgConnectionServices.this)) {
                            //连接不到聊天服务器
                            Log.d(TAG, "Network success");
//                            Toast.makeText(getApplicationContext(),"连接不到聊天服务器",Toast.LENGTH_SHORT).show();
//                            showToash("连接不到聊天服务器");
                        } else {
                            //当前网络不可用，请检查网络设置
                            Log.d(TAG, "run: 当前网络不可用，请检查网络设置");
//                            Toast.makeText(getApplicationContext(),"当前网络不可用，请检查网络设置",Toast.LENGTH_SHORT).show();
                            showToash("当前网络不可用，请检查网络设置");
                        }

                    }
                }
            }).start();
        }
    }

    /**
     * 由于在service中的Toash需要在主线程中运行，所以要通过Handler将一个自定义的线程运行于主线程之上。
     *
     * @param string
     */
    private void showToash(final String string) {
        handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
