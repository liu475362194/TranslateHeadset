package com.example.liu.translateheadset.translate;

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

    public void getTransResult(String query, String from, String to, Callback callback) {
        Map<String, String> params = buildParams(query, from, to);
        HttpGet.get(TRANS_API_HOST, params, callback);
    }

    private Map<String, String> buildParams(String query, String from, String to) {
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
}
