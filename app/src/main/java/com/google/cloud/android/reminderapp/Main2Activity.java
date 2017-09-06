package com.google.cloud.android.reminderapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tsengvn.typekit.TypekitContextWrapper;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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

//        //볼륨조절하기 볼륨 최대로 참조 : http://blog.naver.com/oh4zzang/40114444637
        //이것을 이용하면 처음 시작할 때마다 볼륨을 최대로 초기화해 주는 것 같음.
        //사용자가 볼륨을 조절할 수 있도록 이것은 주석처리.
//        AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audio.setStreamVolume(AudioManager.STREAM_MUSIC, (int)(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ), AudioManager.FLAG_PLAY_SOUND);

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
        //네트워크 연결 상태 체크 ** 중요 ** - 출처 : http://developer88.tistory.com/104
        //네트워크 연결 시에만 녹음 및 stt가능
        NetworkInfo mNetworkState = getNetworkInfo();
        //근데 이 경우, 무료 와이파이 존이 연결이 된 것으로 표시만 되고, 실제로 연결이 되지 않은 경우를 판별하지 못함.
        if(mNetworkState != null && mNetworkState.isConnected()) {
            Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
            startActivity(intent);
        }
        else {
            System.out.println("네트워크 연결 상태 확인 좀 해라");
            Toast.makeText(this, "네트워크 연결 상태를 확인해주세요.", Toast.LENGTH_LONG).show();
        }
    }

    public void onButtonPlayClicked(View v) {
//        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
//        startActivity(intent);
        if(db.getAllPlayListNum() > 0) {
            Intent intent = new Intent(getApplicationContext(), PlayListActivity.class);
            startActivity(intent);
        }
        else {
            Toast.makeText(this, "재생할 목록이 비어있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private NetworkInfo getNetworkInfo() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo;
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
