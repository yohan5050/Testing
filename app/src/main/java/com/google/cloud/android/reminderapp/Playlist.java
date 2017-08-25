package com.google.cloud.android.reminderapp;

/**
 * Created by 이상원 on 2017-07-27.
 */

public class Playlist {
    String content;

    public Playlist(String content) {
        this.content = content;
    }

    public String getName() {
        return content;
    }

    public void setName(String content) {
        this.content = content;
    }
}