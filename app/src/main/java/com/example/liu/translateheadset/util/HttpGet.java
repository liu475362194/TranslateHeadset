package com.example.liu.translateheadset.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;

import static android.content.ContentValues.TAG;

/**
 * Created by pzbz025 on 2017/11/14.
 */

public class HttpGet {
    protected static final int SOCKET_TIMEOUT = 10000; // 10S
    protected static final String GET = "GET";
    private static final String TAG = "HttpGet";
    private static HttpGet instance;

    public static HttpGet getInstance(){
        if (instance != null){
            return instance;
        }
        instance = new HttpGet();
        return instance;
    }

    public void get(String host, Map<String, String> params, final okhttp3.Callback callback) {
        try {
            // 设置SSLContext
            SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, new TrustManager[] { myX509TrustManager }, null);

            //将请求的地址值和需要的配置项以键值对（params）的形式封装并由getUrlWithQueryString拼接起来
            //getUrlWithQueryString方法最后返回最终的请求地址sendUrl
            final String sendUrl = getUrlWithQueryString(host, params);

            // System.out.println("URL:" + sendUrl);
            Log.d(TAG, "get: " + sendUrl);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //发送请求,并通过callback通知
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(sendUrl).build();
                    client.newCall(request).enqueue(callback);
                }
            }).start();

//            URL uri = new URL(sendUrl); // 创建URL对象
//            HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
//            if (conn instanceof HttpsURLConnection) {
//                ((HttpsURLConnection) conn).setSSLSocketFactory(sslcontext.getSocketFactory());
//            }
//
//            conn.setConnectTimeout(SOCKET_TIMEOUT); // 设置相应超时
//            conn.setRequestMethod(GET);
//            int statusCode = conn.getResponseCode();
//            if (statusCode != HttpURLConnection.HTTP_OK) {
//                System.out.println("Http错误码：" + statusCode);
//            }
//
//            // 读取服务器的数据
//            InputStream is = conn.getInputStream();
//            BufferedReader br = new BufferedReader(new InputStreamReader(is));
//            StringBuilder builder = new StringBuilder();
//            String line = null;
//            while ((line = br.readLine()) != null) {
//                builder.append(line);
//            }
//
//            String text = builder.toString();
//
//            close(br); // 关闭数据流
//            close(is); // 关闭数据流
//            conn.disconnect(); // 断开连接
//
//            return text;
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
        }
        catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将请求的地址值和需要的配置项以键值对（params）的形式封装并由getUrlWithQueryString拼接起来
     * @param url 请求的地址
     * @param params 所有配置项
     * @return 最终拼接成的地址
     */
    public String getUrlWithQueryString(String url, Map<String, String> params) {
        if (params == null) {
            return url;
        }

        //如果给的请求地址包含 ? 就说明已含有至少一个配置项，就在最后加 &，否则加 ?
        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            builder.append("&");
        } else {
            builder.append("?");
        }

        int i = 0;
        Log.d(TAG, "-------------------------------------------------------------------------------");
        for (String key : params.keySet()) {
            Log.d(TAG, "key= "+ key + " and value= " + params.get(key));
            String value = params.get(key);
            if (value == null) { // 过滤空的key
                continue;
            }

            if (i != 0) {
                builder.append('&');
            }

            builder.append(key);
            builder.append('=');
            builder.append(encode(value));
//            builder.append(value);

            i++;
        }

        return builder.toString();
    }

    protected static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 对输入的字符串进行URL编码, 即转换为%20这种形式
     *
     * @param input 原文
     * @return URL编码. 如果编码失败, 则返回原文
     */
    public static String encode(String input) {
        if (input == null) {
            return "";
        }

        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return input;
    }

    private static TrustManager myX509TrustManager = new X509TrustManager() {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    };

}
