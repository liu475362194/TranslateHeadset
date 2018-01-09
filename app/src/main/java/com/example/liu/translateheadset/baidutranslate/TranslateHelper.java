package com.example.liu.translateheadset.baidutranslate;

import android.util.Log;

import com.example.liu.translateheadset.gson.Translate;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by pzbz025 on 2017/12/11.
 */

public class TranslateHelper {
    private TransApi transApi;
    private static final String APP_ID = "20171114000095150";
    private static final String SECURITY_KEY = "iKpMRhTW0IOBXJXbhNpw";
    private static final String TAG = "TranslateHelper";
    private Gson gson = new Gson();

    public TranslateHelper() {
        transApi = new TransApi(APP_ID, SECURITY_KEY);
    }

    public void startTranslate(String query, okhttp3.Callback callback){
        if (query.equals("")) {
            return;
        }
        final String toText = isEnglish(query);
        Log.d(TAG, "startTrans: " + query);
        String mResponse;
        transApi.getTransResult(query, "auto", toText, callback);
    }

    public String responseTrans(Response response) throws IOException {
        String result = response.body().string();
        Translate translate = gson.fromJson(result, Translate.class);
        if (null == translate.getTrans_result()) {
            return null;
        }
        final String translateResult = translate.getTrans_result().get(0).getDst();
        Log.d(TAG, "responseTrans: " + translateResult);
        return translateResult;
    }


    /**
     * 判断需要翻译的内容是中文还是英文
     *
     * @param text
     * @return
     */
    private String isEnglish(String text) {
        int n = 0, e = 0, c = 0;
        Log.d(TAG, "isEnglish: " + text.length());
        for (int i = 0; i < text.length(); i++) {
            String str = String.valueOf(text.charAt(i));
            Log.d(TAG, "isEnglish: str" + str);
            Pattern p = Pattern.compile("[0-9]*");
            Matcher m = p.matcher(str);
            if (m.matches()) {
                Log.d(TAG, "isEnglish: 识别为数字");
                n++;
            }
            p = Pattern.compile("[a-zA-Z]");
            m = p.matcher(str);
            if (m.matches()) {
                Log.d(TAG, "isEnglish: 识别为英文");
                e++;
            }
            p = Pattern.compile("[\u4e00-\u9fa5]");
            m = p.matcher(str);
            if (m.matches()) {
                Log.d(TAG, "isEnglish: 识别为中文");
                c++;
            }
        }
        Log.d(TAG, "isEnglish: n = " + n + " e = " + e + " c = " + c);
        if (e > 0 && c == 0) {
            return "zh";
        } else if (c > 0) {
            return "en";
        }
        return "en";
    }
}
