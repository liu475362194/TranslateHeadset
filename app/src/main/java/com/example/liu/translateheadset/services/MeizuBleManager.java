package com.example.liu.translateheadset.services;

import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleGattCallback;
import com.clj.fastble.callback.BleNotifyCallback;
import com.clj.fastble.callback.BleScanAndConnectCallback;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.callback.BleWriteCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.exception.BleException;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.clj.fastble.utils.HexUtil;
import com.example.liu.translateheadset.DemoApplication;

import java.util.List;

/**
 * Created by liu on 2018/3/19.
 */

public class MeizuBleManager {
    private static final String TAG = "MeizuBleManager";
    public static MeizuBleManager instance;
    private Context context;
    private BleDevice mBleDevice;
    private OnScanListener onScanListener;

    public interface OnScanListener {
        void scan(BleDevice result);

        void connectionSuccess(BleDevice result);

        void disconnection();
    }


    public MeizuBleManager(Context context) {
        //设置超时，默认5秒
        BleManager.getInstance().setOperateTimeout(5000);

        if (!BleManager.getInstance().isSupportBle()) {
//            Toast.makeText(context, R.string.not_support_ble, Toast.LENGTH_SHORT).show();
        }

        initScanRule();

        this.context = context;
    }


    /**
     * 配置扫描规则
     */
    private void initScanRule() {
        String names[] = {"MEIZU Laser"};
        boolean isAutoConnect = true;
        BleScanRuleConfig scanRuleConfig = new BleScanRuleConfig.Builder()
                //.setServiceUuids(serviceUuids)      // 只扫描指定的服务的设备，可选
//                .setDeviceName(true, names)         // 只扫描指定广播名的设备，可选
                //.setDeviceMac(mac)                  // 只扫描指定mac的设备，可选
                .setAutoConnect(isAutoConnect)      // 连接时的autoConnect参数，可选，默认false
                .setScanTimeOut(10000)              // 扫描超时时间，可选，默认10秒；小于等于0表示不限制扫描时间
                .build();
        BleManager.getInstance().initScanRule(scanRuleConfig);
    }

    public static MeizuBleManager getInstance(Context context) {
        if (instance == null) {
            instance = new MeizuBleManager(context);
        }
        return instance;
    }

    public void scan(final OnScanListener onScanListener) {
        this.onScanListener = onScanListener;
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanStarted(boolean success) {
                // 开始扫描（主线程）
                Log.d(TAG, "onScanStarted:开始扫描（主线程） ");
            }

            @Override
            public void onScanning(BleDevice result) {
// 扫描到一个符合扫描规则的BLE设备（主线程）
                Log.d(TAG, "onScanning:扫描到一个符合扫描规则的BLE设备（主线程） " + result.getName());
                onScanListener.scan(result);
            }

            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
// 扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程）
                Log.d(TAG, "onScanFinished:扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程） ");
            }
        });
    }

    public void connect(BleDevice result) {
        BleManager.getInstance().connect(result, new BleGattCallback() {
            @Override
            public void onStartConnect() {
                Log.d(TAG, "onStartConnect:开始连接 ");
            }

            @Override
            public void onConnectFail(BleDevice bleDevice, BleException exception) {
                Log.d(TAG, "onConnectFail:连接失败 ");
            }

            @Override
            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onConnectSuccess:连接成功，BleDevice即为所连接的BLE设备 ");
                onScanListener.connectionSuccess(bleDevice);
                addBroadcast("com.example.broadcasttest.CONNECTED");
                bleNotify();
            }

            @Override
            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
                Log.d(TAG, "onDisConnected:连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法 ");
                onScanListener.disconnection();
                addBroadcast("com.example.broadcasttest.DISCONNECT");
            }
        });
    }

//    public void scanAndConnect(IPairingModel.OnBLEConnectListener onBLEConnectListener) {
//        BleManager.getInstance().scanAndConnect(new BleScanAndConnectCallback() {
//            @Override
//            public void onScanStarted(boolean success) {
//                // 开始扫描（主线程）
//                Log.d(TAG, "onScanStarted:开始扫描（主线程） ");
//                onBLEConnectListener.onScanStarted();
//
//            }
//
//            @Override
//            public void onScanFinished(BleDevice scanResult) {
//                if (scanResult != null) {
//                    Log.d(TAG, "onScanFinished:扫描结束，列出所有扫描到的符合扫描规则的BLE设备（主线程） " + scanResult.getName());
//                } else {
////                    MeizuPairingActivity.getInstance().bt_pairing.setText(R.string.restart_connect);
////                    MeizuPairingActivity.getInstance().bt_pairing.setClickable(true);
////                    Log.d(TAG, "onScanFinished:扫描结束,无任何符合扫描规则的BLE设备 ");
////                    Log.d(TAG, "onScanFinished:扫描到设备：" + scanResult.getName());
//                    onBLEConnectListener.onScanFinished();
//                }
//
//            }
//
//            @Override
//            public void onStartConnect() {
//                Log.d(TAG, "onStartConnect:开始连接 ");
//                onBLEConnectListener.onStartConnect();
//            }
//
//            @Override
//            public void onConnectFail(BleException exception) {
////                MeizuPairingActivity.getInstance().bt_pairing.setText(R.string.restart_connect);
////                MeizuPairingActivity.getInstance().bt_pairing.setClickable(true);
//                Log.d(TAG, "onConnectFail:连接失败 ");
//                onBLEConnectListener.connectFail();
//            }
//
//            @Override
//            public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
//                Log.d(TAG, "onConnectSuccess:连接成功，BleDevice即为所连接的BLE设备 " + bleDevice.getName());
//                mBleDevice = bleDevice;
//
//                bleNotify(onBLEConnectListener);
//            }
//
//            @Override
//            public void onDisConnected(boolean isActiveDisConnected, BleDevice device, BluetoothGatt gatt, int status) {
//                Log.d(TAG, "onDisConnected:连接中断，isActiveDisConnected表示是否是主动调用了断开连接方法 " + isActiveDisConnected);
//                if (!isActiveDisConnected) {
//                    onBLEConnectListener.onDisConnected();
//                    Toast.makeText(MyApplication.getContext(), R.string.on_disconnected, Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//    }

    public void write(final String data) {
        if (BleManager.getInstance().isConnected(mBleDevice)) {
            BleDevice bleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
            String service_uuid = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
            String characteristic_write_uuid = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
            BleManager.getInstance().write(
                    bleDevice,
                    service_uuid,
                    characteristic_write_uuid,
                    toBytes(data),
                    new BleWriteCallback() {
                        @Override
                        public void onWriteSuccess(int current, int total, byte[] justWrite) {
                            Log.d(TAG, "onWriteSuccess: 发送数据" + data + "到设备成功");
                        }

                        @Override
                        public void onWriteFailure(BleException exception) {
                            Log.d(TAG, "onWriteFailure: 发送数据" + data + "到设备失败");
                        }
                    }
            );
        } else {
//            Toast.makeText(MyApplication.getContext(), R.string.on_disconnected, Toast.LENGTH_SHORT).show();
        }
    }

    public void bleNotify() {
        BleDevice bleDevice = BleManager.getInstance().getAllConnectedDevice().get(0);
        String service_uuid = "65786365-6c70-6f69-6e74-2e636f810000";
        String characteristic_uuid = "65786365-6c70-6f69-6e74-2e636f810001";
        BleManager.getInstance().notify(
                bleDevice, service_uuid, characteristic_uuid,
                new BleNotifyCallback() {
                    @Override
                    public void onNotifySuccess() {
                        Log.d(TAG, "onNotifySuccess: 通知打开成功");
                        write("55");
                    }

                    @Override
                    public void onNotifyFailure(BleException exception) {
                        Log.d(TAG, "onNotifyFailure: 通知打开失败");
                    }

                    @Override
                    public void onCharacteristicChanged(byte[] data) {
                        String resule = HexUtil.formatHexString(data);
                        Log.d(TAG, "onCharacteristicChanged: 收到的通知是" + resule);
                        if (resule.equals("84"))
                            addBroadcast("com.example.broadcasttest.NOTIFY");

                    }
                }
        );

    }

    private void addBroadcast(String action) {
        Intent intent = new Intent(action);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(DemoApplication.getInstance());
        localBroadcastManager.sendBroadcast(intent);
    }

    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }
}
