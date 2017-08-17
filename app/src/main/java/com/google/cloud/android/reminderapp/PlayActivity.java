package com.google.cloud.android.reminderapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import com.google.cloud.android.reminderapp.R;

public class PlayActivity extends AppCompatActivity {

//    VoicePlayer mVoicePlayer;
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

//    PlaylistAdapter adapter;
//    PlaylistView viewArr[] = new PlaylistView[200]; //list의 각 아이템들의 view값을 담고 있다. 일단 최대 200개로 해보자.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        db = Main2Activity.getDBInstance();
//        mVoicePlayer = new VoicePlayer
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
                        //mText.setText("<일반 메모>\n" + words[1]);
                        textView.setText(playCutValue(words[1].replaceAll(" ", "")));
                    } else {
                        /*
                        if (Integer.parseInt(words[3]) < 10) words[3] = '0' + words[3];
                        if (Integer.parseInt(words[4]) < 10) words[4] = '0' + words[4];

                        String timeRegistered = words[3] + ":" + words[4] + "(" + words[1] + "월" + words[2] + "일" + ")";
                        System.out.println("재성(vhandler) " + timeRegistered);
                        //mText.setText(timeRegistered +"\n" + words[5]);
                        */
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
        System.out.println("on start 시작 : " + playCount);
        if(playCount == -2) playCount = intent.getIntExtra("playcount", -1);
        System.out.println("on start 시작2 : " + playCount);
        if(playCount == -1) playCount = db.getAllPlayListNum();

//        playCount = db.getAllPlayListNum();
        //가장 최근 녹음 파일부터 재생 시작
        //플레이리스트 업데이트
//        makeList2();
        if (playCount == 0) {
            Toast.makeText(getApplicationContext(), "재생할 목록이 비어있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Main2Activity.mVoicePlayer.isPlaying()) {
//            textView.setText("재생 중"); // -> 재생 중 대신 재생하는 파일의 텍스트 필요. -> 일단은 재생중으로 해볼까..
            Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playCount);
            playCount = -2;
            //TODO 모든 파일의 재생이 완료된 후, 시작 화면으로 전환되도록 개선 필요
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        playCount = playingPos + 1;
        //listBtn을 클릭해서 list화면으로 넘어가는 경우 playCount를 -2로 초기화 하고, listBtnClicked를 false로 초기화
        System.out.println("list 버튼 클릭됐나? " + listBtnClicked);
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

    /**
     * 재생 목록을 만드는 메소드, 재생 목록에는 각 컨텐츠들이 보여진다.
     */
//    public void makeList2() {
//        adapter = new PlaylistAdapter();
//        ContentAnalysis contentAnalysis = new ContentAnalysis();
//
//        String[] contentNameArr = db.getAllContent();
//        String[] alarmTimeArr = db.getAllAlarmTime();
//        String[] fileNameArr = db.getAllFileName();
//        playCount = contentNameArr.length;
//
//        System.out.println("Play Count : " + playCount);
//        for (int i = playCount - 1; i >= 0; i--) {
//            //각 녹음 파일의 일정 내용을 목록에 출력하는 코드.
//            contentNameArr[i] = contentAnalysis.Analysis(contentNameArr[i]);
//
//            if (contentNameArr[i].equals("")) {
//                adapter.addItem(new Playlist((i + 1) + ". " + "내용 없음"));
//            } else {
//                adapter.addItem(new Playlist((i + 1) + ". " + contentTime(contentNameArr[i])));
//            }
//        }
//    }

//    // 목록을 관리해주는 adapter
//    class PlaylistAdapter extends BaseAdapter {
//        ArrayList<Playlist> items = new ArrayList<Playlist>();
//
//        @Override
//        public int getCount() {
//            return items.size();
//        }
//
//        public void addItem(Playlist item) {
//            items.add(item);
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return items.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup viewGroup) {
//            PlaylistView view = new PlaylistView(getApplicationContext());
//            Playlist item = items.get(position);
//            view.setName(item.getName());
//
//            if (position == tempPos2) {
//                view.setBackgroundColor(Color.YELLOW);
//            } else {
//                view.setBackgroundColor(Color.BLACK);
//            }
//            viewArr[position] = view;
//
//            return view;
//        }
//    }

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
}
