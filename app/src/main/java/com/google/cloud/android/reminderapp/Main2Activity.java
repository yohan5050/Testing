package com.google.cloud.android.reminderapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Main2Activity extends AppCompatActivity {
    Button countList;
//    Button record;
//    Button play;
    public static DataBase db;
    public static VoiceRecorder mVoiceRecorder;
    public static VoicePlayer mVoicePlayer;

    private SpeechService mSpeechService;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1; //추가
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        db = new DataBase(Main2Activity.this);
        mVoiceRecorder = new VoiceRecorder(this, mVoiceCallback);
        mVoicePlayer = new VoicePlayer(this);
//        record = (Button) findViewById(R.id.record);
//        play = (Button) findViewById(R.id.play);
        countList = (Button) findViewById(R.id.countlist);
//        countList.bringToFront();
//        countList.invalidate();

    }

    @Override
    protected void onStart() {
        super.onStart();

        int playCount = db.getAllPlayListNum();
        String pCntStr = "" + playCount;
        countList.setText(pCntStr);

//        // Prepare Cloud Speech API
//        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);
        // Start listening to voices
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.RECORD_AUDIO)) {
            showPermissionMessageDialog();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
//    /**
//     * MainActivity가 멈출 때, SpeechService를 종료한다.
//     */
//    @Override
//    protected void onStop() {
//        // Stop Cloud Speech API
//        mSpeechService.removeListener(mSpeechServiceListener);
//        unbindService(mServiceConnection);
//        mSpeechService = null;
//
//        super.onStop();
//    }

    public void onButtonRecordClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
        startActivity(intent);
    }

    public void onButtonPlayClicked(View v) {
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        startActivity(intent);
    }

    /**
     * Permission을 체크한다. Permission 되어있지 않을 경우 다이얼로그를 통해
     * 유저에게 이를 알린다.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startVoiceRecorder();
//                stopVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

//    private final ServiceConnection mServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName componentName, IBinder binder) {
//            mSpeechService = SpeechService.from(binder);
//            mSpeechService.addListener(mSpeechServiceListener);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName componentName) {
//            mSpeechService = null;
//        }
//
//    };

    /**
     * 녹음과 관련된 Permission을 유저에게 확인받기 위해 다이얼로그를 띄운다.
     */
    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    /**
     * 다이얼로그가 무시되었을 경우 다시 다이얼로그를 띄운다.
     */
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    /**
     * DBInstance를 다른 Class에서도 사용할 수 있도록 하기 위해 사용한다.
     */
    public static DataBase getDBInstance() {
        return db;
    }

    // 완성 후 mVoiceCallback 지워도 상관없으면 지우기.
    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

    };

//    /**
//     * 구글 STT 서버로 부터 분석된 텍스트를 받아 onCreate에 있는 handler로 전송시킨다.
//     *
//     * @param text 파일의 음성을 텍스트로 변경한 값
//     * @param isFinal 구글 STT 서버로부터 텍스트를 받았는지 아닌지 확인하는 값
//     */
//    private final SpeechService.Listener mSpeechServiceListener =
//            new SpeechService.Listener() {
//                @Override
//                public void onSpeechRecognized(final String text, final boolean isFinal) {
//
//
////                    if (mText2 != null) {
////                        //if (isFinal) {
////                        Message message = handler.obtainMessage(1, text);
////                        handler.sendMessage(message);
////                        //}
////                    }
//                }
//            };
}
