package com.google.cloud.android.reminderapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
    }

    @Override
    protected void onStart() {
        super.onStart();

        int playCount = db.getAllPlayListNum();
        String pCntStr = "" + playCount;
        countList.setText(pCntStr);

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
}
