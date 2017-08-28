package com.google.cloud.android.reminderapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.android.reminderapp.R;

import static com.google.cloud.android.reminderapp.R.id.center;
import static com.google.cloud.android.reminderapp.R.id.center_horizontal;

public class PlayActivity extends AppCompatActivity {

    DataBase db;
    ImageButton button, backwardsBtn, forwardBtn;
    ImageButton listBtn, infoBtn, delBtn;
    int playCount = -2;
    TextView textView;

    int SampleRate = 16000;
    int BufferSize = 1024;
    int playingPos = -3; //onPause에서 +1하면 -2가 되도록
    boolean listBtnClicked = false, delBtnClicked = false;
    boolean wasPlaying = true; //onPause전에 재생 중이었는지 아닌지를 체크
    AlertDialog alertDialog;
    int resCode = -1;

    public static Handler vhandler; // 재생중 화면 처리 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        db = Main2Activity.getDBInstance();
        button = (ImageButton) findViewById(R.id.button);
        listBtn = (ImageButton) findViewById(R.id.listImage);
        infoBtn = (ImageButton) findViewById(R.id.informationImage);
        delBtn = (ImageButton) findViewById(R.id.deleteImage);
        textView = (TextView) findViewById(R.id.text);

        backwardsBtn = (ImageButton) findViewById(R.id.backwards_btn);
        forwardBtn = (ImageButton) findViewById(R.id.forward_btn);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Main2Activity.mVoicePlayer.mIsPlaying) {
                    //재생을 중지한다.
                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();
                    button.setImageResource(R.drawable.play_btn2);
                }
                else {
                    System.out.println("왜 재생이 안되니??" + playingPos);
                    Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playingPos + 1);
                    button.setImageResource(R.drawable.stop_btn2);
                }
//                //메인 화면으로 돌아간다
//                finish();
            }
        });

        backwardsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Main2Activity.mVoicePlayer.mIsPlaying) { //재생 중인 경우
                    //현재 재생 중인 파일 재생 중지 후
                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();

                    if(playingPos == db.getAllPlayListNum() - 1) { //맨 앞인 경우
                        button.setImageResource(R.drawable.play_btn2);
                        Toast.makeText(getApplicationContext(), "이전 재생 파일이 없습니다", Toast.LENGTH_LONG).show();
                    }
                    else {
                        try {
                            Thread.sleep(500);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }

                        //직전 파일 재생
                        Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, (playingPos + 1) + 1);

                        //TextView 변경
                        String returnedValue[] = db.getAllContent();
                        textView.setText(returnedValue[playingPos + 1].replaceAll(" ", ""));
                    }
                }
                else { //재생 중이지 않은 경우
                    if(playingPos == db.getAllPlayListNum() - 1) { //맨 앞인 경우
                        Toast.makeText(getApplicationContext(), "이전 재생 파일이 없습니다", Toast.LENGTH_LONG).show();
                    }
                    else {
                        System.out.println("여기에 안들어오노? backwardsbtn");
                        //TextView 변경
                        String returnedValue[] = db.getAllContent();
                        textView.setText(returnedValue[playingPos + 1].replaceAll(" ", ""));
                        playingPos++; //***
                    }
                }
            }
        });

        forwardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Main2Activity.mVoicePlayer.mIsPlaying) { //재생 중인 경우
                    //현재 재생 중인 파일 재생 중지 후
                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();

                    if(playingPos == 0) { //맨 뒤인 경우
                        button.setImageResource(R.drawable.play_btn2);
                        Toast.makeText(getApplicationContext(), "다음 재생 파일이 없습니다", Toast.LENGTH_LONG).show();
                    }
                    else {
                        try {
                            Thread.sleep(500);
                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }

                        //다음 파일 재생
                        Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, (playingPos - 1) + 1);

                        //TextView 변경
                        String returnedValue[] = db.getAllContent();
                        textView.setText(returnedValue[playingPos - 1].replaceAll(" ", ""));
                    }
                }
                else { //재생 중이지 않은 경우
                    if(playingPos == 0) { //맨 뒤인 경우
                        Toast.makeText(getApplicationContext(), "다음 재생 파일이 없습니다", Toast.LENGTH_LONG).show();
                    }
                    else {
                        System.out.println("여기에 안들어오노? forwardbtn");
                        //TextView 변경
                        String returnedValue[] = db.getAllContent();
                        textView.setText(returnedValue[playingPos - 1].replaceAll(" ", ""));
                        playingPos--;  //***
                    }
                }
            }
        });

        listBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listBtnClicked = true;
                //PlayListActivity 호출
                Intent intent = new Intent(getApplicationContext(), PlayListActivity.class);
                intent.putExtra("playingpos", playingPos);
                //알람 액티비티가 뜨는데 시간이 좀 걸리는데, 그 직전에 리스트 버튼을 클릭하면 onUserLeaveHint()에 들어가서
                //알람이 자동으로 종료되는 것 같다 그래서 리스트 버튼을 클릭하고 잠시 멈춰있다가 리스트 액티비티를 띄워본다.
                //다시 생각해보니 잘 이해는 안 되지만 이렇게 해줌으로써 문제가 해결된 듯하다.
                //일단 이렇게 해보고, 알람 액티비티를 좀 빠르게 띄우거나 alarmsoundservice랑 합치는 방법도 고려해보자.
                try {
                    Thread.sleep(90);
                } catch(InterruptedException e) {
                    e.printStackTrace();
                }
                startActivityForResult(intent, 100);
            }
        });

        infoBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Main2Activity.mVoicePlayer.mIsPlaying) {
                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();
                    button.setImageResource(R.drawable.play_btn2);
                }
                //PlayInfoActivity 호출
                Intent intent = new Intent(getApplicationContext(), PlayInfoActivity.class);
                intent.putExtra("playingpos", playingPos);
                startActivityForResult(intent, 99);
            }
        });

        delBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                delBtnClicked = true;
                if(Main2Activity.mVoicePlayer.mIsPlaying)
                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();

                button.setImageResource(R.drawable.play_btn2);

                //참조1 : http://mainia.tistory.com/2017
                //참조2 : http://pluu.github.io/blog/rxjava/2017/02/04/android-alertdialog/
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(PlayActivity.this, R.style.MyAlertDialogStyle);

                // Title setting
//                alertDialogBuilder.setTitle("음성 파일 삭제");
//                alertDialogBuilder.setTitle("삭제할까요?");

                // AlertDialog setting
                alertDialogBuilder
                        .setMessage("삭제할까요?")
                        .setCancelable(false)
                        .setPositiveButton("삭제",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        delBtnClicked = false;
                                        //해당파일을 삭제한다.
                                        delFunction();
//                                        //다시 재생 시작
//                                        onPause();
//                                        onResume();
                                    }
                                })
                        .setNegativeButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        delBtnClicked = false;
                                        //dialog를 취소
                                        dialog.cancel();
//                                        //다시 재생 시작
//                                        onPause();
//                                        onResume();
                                    }
                                });

                // Dialog 생성
                alertDialog = alertDialogBuilder.create();

                //Dialog 위치 이동시키기
//                alertDialog.getWindow().setGravity(Gravity.BOTTOM);

                // show Dialog
                alertDialog.show();

                // 메시지 택스트의 크기를 변경한다.
                TextView textView = (TextView) alertDialog.findViewById(android.R.id.message);
                textView.setTextSize(40.0f);
                // 메시지를 가운데 정렬한다.
                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                //각 버튼을 얻는다.
                Button button2 = (Button) alertDialog.findViewById(android.R.id.button2);
                Button button1 = (Button) alertDialog.findViewById(android.R.id.button1);
                //각 버튼의 비율을 지정한다.
                ((LinearLayout.LayoutParams)button2.getLayoutParams()).weight = 100.0f;
                ((LinearLayout.LayoutParams)button1.getLayoutParams()).weight = 100.0f;
                //각 버튼의 폭을 0으로 한다
                ((LinearLayout.LayoutParams)button2.getLayoutParams()).width = 0;
                ((LinearLayout.LayoutParams)button1.getLayoutParams()).width = 0;

                //버튼의 크기 변경 / 이렇게 변경하지 않고, styles.xml과 dimens.xml에서 했음
//                Button btn = (Button) alertDialog.findViewById(android.R.id.button1);
//                btn.setTextSize(30.0f);
//
//                Button btn2 = (Button) alertDialog.findViewById(android.R.id.button2);
//                btn2.setTextSize(30.0f);
            }
        });

        vhandler = new Handler() {
            public void handleMessage(Message msg) {
                if (((String) msg.obj).equals("stop")) {
                    //list를 보고있는 중에 메인화면으로 돌아가기 위해서 PlayListActivity도 함께 종료
                    if(PlayListActivity.PLactivity != null) //list를 한 번도 실행하지 않은 경우
                        PlayListActivity.PLactivity.finish();
                    finish();
                } else if (Main2Activity.mVoicePlayer.isPlaying()) {
                    String alarmTime = (String) msg.obj;
                    String[] words = alarmTime.split(":");

                    if (words[0].equals("일반 메모")) {
//                        textView.setText(playCutValue(words[1].replaceAll(" ", "")));
                        textView.setText(words[1].replaceAll(" ", ""));
                    } else {
//                        textView.setText(playCutValue(words[5].replaceAll(" ", "")));
                        textView.setText(words[5].replaceAll(" ", ""));
                    }
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();

//        //재생 중인지 아닌지에 따라 재생/중지 버튼 표시 //-> onResume이 onStart뒤에 실행될 경우, onStart에서 재생 여부를 알 수 없다.
//        if(Main2Activity.mVoicePlayer.mIsPlaying) {
//            button.setImageResource(R.drawable.stop_btn2);
//        }
//        else {
//            button.setImageResource(R.drawable.play_btn2);
//        }
    }

    //onActivityResult는 항상 onResume()전에 호출된다!
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        resCode = resultCode;
        playingPos = data.getIntExtra("key", db.getAllPlayListNum()); //list에서 재생 중이었거나, 정지했던 위치

        //list에서 back button으로 play화면으로 돌아올 때, 재생 중인지 아닌지에 따라 재생/중지 버튼 표시
        if(Main2Activity.mVoicePlayer.mIsPlaying) {
            System.out.println("재생중이냐? " + Main2Activity.mVoicePlayer.mIsPlaying);
            button.setImageResource(R.drawable.stop_btn2);
        }
        else {
            button.setImageResource(R.drawable.play_btn2);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        //삭제여부 다이얼로그  창이 띄워져있을 때는 재생을 하면 안된다.
        if(delBtnClicked) {
            return;
        }

        //list 화면에서 back button을 눌러 play화면으로 왔다면 그냥 그대로 놔둔다.
        if(resCode == -100) {
            resCode = -1; //초기화
            return;
        }

        //info 화면에서 back button을 눌러 play화면으로 왔다면 그냥 그대로 놔둔다.
        if(resCode == -99) {
            resCode = -1; //초기화
            return;
        }

        if(!wasPlaying) { //onPause상태가 되기 전에 재생 중이지 않았다면
            return;
        }

        Intent intent = getIntent();
        //list에서 음성 파일을 선택한 경우 해당 위치를 전달 받는다.
        if (playCount == -2) playCount = intent.getIntExtra("playcount", -1);

        System.out.println("테스팅 mVoicePlayer : " + Main2Activity.mVoicePlayer);
        if(Main2Activity.mVoicePlayer == null) {
            Main2Activity.mVoicePlayer = new VoicePlayer(this);
        }
        if (Main2Activity.mVoicePlayer.mIsPlaying) { // 재생 중이라면 재생을 멈추고
            Main2Activity.mVoicePlayer.stopPlaying();
        }

        try { //재생이 완전히 종료될 때까지 좀 기다린다.
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //시작하는 지점 정보(int)가 없으면 db에서 모든파일의 개수 얻어와서 가장 최근 녹음파일부터 재생 시작
        if (playCount == -1) playCount = db.getAllPlayListNum();

        //가장 최근 녹음 파일부터 재생 시작
        //플레이리스트 업데이트
//        makeList2();
        if (db.getAllPlayListNum() == 0) { //if (playCount == 0) { //이렇게 할 경우 맨 마지막(맨 아래) 음성 파일을 삭제 후 이쪽으로 들어온다.
            Toast.makeText(getApplicationContext(), "재생할 목록이 비어있습니다.", Toast.LENGTH_SHORT).show();
            textView.setText("재생할 파일이 없습니다.");
            finish();
            return;
        }
        System.out.println("삭제 취소하면 여기로 1");
        if (!Main2Activity.mVoicePlayer.isPlaying()) {
            Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playCount);
            System.out.println("삭제 취소하면 여기로 2 와서 재생돼야 하지 않음?");
            playCount = -2;
        }

                //재생 중인지 아닌지에 따라 재생/중지 버튼 표시 //-> onResume이 onStart뒤에 실행될 경우, onStart에서 재생 여부를 알 수 없다.
        if(Main2Activity.mVoicePlayer.mIsPlaying) {
            button.setImageResource(R.drawable.stop_btn2);
        }
        else {
            button.setImageResource(R.drawable.play_btn2);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(Main2Activity.mVoicePlayer.mIsPlaying) {
            wasPlaying = true; //onPause전에 재생 중이었는지 아닌지를 체크
        }
        else {
            wasPlaying = false;
        }

        playCount = playingPos + 1;
        //listBtn을 클릭해서 list화면으로 넘어가는 경우 playCount를 -2로 초기화 하고, listBtnClicked를 false로 초기화
        System.out.println("onPause in PlayActivity");
        if(listBtnClicked) {
            playCount = -2;
            listBtnClicked = false;
        }
        else { //listBtn을 클릭하지 않은 경우의 화면 전환에서는 재생을 멈춰준다.
            //mIsPlaying을 체크하지 않으면, 다이얼로그를 띄웠다가 삭제한 후에 playCount값(playingPos값)이 변경되는 문제가 생긴다.
            //arrayoutofbound exception 생겼었음.
//            if(Main2Activity.mVoicePlayer.mIsPlaying)
//                playCount = Main2Activity.mVoicePlayer.stopPlaying() + 1;
            if(!delBtnClicked) {
                //알람 화면에서 전환되어 onPause에 들어올 경우, 재생 중이지 않더라도 stopPlaying 메소드를 통해 playingPos값을 얻어야 한다...
                playingPos = Main2Activity.mVoicePlayer.stopPlaying();
                playCount = playingPos + 1;
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(Main2Activity.mVoicePlayer.mIsPlaying) {
            Main2Activity.mVoicePlayer.stopPlaying();
        }
        if(delBtnClicked) { //삭제 여부 확인 다이얼로그가 띄워져 있다면 dialog도 종료시킨다.
            alertDialog.cancel();
        }
        finish();
    }

    // make list2에서 사용 (컨텐츠 명으로 나타내기 위해서)
    public String contentTime(String contentName) {
        contentName = contentName.replaceAll(" ", "");
        if (contentName.length() > 6) return contentName.substring(0, 6);
        else return contentName;
    }

    //재생중일 경우 화면에 표시해주는 것을 설정해준다.
    public String playCutValue(String contentValue) { //기기에 따라 화면의 한 줄에 들어갈 수 있는 글자 수가 다를 수 있고, 화면도 크기 때문에 사용 안 할 예정.
        String cutvalue = "";
        if (contentValue.length() > 27) {
            cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(8, 18) + "\n" + contentValue.substring(18, 27) + "..";
        } else if (contentValue.length() > 18) {
            cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "\n" + contentValue.substring(18, contentValue.length());
        } else if (contentValue.length() > 9) {
            cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
        } else {
            cutvalue = contentValue.substring(0, contentValue.length());
        }
        return cutvalue;
    }

    public void delFunction() {
        String fileNameArr[] = db.getAllFileName();
        String alarmTimeArr[] = db.getAllAlarmTime();

        //db에서 파일이름 삭제
        db.delete(fileNameArr[playingPos]);

        //내부 저장소의 음성파일 삭제
        Context context = getApplicationContext();
        context.deleteFile(fileNameArr[playingPos]);

        //파일 이름에 해당하는 알람이 있으면 취소////////////////////////////////////////////
        SharedPreferences tempPref = getSharedPreferences("piPref", MODE_PRIVATE);
        int rCode = tempPref.getInt(fileNameArr[playingPos], -1); //fileNameArr[playingPos]에 해당하는 값이 없으면 -1을 받아온다.
        System.out.println("알람삭제 : " + rCode);
        if(rCode != -1) {
            AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            Intent intent = new Intent("com.google.cloud.android.reminderapp.ALARM_START");
            PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), rCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            if (sender != null) {
                am.cancel(sender);
                sender.cancel();
            }
        }

        Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show();

        //삭제했으면, 다음 파일부터 재생을 해야 한다. playingPos -= 1
        playingPos--;
        //textView을 다음파일의 내용으로 세팅한다.
        String returnedValue[] = db.getAllContent();
        if(playingPos < 0) {
            finish();
        }
        else {
            textView.setText(returnedValue[playingPos].replaceAll(" ", ""));
        }
    }
}