package com.google.cloud.android.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.android.reminderapp.R;

public class PlayActivity extends AppCompatActivity {

    DataBase db;
    Button button;
    ImageButton listBtn, infoBtn, delBtn;
    int playCount = -2;
    TextView textView;

    int SampleRate = 16000;
    int BufferSize = 1024;
    int playingPos = -3; //onPause에서 +1하면 -2가 되도록
    boolean listBtnClicked = false;

    public static Handler vhandler; // 재생중 화면 처리 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        db = Main2Activity.getDBInstance();
        button = (Button) findViewById(R.id.button);
        listBtn = (ImageButton) findViewById(R.id.listImage);
        infoBtn = (ImageButton) findViewById(R.id.informationImage);
        delBtn = (ImageButton) findViewById(R.id.deleteImage);
        textView = (TextView) findViewById(R.id.text);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Main2Activity.mVoicePlayer.stopPlaying();
                //메인 화면으로 돌아간다
                finish();
            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listBtnClicked = true;
                //PlayListActivity 호출
                Intent intent = new Intent(getApplicationContext(), PlayListActivity.class);
                startActivity(intent);
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playingPos = Main2Activity.mVoicePlayer.stopPlaying();
                //PlayInfoActivity 호출
                Intent intent = new Intent(getApplicationContext(), PlayInfoActivity.class);
                intent.putExtra("playingpos", playingPos);
                startActivity(intent);
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                playingPos = Main2Activity.mVoicePlayer.stopPlaying();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayActivity.this);

                // Title setting
                alertDialogBuilder.setTitle("음성 파일 삭제");

                // AlertDialog setting
                alertDialogBuilder
                        .setMessage("삭제할까요?")
                        .setCancelable(false)
                        .setPositiveButton("네",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //해당파일을 삭제한다.
                                        delFunction();
                                        //다시 재생 시작
                                        onPause();
                                        onStart();
                                    }
                                })
                        .setNegativeButton("아니오",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        //dialog를 취소하고 해당 파일부터 다시 재생한다
                                        dialog.cancel();
                                        //다시 재생 시작
                                        onPause();
                                        onStart();
                                    }
                                });

                // Dialog 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show Dialog
                alertDialog.show();
            }
        });

        vhandler = new Handler() {
            public void handleMessage(Message msg) {
                if (((String) msg.obj).equals("stop")) {
                    //list를 보고있는 중에 메인화면으로 돌아가기 위해서 PlayListActivity도 함께 종료
                    PlayListActivity.PLactivity.finish();
                    finish();
                } else if (Main2Activity.mVoicePlayer.isPlaying()) {
                    String alarmTime = (String) msg.obj;
                    String[] words = alarmTime.split(":");

                    if (words[0].equals("일반 메모")) {
                        textView.setText(playCutValue(words[1].replaceAll(" ", "")));
                    } else {
                        textView.setText(playCutValue(words[5].replaceAll(" ", "")));
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

        Intent intent = getIntent();
        //시작하는 지점 정보(int)가 없으면 db에서 모든파일의 개수 얻어와서 가장 최근 녹음파일부터 재생 시작
        if(playCount == -2) playCount = intent.getIntExtra("playcount", -1);
        if(playCount == -1) playCount = db.getAllPlayListNum();

        //가장 최근 녹음 파일부터 재생 시작
        //플레이리스트 업데이트
//        makeList2();
        if (playCount == 0) {
            Toast.makeText(getApplicationContext(), "재생할 목록이 비어있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Main2Activity.mVoicePlayer.isPlaying()) {
            Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playCount);
            playCount = -2;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        playCount = playingPos + 1;
        //listBtn을 클릭해서 list화면으로 넘어가는 경우 playCount를 -2로 초기화 하고, listBtnClicked를 false로 초기화
        System.out.println("onPause in PlayActivity");
        if(listBtnClicked) {
            playCount = -2;
            listBtnClicked = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Main2Activity.mVoicePlayer.stopPlaying();
        finish();
    }

    // make list2에서 사용 (컨텐츠 명으로 나타내기 위해서)
    public String contentTime(String contentName) {
        contentName = contentName.replaceAll(" ", "");
        if (contentName.length() > 6) return contentName.substring(0, 6);
        else return contentName;
    }

    //재생중일 경우 화면에 표시해주는 것을 설정해준다.
    public String playCutValue(String contentValue) {
        String cutvalue = "";
        if (contentValue.length() > 27) {
            cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(8, 18) + "\n" + contentValue.substring(18, 27) + "..";
        } else if (contentValue.length() > 16) {
            cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "\n" + contentValue.substring(18, contentValue.length());
        } else if (contentValue.length() > 8) {
            cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
        } else {
            cutvalue = contentValue.substring(0, contentValue.length());
        }
        return cutvalue;
    }

    public void delFunction() {
        String fileNameArr[] = db.getAllFileName();
        String alarmTimeArr[] = db.getAllAlarmTime();
        db.delete(fileNameArr[playingPos]);
        playingPos--;

        //파일 이름에 해당하는 알람이 있으면 취소////////////////////////////////////////////
        SharedPreferences tempPref = getSharedPreferences("piPref", MODE_PRIVATE);
        int rCode = tempPref.getInt(fileNameArr[playingPos], -1); //fileNameArr[playingPos]에 해당하는 값이 없으면 -1을 받아온다.
        if(rCode != -1) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("com.google.cloud.android.reminderapp.ALARM_START");
            PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), rCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (sender != null) {
                am.cancel(sender);
                sender.cancel();
            }
        }
    }
}
