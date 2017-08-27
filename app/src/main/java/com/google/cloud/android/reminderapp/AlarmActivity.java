package com.google.cloud.android.reminderapp;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;


public class AlarmActivity extends AppCompatActivity {

    public static Handler ahandler; // 알람 화면 처리 핸들러(알람이 끝나면 알람 화면 종료하도록)
    TextView textView;
    ImageButton button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        textView = (TextView) findViewById(R.id.text);
        button = (ImageButton) findViewById(R.id.button);

        //진동 - 참조 : http://bitsoul.tistory.com/129
        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(new long[] {500, 1000}, 0); //진동 패턴: 대기, 진동,.. / 0: 무한 반복, -1: 반복 없음.

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "알람을 종료합니다.", Toast.LENGTH_LONG).show();
                if(Main2Activity.mVoicePlayer != null)
                    Main2Activity.mVoicePlayer.stopPlaying();

                if(AlarmSoundService.mVoicePlayerAlarm != null) //프로그램이 종료된 상태에서 알람이 울리는 경우에 알람 처리
                    AlarmSoundService.mVoicePlayerAlarm.stopPlaying();

                vibrator.cancel(); //진동 취소
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

    @Override
    protected void onStop() {
        super.onStop();
        //알람 화면이 사라질 경우(예를 들어 홈버튼, 최근 사용 앱 확인 버튼을 누르는 경우) 알람을 종료한다
//        button.callOnClick(); // 화면이 꺼져있으면 AlarmActivity가 2번 호출되는 듯한 현상이 일어난다. 즉, onStop이 한번 더 호출되어
        //시작도 전에 종료가 된다. 그래서 onUserLeaveHint()를 사용하기로 했다.
    }

    @Override
    protected void onUserLeaveHint() { //홈버튼, 멀티버튼을 누르거나 이 액티비티에서 다른 액티비티로 넘어갈 때 onPause직전에 호출된다.
        //다른 액티비티로 넘어갈 때 호출되지 않는 방법은 해당 인텐트에 FLAG_ACTIVITY_NO_USER_ACTION를 추가하면 되는듯.
        //하지만 AlarmActivity에서 다른 액티비를 호출하는 일은 없고, 단지 홈버튼을 누르거나 최근 사용 앱 확인버튼(멀티버튼)을 누를 경우
        //종료하는 것이 목적이므로 그냥 써도 될 것 같다.
        super.onUserLeaveHint();

        button.callOnClick();
    }

    @Override
    public void onBackPressed() { //back button을 누르면 알람이 종료되도록 한다.
        super.onBackPressed();

        button.callOnClick();
    }
}