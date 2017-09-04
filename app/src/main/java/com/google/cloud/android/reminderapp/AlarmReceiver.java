package com.google.cloud.android.reminderapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tsengvn.typekit.TypekitContextWrapper;

/**
 * Created by 이상원 on 2017-08-06.
 */

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Intent mServiceintent = new Intent(context, AlarmSoundService.class);

        String fileName = intent.getStringExtra("filename");
        String alarmText = intent.getStringExtra("alarmtext");
        System.out.println("알람텍스트 in AlarmReceiver : " + alarmText);
        mServiceintent.putExtra("filename", fileName);
        mServiceintent.putExtra("alarmtext", alarmText);

        context.startService(mServiceintent);
    }
}