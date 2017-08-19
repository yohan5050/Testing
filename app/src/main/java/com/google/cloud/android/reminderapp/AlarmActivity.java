package com.google.cloud.android.reminderapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AlarmActivity extends AppCompatActivity {

    public static Handler ahandler; // 알람 화면 처리 핸들러(알람이 끝나면 알람 화면 종료하도록)
    TextView textView;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        textView = (TextView) findViewById(R.id.text);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "알람을 종료합니다.", Toast.LENGTH_LONG).show();
                if(Main2Activity.mVoicePlayer != null)
                    Main2Activity.mVoicePlayer.stopPlaying();

                if(AlarmSoundService.mVoicePlayerAlarm != null) //프로그램이 종료된 상태에서 알람이 울리는 경우에 알람 처리
                    AlarmSoundService.mVoicePlayerAlarm.stopPlaying();

                finish();
            }
        });

        //화면이 OFF 되어있는 상태에서도 알람 화면이 나오도록 하는 코드. // 참고 : http://cofs.tistory.com/173
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);


        //VoicePlayer의 playWaveFileAlarm에서 알람이 11번 울리고 끝나면 stop message가 전달되고, AlarmActivity를 종료한다.
        ahandler = new Handler() {
            public void handleMessage(Message msg) {
                if(((String) msg.obj).equals("stop")) {
                    Toast.makeText(getApplicationContext(), "알람을 종료합니다.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = getIntent();

        String alarmText = intent.getStringExtra("alarmtext");
        System.out.println("알람텍스트 in AlarmActivity : " + alarmText);
        textView.setText(alarmText);
    }
}