package com.example.liu.translateheadset.gson;

/**
 * Created by pzbz025 on 2017/11/21.
 */

public class TalkAll {
    private int who;
    private String translateText;
    private String yuanWen;

    public String getYuanWen() {
        return yuanWen;
    }

    public void setYuanWen(String yuanWen) {
        this.yuanWen = yuanWen;
    }

    public int getWho() {
        return who;
    }

    public void setWho(int who) {
        this.who = who;
    }

    public String getTranslateText() {
        return translateText;
    }

    public void setTranslateText(String translateText) {
        this.translateText = translateText;
    }
}
