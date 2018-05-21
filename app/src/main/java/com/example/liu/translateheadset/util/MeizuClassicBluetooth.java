package com.example.liu.translateheadset.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * 经典蓝牙的搜索和连接
 * Created by liu on 2018/3/20.
 */

public class MeizuClassicBluetooth {
    private static final String TAG = "MeizuPairingActivity";
    private BluetoothAdapter mBluetoothAdapter;
    private WeakReference<Context> context;
    public static MeizuClassicBluetooth instance;

    public MeizuClassicBluetooth(Context context) {
//        this.context = context;
        this.context = new WeakReference<>(context);
    }

    public static MeizuClassicBluetooth getInstance(Context context) {
        if (instance == null) {
            instance = new MeizuClassicBluetooth(context);
        }
        return instance;
    }


    private void init() {
        checkBleDevice(context.get());
        // 分别注册三种类型的广播接收器：
        // 1.BluetoothDevice.ACTION_FOUND —— 发现了一台蓝牙设备
        // 2.BluetoothAdapter.ACTION_DISCOVERY_FINISHED —— 扫描设备的动作完成
        // 3.BluetoothConstants.PAIRING_REQUEST —— 触发了配对请求
        // 4.BluetoothDevice.ACTION_BOND_STATE_CHANGED —— 当前的绑定状态返回
        IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.get().registerReceiver(mReceiver, foundFilter);
        IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.get().registerReceiver(mReceiver, discoveryFilter);
        IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.get().registerReceiver(mReceiver, bondFilter);
        IntentFilter stateFilter = new IntentFilter(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        context.get().registerReceiver(mReceiver, stateFilter);
    }

    /**
     * 判断是否支持蓝牙，并打开蓝牙
     * 获取到BluetoothAdapter之后，还需要判断是否支持蓝牙，以及蓝牙是否打开。
     * 如果没打开，需要让用户打开蓝牙：
     */
    private void checkBleDevice(Context context) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.e(TAG, "Unable to initialize BluetoothManager.");
        } else {
            mBluetoothAdapter = bluetoothManager.getAdapter();
        }

        if (mBluetoothAdapter != null) {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            }
        } else {
            Log.i("blueTooth", "该手机不支持蓝牙");
        }
    }

    /**
     * 获取手机是否已连接经典蓝牙耳机。
     *
     * @return
     */
    public boolean getConnectionBluetooth() {
        checkBleDevice(context.get());

        boolean bluetoothDevices = mBluetoothAdapter.getProfileConnectionState(BluetoothHeadset.HEADSET) == BluetoothHeadset.STATE_CONNECTED
                && mBluetoothAdapter.isEnabled()
                && mBluetoothAdapter != null;
//        Toast.makeText(context.get(),"目前设备：" + bluetoothDevices,Toast.LENGTH_SHORT).show();
        return bluetoothDevices;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("WrongConstant")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (BluetoothDevice.ACTION_FOUND.equals(action)) { // 每发现一台蓝牙设备都会发出此广播
                Log.i("test", "发现了一台蓝牙设备！" + device.getName() + "，address" + device.getAddress());
                // 处理获取到的蓝牙设备device，一般是将其存入一个蓝牙设备列表list
                if (null != device.getName() && device.getName().contains("MEIZU LASER G20")) {
//                if (null != device.getName() && device.getName().contains("JVC HA-S38BT")) {
                    Log.d(TAG, "onReceive: 已找到‘MEIZU LASER G20’");
                    //找到了设备，停止搜索
                    mBluetoothAdapter.cancelDiscovery();
                    if (device.getBluetoothClass().getMajorDeviceClass() != BluetoothClass.Device.Major.AUDIO_VIDEO) {
                        //如果待连接设备不是蓝牙耳机，则不连接
                        return;
                    }
                    mBluetoothAdapter.getProfileProxy(context, new BluetoothProfile.ServiceListener() {
                        @Override
                        public void onServiceConnected(int profile, BluetoothProfile proxy) {
                            BluetoothHeadset bluetoothHeadset = (BluetoothHeadset) proxy;
                            Class btHeadsetCls = BluetoothHeadset.class;
                            try {
                                //调用BluetoothDevice内置的connect方法，去连接待连接的蓝牙耳机设备。
                                Method connect = btHeadsetCls.getMethod("connect", BluetoothDevice.class);
                                connect.setAccessible(true);
                                connect.invoke(bluetoothHeadset, device);
                            } catch (Exception e) {
                                Log.e(TAG, e + "");
                            }
                        }

                        @Override
                        public void onServiceDisconnected(int profile) {

                        }
                    }, BluetoothProfile.HEADSET);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i(TAG, "扫描蓝牙设备结束！");
                // 进行设备扫描结束的动作
            } else if (BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                Log.d(TAG, "onReceive: ACTION_STATE_CHANGED" + mBluetoothAdapter.getState());
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.i(TAG, "蓝牙绑定状态改变！");
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.i(TAG, "设备正在绑定中...");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.i(TAG, "设备已绑定！");
                        closeReceiver();
//                        Intent localIntent = new Intent();
//                        localIntent.setClass(context, MeizuGlowActivity.class);
//                        context.startActivity(localIntent);
//                        ((AppCompatActivity) context).finish();
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.i(TAG, "设备未绑定！");
                        break;
                }
            }
        }
    };

    /**
     * 开始搜索经典蓝牙
     */
    public void startSearthBltDevice() {
        init();
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }

    private void closeReceiver() {
        context.get().unregisterReceiver(mReceiver);
    }
}
