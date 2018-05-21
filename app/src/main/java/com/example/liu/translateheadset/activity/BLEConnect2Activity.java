package com.example.liu.translateheadset.activity;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.utils.HexUtil;
import com.example.liu.translateheadset.DemoApplication;
import com.example.liu.translateheadset.R;
import com.example.liu.translateheadset.adapter.DeviceAdapter;
import com.example.liu.translateheadset.services.MeizuBleManager;
import com.example.liu.translateheadset.util.TimeStart2Stop;
import com.kaopiz.kprogresshud.KProgressHUD;

import java.util.ArrayList;
import java.util.List;

public class BLEConnect2Activity extends BaseActivity {

    private static final String TAG = "BLEConnect2Activity";

    private KProgressHUD kProgressHUD;

    private ListView listView;
    //设备扫描结果展示适配器
    private DeviceAdapter adapter;
    //设备扫描结果集合
//    private volatile List<BluetoothLeDevice> bluetoothLeDeviceList = new ArrayList<>();
    private volatile List<BleDevice> bluetoothLeDeviceList = new ArrayList<>();

//    private ServiceConnection connection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
//            bleBinder = (BLEService.BLEBinder) iBinder;
//            checkBluetoothPermission();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//
//        }
//    };

    private IntentFilter intentFilter;
    private LocalBroadcastManager localBroadcastManager;
    private ScanReceiver scanReceiver;
    private ConnectingReceiver connectingReceiver;
    private ConnectingGattReceiver connectingGattReceiver;
    private ConnectedReceiver connectedReceiver;
    private ConnectFailReceiver connectFailReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bleconnect2);

        Log.d(TAG, "onCreate: " + isServiceWork(this, "com.example.liu.translateheadset.services.BLEService"));
        if (!isServiceWork(this, "com.example.liu.translateheadset.services.BLEService")) {
//            bleBinder = DemoApplication.getBleBinder();
            checkBluetoothPermission();
//            bindService();
        }


        localBroadcastManager = LocalBroadcastManager.getInstance(this);

//        getSupportActionBar().setTitle("蓝牙连接");

        listView = findViewById(R.id.list);
        adapter = new DeviceAdapter(this);
        listView.setAdapter(adapter);

//        Log.e(TAG, "onCreate: " + bleBinder);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                BluetoothLeDevice device = (BluetoothLeDevice) adapter.getItem(i);
//                bleBinder.connectBLE(device.getAddress());
                BleDevice device = (BleDevice) adapter.getItem(i);
                MeizuBleManager.getInstance(BLEConnect2Activity.this).connect(device);
                kProgressHUD = new KProgressHUD(BLEConnect2Activity.this);
                kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                        .setDetailsLabel("正在连接蓝牙设备...")
                        .setAnimationSpeed(2)
                        .show();
            }
        });

        connectedReceiver = new ConnectedReceiver();
        connectingReceiver = new ConnectingReceiver();
        connectingGattReceiver = new ConnectingGattReceiver();
        connectFailReceiver = new ConnectFailReceiver();
        scanReceiver = new ScanReceiver();
//        registerReceiver("com.example.broadcasttest.CONNECTED", connectedReceiver);
//        registerReceiver("com.example.broadcasttest.CONNECTING", connectingReceiver);
//        registerReceiver("com.example.broadcasttest.CONNECTING_GATT", connectingGattReceiver);
//        registerReceiver("com.example.broadcasttest.DEVICE_FOUND", scanReceiver);
//        registerReceiver("com.example.broadcasttest.CONNECT_FAILURE", connectFailReceiver);

        if (Build.VERSION.SDK_INT <= 23) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    TeachView.getInstance(BLEConnect2Activity.this).setImageId(R.drawable.ble_window).initStudyWindow();
                }
            });
        }


        MeizuBleManager.getInstance(this).scan(new MeizuBleManager.OnScanListener() {
            @Override
            public void scan(BleDevice result) {
//                bluetoothLeDeviceList = bleBinder.getBluetoothLeDeviceList();
                bluetoothLeDeviceList.add(result);
                adapter.setDeviceList(bluetoothLeDeviceList);
            }

            @Override
            public void connectionSuccess(BleDevice result) {
                DemoApplication.getInstance().setBleDevice(result);
                kProgressHUD.dismiss();
                finish();
            }

            @Override
            public void disconnection() {

            }
        });

    }



    /**
     * 判断某个服务是否正在运行的方法
     *
     * @param mContext
     * @param serviceName 是包名+服务的类名（例如：net.loonggg.testbackstage.TestService）
     * @return true代表正在运行，false代表服务没有正在运行
     */
    public boolean isServiceWork(Context mContext, String serviceName) {
        long last = TimeStart2Stop.timeNeed(BLEConnect2Activity.this, "isServiceWork", -1);
        boolean isWork = false;
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> myList = myAM.getRunningServices(40);
        if (myList.size() <= 0) {
            TimeStart2Stop.timeNeed(BLEConnect2Activity.this, "isServiceWork", last);
            return false;
        }
        for (int i = 0; i < myList.size(); i++) {
            String mName = myList.get(i).service.getClassName().toString();
            if (mName.equals(serviceName)) {
                isWork = true;
                break;
            }
        }
        TimeStart2Stop.timeNeed(BLEConnect2Activity.this, "isServiceWork", last);
        return isWork;

    }

    private void registerReceiver(String action, BroadcastReceiver receiver) {
        intentFilter = new IntentFilter();
        intentFilter.addAction(action);
        localBroadcastManager.registerReceiver(receiver, intentFilter);
    }


    class ScanReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            bluetoothLeDeviceList = bleBinder.getBluetoothLeDeviceList();
//            adapter.setDeviceList(bluetoothLeDeviceList);
        }
    }

    class ConnectingReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            kProgressHUD = new KProgressHUD(BLEConnect2Activity.this);
            kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                    .setDetailsLabel("正在连接蓝牙设备...")
                    .setAnimationSpeed(2)
                    .show();
        }
    }

    class ConnectingGattReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if (kProgressHUD.isShowing()) {
//                kProgressHUD.dismiss();
//            }
//            kProgressHUD = new KProgressHUD(BLEConnect2Activity.this);
//            kProgressHUD.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
//                    .setDetailsLabel("正在连接通讯服务...")
//                    .setAnimationSpeed(2)
//                    .show();
        }
    }

    class ConnectedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            kProgressHUD.dismiss();
            finish();
//            getSupportActionBar().setIcon(R.drawable.home_disconnected_pressed);

        }
    }

    class ConnectFailReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            kProgressHUD.dismiss();

            Toast.makeText(BLEConnect2Activity.this,"连接失败！请重试",Toast.LENGTH_SHORT).show();

//            bleBinder.startScan();
//            ImageView imageView = new ImageView(BLEConnect2Activity.this);
//            imageView.setBackgroundResource(R.drawable.ease_msg_state_fail_resend);
//            KProgressHUD.create(BLEConnect2Activity.this)
//                    .setCustomView(imageView)
//                    .setLabel("连接失败！请重试")
//                    .show();
////            checkBluetoothPermission();
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d(TAG, "run: dismiss");
//                    kProgressHUD.dismiss();
//                }
//            }, 1000);
        }
    }

    /**
     * 检查蓝牙权限
     */
    private void checkBluetoothPermission() {
        long last = TimeStart2Stop.timeNeed(BLEConnect2Activity.this, "checkBluetoothPermission", -1);
//        if (bluetoothLeDeviceStore != null) {
//            bluetoothLeDeviceStore.clear();
//        }
//        if (adapter != null && bluetoothLeDeviceList != null) {
//            bluetoothLeDeviceList.clear();
//            adapter.setDeviceList(bluetoothLeDeviceList);
//        }

        if (BleManager.getInstance().isBlueEnable()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //校验是否已具有模糊定位权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                            100);
                } else {
                    //具有权限
//                    bleBinder.startScan();
                }
            } else {
//                Log.e(TAG, "checkBluetoothPermission: " + bleBinder);
                //系统不高于6.0直接执行
//                bleBinder.startScan();
            }
        } else {
            BleManager.getInstance().enableBluetooth();
//            ViseBle.getInstance().getBluetoothAdapter().enable();
//            bleBinder.startScan();
        }

        TimeStart2Stop.timeNeed(BLEConnect2Activity.this, "checkBluetoothPermission", last);
    }

    /**
     * 打开或关闭蓝牙后的回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                checkBluetoothPermission();
            }
        } else if (resultCode == RESULT_CANCELED) {
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 对返回的值进行处理，相当于StartActivityForResult
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }


    /**
     * 权限申请的下一步处理
     *
     * @param requestCode  申请码
     * @param grantResults 申请结果
     */
    private void doNext(int requestCode, int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //同意权限
                checkBluetoothPermission();
            } else {
                // 权限拒绝，提示用户开启权限
                denyPermission();
            }
        }
    }

    /**
     * 权限申请被拒绝的处理方式
     */
    private void denyPermission() {
        finish();
    }

    @Override
    protected void onDestroy() {
        long last = TimeStart2Stop.timeNeed(BLEConnect2Activity.this, TAG + " onDestroy", -1);
        super.onDestroy();
//        if (connection != null)
//        unbindService(connection);
        localBroadcastManager.unregisterReceiver(scanReceiver);
        localBroadcastManager.unregisterReceiver(connectedReceiver);
        localBroadcastManager.unregisterReceiver(connectingReceiver);
        localBroadcastManager.unregisterReceiver(connectFailReceiver);
        TimeStart2Stop.timeNeed(BLEConnect2Activity.this, TAG + " onDestroy", last);
    }

}
