package com.google.cloud.android.reminderapp;

/**
 * Created by 이상원 on 2017-07-27.
 */

public class Playlist {
    String content;
    String alarmTime;
    int resId;

    public Playlist(String content, String alarmTime, int resId) {
        this.content = content;
        this.alarmTime = alarmTime;
        this.resId = resId;
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

    public void setContent(String content) {
        this.content = content;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }
}