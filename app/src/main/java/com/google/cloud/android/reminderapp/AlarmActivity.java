package com.google.cloud.android.reminderapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class AlarmActivity extends AppCompatActivity {

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
                Main2Activity.mVoicePlayer.stopPlaying();
                finish();
            }
        });
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