package com.google.cloud.android.reminderapp;

import android.util.StringBuilderPrinter;

/**
 * Created by 이상원 on 2017-07-27.
 */

public class Playlist {
    String content;
    String alarmTime;
    int resId;
    String strColor;

    public Playlist(String content, String alarmTime, int resId, String strColor) {
        this.content = content;
        this.alarmTime = alarmTime;
        this.resId = resId;
        this.strColor = strColor;
    }

    public String getContent() {
        return content;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public int getResId() {
        return resId;
    }

    public String getStrColor() {
        return strColor;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public void setStrColor(String strColor) {
        this.strColor = strColor;
    }
}