package com.google.cloud.android.reminderapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tsengvn.typekit.TypekitContextWrapper;

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

    long bpTime = 0;
    Toast bpToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //볼륨조절하기 볼륨 최대로 참조 : http://blog.naver.com/oh4zzang/40114444637
        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ), AudioManager.FLAG_PLAY_SOUND);

        db = new DataBase(Main2Activity.this);
        mVoiceRecorder = new VoiceRecorder(this, mVoiceCallback);
        mVoicePlayer = new VoicePlayer(this);
//        record = (Button) findViewById(R.id.record);
//        play = (Button) findViewById(R.id.play);
        countList = (Button) findViewById(R.id.countlist);
        bpToast = Toast.makeText(this, "뒤로가기를 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT);
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
        //참고 : http://best421.tistory.com/71
        //이렇게 해도 cancel이 안되는 이유는 onBackPressed()에 들어올 때마다 toast가 새로 생성되기 때문에
        //결국 cancel하는 toast는 다른 값이 된다. -> 전역변수로 설정하자.
//        Toast toast = Toast.makeText(this, "뒤로가기를 한번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT);
        if(bpTime == 0) {
            System.out.println("토스트1 : "+ bpToast);
            bpToast.show();
            bpTime = System.currentTimeMillis();
        }
        else {
            long sec = System.currentTimeMillis() - bpTime;

            if(sec > 2000) {
                System.out.println("토스트2 : " + bpToast);
                bpToast.show();
                bpTime = System.currentTimeMillis();
            }
            else {
                bpToast.setText("                           종료합니다.                           ");
                //앱 종료하면서 toast도 없애주기
                bpToast.cancel();
                System.out.println("토스트1 : "+ bpToast + " , 토스트2 : " + bpToast);
                super.onBackPressed();
                finish();
            }
        }
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

    /**
     * 액티비티의 글꼴을 바꾸기 위해 불러지는 함수이다.
     * CustomStartApp과 연결되어 있다.
     */

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
