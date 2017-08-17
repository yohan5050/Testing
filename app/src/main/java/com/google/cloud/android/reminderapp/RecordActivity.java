package com.google.cloud.android.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Calendar;


public class RecordActivity extends AppCompatActivity {

    DataBase db;
//    VoiceRecorder mVoiceRecorder;
    public static CountDownTimer timer;
    TextView mText;
    Button stopBtn;
    int value;
    String fileName;

    private SpeechService mSpeechService;

    TimeAnalysis timeAnalysis;
    ContentAnalysis contentAnalysis;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        db = Main2Activity.getDBInstance();
//        mVoiceRecorder = new VoiceRecorder(, mVoiceCallback);
        stopBtn = (Button) findViewById(R.id.button);
        mText = (TextView) findViewById(R.id.text);

        timeAnalysis = new TimeAnalysis();
        contentAnalysis = new ContentAnalysis();

        //timer - 시간 제한 7초.
        value = 0;
        timer = new CountDownTimer(7000, 880) {
            @Override
            public void onTick(long millisUntilFinished) {
                mText.setText("녹음 중\n" + (7 - value) + "초 후 종료");
                value++;
            }

            @Override
            public void onFinish() {
                value = 0;
                mText.setText("녹음 종료");
                stopVoiceRecorder();
                timer.cancel();
            }
        };

        stopBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Main2Activity.mVoiceRecorder.isRecording()) {
                    stopVoiceRecorder();
                    timer.cancel();
                }
            }
        });
    }

//    // 완성 후 mVoiceCallback 지워도 상관없으면 지우기.
//    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {
//
//    };

    @Override
    protected void onStart() {
        super.onStart();

        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        if(!Main2Activity.mVoiceRecorder.isRecording()) {
            startVoiceRecorder();

            //TimeLimit 구현
            timer.cancel();
            timer.start(); //타이머 시작
        }
    }

    /**
     * RecordActivity가 멈출 때, SpeechService를 종료한다.
     */
    @Override
    protected void onStop() {
        // Stop Cloud Speech API
        mSpeechService.removeListener(mSpeechServiceListener);
        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    private void startVoiceRecorder() {
//        if (mVoiceRecorder != null) {
//            mVoiceRecorder.stopRecording();
//        }
        Main2Activity.mVoiceRecorder.startRecording();
    }

    private void stopVoiceRecorder() {
        if (Main2Activity.mVoiceRecorder != null) {
            fileName = Main2Activity.mVoiceRecorder.stopRecording();
            FileInputStream fis = null;
            try {
//                String fileName = db.getLastFileName();
                fis = openFileInput(fileName);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            System.out.println("파일 이름을 보내남 : " + fileName);
            mSpeechService.recognizeInputStream(fis);
        }
    }


    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }

    };

    /**
     * 구글 STT 서버로 부터 분석된 텍스트를 받아 onCreate에 있는 handler로 전송시킨다.
     *
     * @param text 파일의 음성을 텍스트로 변경한 값
     * @param isFinal 구글 STT 서버로부터 텍스트를 받았는지 아닌지 확인하는 값
     */
    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if(isFinal) {
                        handleText(text);
                    }
                    else {
                        handleText("");
                    }
//                    if (mText2 != null) {
//                        //if (isFinal) {
//                        Message message = handler.obtainMessage(1, text);
//                        handler.sendMessage(message);
//                        //}
//                    }
                }
            };

    public void handleText(String text) {
        System.out.println("핸들텍스트");
        String returnedValue = text;
        //TODO 나중에 살펴보고 사용되지 않으면 삭제하기
        String extractValue = new String();

        //아무말 없이 취소했을 경우
        if (returnedValue.equals("")) { //음성인식 실패
//            Toast.makeText(getApplicationContext(), "아무말도 안하셨습니다", Toast.LENGTH_LONG).show();
            //여기서 음성인식 실패 activity를 호출한다
            Intent intent = new Intent(getApplicationContext(), RecFailActivity.class);
            startActivity(intent);
            //TODO if else각각에 쓰지말고, 이것을 맨 아래쪽에 하나를 쓰는 것으로 나중에 고치기
            finish();
        }
        else {
            //여기서는 시간표현이 있는지 없는지 판단 후, 시간표현 존재 여부에 따라 엑티비티를 호출한다
            String alarmTime = timeAnalysis.Analysis(returnedValue);
            String contentValue = contentAnalysis.Analysis(returnedValue);

            if (alarmTime.equals("note")) { //시간표현이 없는 경우 -> RecNoTimeActivity
//                db.insert(fileName, "일반 메모", returnedValue); //-> 이것도 시간표현이 없는 결과 activity로 옮겨보자.
                //시간표현이 없는 녹음 결과 activity를 호출한다
                Intent intent = new Intent(getApplicationContext(), RecNoTimeActivity.class);
                intent.putExtra("f_name", fileName);
                intent.putExtra("r_value", returnedValue);
                startActivity(intent);

                finish();
//                recordresult.setText(recordCutValue(returnedValue.replaceAll(" ", ""), 1)); ->RecNoTimeActivity로 옮김

            } else { //시간표현이 있는 경우 -> 여기서 알람 시간 설정을 해줄까 -> 알람 시간 설정은 시간표현 엑티비티 가서 해주자. -> RecTimeActivity
                Intent intent = new Intent(getApplicationContext(), RecTimeActivity.class);
                intent.putExtra("f_name", fileName);
                intent.putExtra("r_value", returnedValue);
                intent.putExtra("a_time", alarmTime);
                intent.putExtra("c_value", contentValue);
                startActivity(intent);

                finish();
//                String[] words = alarmTime.split(":");
//                if (Integer.parseInt(words[3]) < 10) words[3] = '0' + words[3];
//                if (Integer.parseInt(words[4]) < 10) words[4] = '0' + words[4];
//
//
//                String timeRegistered = words[3] + ":" + words[4] + "(" + words[2] + "일" + ")" + "알람";

//                if (contentValue.equals("")) {
//                    recordresult.setText(timeRegistered + "\n" + "내용 없음");
//                } else {
//                    recordresult.setText(timeRegistered + "\n" + recordCutValue(contentValue, 2));
//                }
//
//                db.insert(fileName, alarmTime, returnedValue);
//                Toast.makeText(getApplicationContext(), returnedValue, Toast.LENGTH_LONG).show();
//
//                ///////////////////////////////알람 설정 //////////////////////////////////////////
//                ///////////////////////알람 설정//////////////////////////
//                System.out.println("알람 시간 형식 : " + alarmTime);
//                //SharedPreferences 사용해서 누적된 알람의 개수 저장
//                SharedPreferences alarmNumPref = getSharedPreferences("anPref", MODE_PRIVATE);
//                int alarmNum = alarmNumPref.getInt("anum", 0); //anum에 해당하는 값이 없으면 0을 받아온다.
//                SharedPreferences.Editor editor = alarmNumPref.edit();
//                editor.putInt("anum", alarmNum + 1); // 값 수정.
//                editor.commit();
//
//                //SharedPreferences 사용해서 파일 이름에 해당하는 펜딩인텐트 request code 저장
//                SharedPreferences pIntentPref = getSharedPreferences("piPref", MODE_PRIVATE);
//                SharedPreferences.Editor piEditor = pIntentPref.edit();
//                piEditor.putInt(fileName, alarmNum);
//                piEditor.commit();
//                System.out.println("piPref put " + alarmNum);
//
//                int rCode = alarmNum;
//
//                Calendar mCalendar = Calendar.getInstance();
//                int yy, MM, dd, hh, mm;
//                yy = 2000 + Integer.parseInt(words[0]);
//                MM = Integer.parseInt(words[1]);
//                dd = Integer.parseInt(words[2]);
//                hh = Integer.parseInt(words[3]);
//                mm = Integer.parseInt(words[4]);
//                mCalendar.set(yy, MM - 1, dd, hh, mm, 0);
//
//                Intent mAlarmIntent = new Intent("com.google.cloud.android.reminderapp.ALARM_START");
//                mAlarmIntent.putExtra("filename", fileName);
////                        mAlarmIntent.putExtra("OBJECT", voicePlayer);
//                PendingIntent mPendingIntent =
//                        PendingIntent.getBroadcast(
//                                getApplicationContext(),
//                                rCode, /*request code*/
//                                mAlarmIntent,
//                                PendingIntent.FLAG_UPDATE_CURRENT
//                        );
//
//                AlarmManager mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//                mAlarmManager.setExact(
//                        AlarmManager.RTC_WAKEUP,
//                        mCalendar.getTimeInMillis(),
//                        mPendingIntent
//                );

            }
        }
    }
}
