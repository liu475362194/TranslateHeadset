package com.example.liu.translateheadset.translate;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.liu.translateheadset.util.HttpGet;
import com.example.liu.translateheadset.util.TimeStart2Stop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private static final String TAG = "GoogleApi";

    /**
     * 单例方式获取入口
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


    public GoogleApi setContext(Context context) {
        this.mContext = context;
        return instance;
    }

    public void getTransResult(String query, String from, String to) {
        Map<String, String> params = buildParams(query, from, to);
        String downloadUrl = HttpGet.getInstance().getUrlWithQueryString(TRANS_API_HOST, params);
        download(downloadUrl,"GoogleTranslate.txt");

//        downAsynFile(downloadUrl);
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
    private void download(String versionUrl, String versionName) {
        long last = TimeStart2Stop.timeNeed(mContext, "GoogleApi", -1);
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(versionUrl));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(versionUrl));
        request.setMimeType(mimeString);

        //在通知栏中不显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        //sdcard的目录下的download文件夹，必须设置
        request.setDestinationInExternalPublicDir("/GoogleTranslate/", versionName);
//        request.setDestinationInExternalFilesDir(mContext, Environment.getExternalStorageDirectory() + "/GoogleTranslate/",versionName); //也可以自己制定下载路径

        Log.d(TAG, "download: " + versionUrl);
        Log.d(TAG, "download: "
                + Environment.getExternalStorageDirectory()
                + "/GoogleTranslate/"
                + versionName
                + "---->"
                + fileIsExists(Environment.getExternalStorageDirectory() + "/GoogleTranslate/" + versionName));

        //将下载请求加入下载队列
        downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        try {
            mTaskId = downloadManager.enqueue(request);
        } catch (Exception e) {
            Log.d(TAG, "download: " + e.getMessage());
        }

        //注册广播接收者，监听下载状态
//        mContext.registerReceiver(receiver,
//                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        TimeStart2Stop.timeNeed(mContext, "GoogleApi", last);
    }

    /**
     * 判断文件是否存在
     *
     * @param fileUrl
     * @return
     */
    private boolean fileIsExists(String fileUrl) {
        File file;
        try {
            file = new File(fileUrl);
            if (!file.exists()) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        file.delete();
        return true;

    }


    private void downAsynFile(String versionUrl) {
        OkHttpClient mOkHttpClient = new OkHttpClient();
        String url = versionUrl;
        Log.d(TAG, "downAsynFile:url: " + url);
        Request request = new Request.Builder().url(url).header("Range", "bytes=0-").build();
        mOkHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) {
                InputStream inputStream = response.body().byteStream();


                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/GoogleTranslate/f.txt"));
                    byte[] buffer = new byte[1024];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.flush();
                } catch (IOException e) {
                    Log.i(TAG, "IOException");
                    e.printStackTrace();
                }
                Log.d(TAG, "文件下载成功");


//                StringBuilder builder = new StringBuilder();
//                BufferedReader reader = null;
//                try {
//                    reader = new BufferedReader(new InputStreamReader(inputStream, "GB2312"));
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        builder.append(line + "\n");
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        reader.close();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//                Log.d(TAG, "onResponse:builder " + builder.toString());
            }
        });
    }

}
