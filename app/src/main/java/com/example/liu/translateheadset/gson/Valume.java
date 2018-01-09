package com.example.liu.translateheadset.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by pzbz025 on 2017/11/17.
 */

public class Valume {

    /**
     * volume-percent : 1
     * volume : 73
     */

    @SerializedName("volume-percent")
    private int volumepercent;
    private int volume;

    public int getVolumepercent() {
        return volumepercent;
    }

    public void setVolumepercent(int volumepercent) {
        this.volumepercent = volumepercent;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }
}
