package com.example.liu.translateheadset.gson;

/**
 * Created by pzbz025 on 2017/11/20.
 */

public class WakeUp {

    /**
     * errorDesc : wakup success
     * errorCode : 0
     * word : 翻译中文
     */

    private String errorDesc;
    private int errorCode;
    private String word;

    public String getErrorDesc() {
        return errorDesc;
    }

    public void setErrorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
