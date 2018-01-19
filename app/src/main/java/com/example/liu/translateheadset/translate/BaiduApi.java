package com.example.liu.translateheadset.translate;

import com.example.liu.translateheadset.util.HttpGet;
import com.example.liu.translateheadset.util.MD5;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Callback;

/**
 * Created by pzbz025 on 2017/11/14.
 */

public class BaiduApi {
    private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

    private String appid;
    private String securityKey;

    public BaiduApi(String appid, String securityKey) {
        this.appid = appid;
        this.securityKey = securityKey;
    }

    public void getTransResult(String query, String from, String to, Callback callback) {
        Map<String, String> params = buildParams(query, from, to);
        HttpGet.get(TRANS_API_HOST, params, callback);
    }

    private Map<String, String> buildParams(String query, String from, String to) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("q", query);
        params.put("from", from);
        params.put("to", to);

        params.put("appid", appid);

        // 随机数
        String salt = String.valueOf(System.currentTimeMillis());
        params.put("salt", salt);

        // 签名
        String src = appid + query + salt + securityKey; // 加密前的原文
        params.put("sign", MD5.md5(src));

        return params;
    }
}
