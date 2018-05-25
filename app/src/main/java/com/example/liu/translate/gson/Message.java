package com.example.liu.translate.gson;

/**
 * Created by pzbz025 on 2017/12/11.
 */

public class Message {
    private int type;
    private String message;
    private String translate;

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
