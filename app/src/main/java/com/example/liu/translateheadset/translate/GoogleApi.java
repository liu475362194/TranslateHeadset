package com.example.liu.translateheadset.translate;

import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import com.example.liu.translateheadset.util.HttpGet;
import com.example.liu.translateheadset.util.MD5;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;

/**
 * Created by pzbz025 on 2018/1/19.
 */

public class GoogleApi {
    /**
     * http://translate.google.cn/translate_a/single?client=gtx&sl=zh-CN&tl=en&dt=t&q=你的名字
     */

    private static final String TRANS_API_HOST = "http://translate.google.cn/translate_a/single?client=gtx&dt=t";
    public DownloadManager downloadManager;
    public static GoogleApi instance;
    private Context mContext;
    public long mTaskId;

    /**
     * 单例方式获取蓝牙通信入口
     *
     * @return 返回ViseBluetooth
     */
    public static GoogleApi getInstance() {
        if (instance == null) {
            synchronized (GoogleApi.class) {
                if (instance == null) {
                    instance = new GoogleApi();
                }
            }
        }
        return instance;
    }


    public GoogleApi setContext(Context context){
        this.mContext = context;
        return instance;
    }

    public void getTransResult(String query, String from, String to) {
        Map<String, String> params = buildParams(query, from, to);
        String downloadUrl = HttpGet.getInstance().getUrlWithQueryString(TRANS_API_HOST,params);
        downloadAPK(downloadUrl,"GoogleTranslate.txt");
    }

    private static Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("sl", from);
        params.put("tl", to);

//        // 随机数
//        String salt = String.valueOf(System.currentTimeMillis());
//        params.put("salt", salt);

//        // 签名
//        String src = appid + query + salt + securityKey; // 加密前的原文
//        params.put("sign", MD5.md5(src));

        return params;
    }

    //使用系统下载器下载
    private void downloadAPK(String versionUrl, String versionName) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
        request.setMimeType(mimeString);

        //在通知栏中不显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
//        request.setVisibleInDownloadsUi(true);

        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir("/GoogleTranslate/", versionName);
        //request.setDestinationInExternalFilesDir(),也可以自己制定下载路径

        //将下载请求加入下载队列
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);

        //注册广播接收者，监听下载状态
//        mContext.registerReceiver(receiver,
//                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

}
