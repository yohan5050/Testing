package com.google.cloud.android.reminderapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

public class RecTimeActivity extends AppCompatActivity {
    public static Activity RTActivity;
    String fileName, returnedValue, alarmTime, contentValue;
    DataBase db;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_time);

        RTActivity = this;
        db = Main2Activity.getDBInstance();
        textView = (TextView) findViewById(R.id.text);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //메인으로 돌아간다
                finish();
            }
        });

        //RecordActivity로부터 전달받은 intent를 확인한다.
        Intent intent = getIntent();
        fileName = intent.getStringExtra("f_name");
        returnedValue = intent.getStringExtra("r_value");
        alarmTime = intent.getStringExtra("a_time");
        contentValue = intent.getStringExtra("c_value");
    }

    @Override
    protected void onStart() {
        super.onStart();

        String[] words = alarmTime.split(":");
        if (Integer.parseInt(words[3]) < 10) words[3] = '0' + words[3];
        if (Integer.parseInt(words[4]) < 10) words[4] = '0' + words[4];


        String timeRegistered = words[3] + ":" + words[4] + "(" + words[2] + "일" + ")" + "알람";
        String alarmText = new String();

        if (contentValue.equals("")) {
            textView.setText(timeRegistered + "\n" + "내용 없음");
            alarmText = timeRegistered;
        }
        else {
//            textView.setText(timeRegistered + "\n" + recordCutValue(contentValue, 2));
//            alarmText = timeRegistered + "\n" + recordCutValue(contentValue, 2);
            textView.setText(timeRegistered + "\n" + contentValue);
            alarmText = timeRegistered + "\n" + contentValue;
        }

        db.insert(fileName, alarmTime, returnedValue);
        System.out.println("db에 시간메모 저장");
        Toast.makeText(getApplicationContext(), returnedValue, Toast.LENGTH_LONG).show();

        ///////////////////////////////알람 설정 //////////////////////////////////////////
        ///////////////////////알람 설정//////////////////////////
        System.out.println("알람 시간 형식 : " + alarmTime);
        //SharedPreferences 사용해서 누적된 알람의 개수 저장
        SharedPreferences alarmNumPref = getSharedPreferences("anPref", MODE_PRIVATE);
        int alarmNum = alarmNumPref.getInt("anum", 0); //anum에 해당하는 값이 없으면 0을 받아온다.
        SharedPreferences.Editor editor = alarmNumPref.edit();
        editor.putInt("anum", alarmNum + 1); // 값 수정.
        editor.commit();

        //SharedPreferences 사용해서 파일 이름에 해당하는 펜딩인텐트 request code 저장
        SharedPreferences pIntentPref = getSharedPreferences("piPref", MODE_PRIVATE);
        SharedPreferences.Editor piEditor = pIntentPref.edit();
        piEditor.putInt(fileName, alarmNum);
        piEditor.commit();
        System.out.println("piPref put " + alarmNum);

        int rCode = alarmNum;

        Calendar mCalendar = Calendar.getInstance();
        int yy, MM, dd, hh, mm;
        yy = 2000 + Integer.parseInt(words[0]);
        MM = Integer.parseInt(words[1]);
        dd = Integer.parseInt(words[2]);
        hh = Integer.parseInt(words[3]);
        mm = Integer.parseInt(words[4]);
        mCalendar.set(yy, MM - 1, dd, hh, mm, 0);

        Intent mAlarmIntent = new Intent("com.google.cloud.android.reminderapp.ALARM_START"); //AlarmSoundService
        mAlarmIntent.putExtra("filename", fileName);
        mAlarmIntent.putExtra("alarmtext", alarmText);
        System.out.println("알람텍스트 in RecTimeActivity : " + alarmText);

        PendingIntent mPendingIntent =
                PendingIntent.getBroadcast(
                        getApplicationContext(),
                        rCode, /*request code*/
                        mAlarmIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        mAlarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                mCalendar.getTimeInMillis(),
                mPendingIntent
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //녹음 후 화면에 표시해주는 것을 설정해준다.
    public String recordCutValue(String contentValue, int i) { //기기에 따라 화면의 한 줄에 들어갈 수 있는 글자 수가 다를 수 있고, 화면도 크기 때문에 사용 안 할 예정.
        String cutvalue = "";

        if (i == 1) {

            if (contentValue.length() > 27) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(8, 18) + "\n" + contentValue.substring(18, 27) + "..";
            }
            else if (contentValue.length() > 18) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "\n" + contentValue.substring(18, contentValue.length());
            }
            else if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        else if (i == 2) {

            //두줄 처리
            if (contentValue.length() > 18) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "..";
            }
            else if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

            //한줄 처리일 경우

            if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 7) + "..";
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        return cutvalue;
    }
}
