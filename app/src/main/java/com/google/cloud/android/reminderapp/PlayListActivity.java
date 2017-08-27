package com.google.cloud.android.reminderapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.cloud.android.reminderapp.R;

import java.util.ArrayList;

public class PlayListActivity extends AppCompatActivity {

    public static Activity PLactivity; //PlayActivity에서 사용됨
    DataBase db;
    int SampleRate = 16000;
    int BufferSize = 1024;

    //listing
    ListView listView;
    TextView textView;
    PlaylistAdapter adapter;
    ImageButton imageButton;

    int tempPos = -1, tempPos2 = -1;
    boolean nowStarted = false;
    boolean isAfterOnPause = false; //onPause 상태였다가 onStart하는 것인지 아닌지 체크하기 위함.
    boolean isBackPressed = false;
    int playingPos = -1;
    public static Handler phandler; // 재생중인 리스트 처리 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        PLactivity = this; //재생이 모두 끝나면 list화면도 같이 종료하는 데에 사용됨.
        imageButton = (ImageButton) findViewById(R.id.imageButton);
        listView = (ListView) findViewById(R.id.listView);
        textView = (TextView) findViewById(R.id.text);
        textView.setMovementMethod(new ScrollingMovementMethod());
        db = Main2Activity.getDBInstance();

        //재생 중인지 아닌지에 따라 재생/중지 버튼 표시
        if(Main2Activity.mVoicePlayer.mIsPlaying) {
            imageButton.setImageResource(R.drawable.stop_btn2);
        }
        else {
            imageButton.setImageResource(R.drawable.play_btn2);
        }

        phandler = new Handler() {
            public void handleMessage(Message msg) {
                //재생 중이면
                if (Main2Activity.mVoicePlayer.isPlaying()) {
                    int position = (int) msg.obj;
                    tempPos2 = position;
                    if(nowStarted) { //list버튼을 눌러 처음 list화면이 뜰 때,
                        if(tempPos2 > 4)
                            listView.setSelection(tempPos2-4); //하이라이트 된 부분이 가운데로 오는 위치(-4)로 이동해주기(처음에만...! 찾기 편하도록!)
                        nowStarted = false;
                    }

                    //같은 position이 여러번 들어오면 한번만 색깔을 바꾸도록(textView에는 한번만 출력) 하기 위함.
                    if (tempPos != position) {
                        tempPos = position;

                        String[] contentNameArr = db.getAllContent();
                        int cnt = contentNameArr.length;
                        textView.setText(contentNameArr[cnt -1 - position]);
                    }
                    else {
                        return;
                    }

                    adapter.notifyDataSetChanged(); //adapter 내용 변경 - 리스트뷰 갱신 ***
                }
            }
        };

        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Main2Activity.mVoicePlayer.mIsPlaying) { //재생 중
                    playingPos = Main2Activity.mVoicePlayer.stopPlaying();
                    imageButton.setImageResource(R.drawable.play_btn2);
                }
                else {
                    Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playingPos + 1);
                    imageButton.setImageResource(R.drawable.stop_btn2);
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Main2Activity.mVoicePlayer.stopPlaying();
                int playCount = db.getAllPlayListNum();

                //PlayActivity 호출하면 될 듯...
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                intent.putExtra("playcount", playCount - position);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //PlayActivity위의 activity 모두 삭제
                System.out.println("stopPoint in PlayListActivity : " + (playCount - position));
                startActivity(intent);

                finish();
//                //목록에서 재생화면으로 넘어갈 때 하이라이트 잔상이 남길래... test... 나중에 생각하자.
//                try {
//                    System.out.println("피니시를 해도 여기로 올 수 있을까?");
//                    Thread.sleep(500);
//                } catch(InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(!isAfterOnPause && !Main2Activity.mVoicePlayer.mIsPlaying) { //재생 중지 상태의 playActivity에서 넘어왔을 경우
            String[] contentNameArr = db.getAllContent();
            int playCount = db.getAllPlayListNum();
            Intent intent = getIntent();
            playingPos = intent.getIntExtra("playingpos", 0);
            //목록에 highlight
            tempPos2 = playCount - playingPos - 1;
            //textView에 내용 출력
            textView.setText(contentNameArr[playingPos]);
        }

        makeList2();
        listView.setAdapter(adapter);
        nowStarted = true;

        //onPause상태에 있었다가 onStart된다면 -> 홈버튼을 눌러서 onPause가 되었다가 다시 실행시킨 경우.
        //backButton이나, 리스트의 파일을 선택하는 경우는 finish()되므로, 이 경우는 홈버튼을 눌렀다가 다시 실행한 경우이다.
        if(isAfterOnPause) {
            System.out.println("여기에 들어옵니까? onstart");
            Main2Activity.mVoicePlayer.startPlaying(SampleRate, BufferSize, playingPos + 1);
            isAfterOnPause = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("백버튼 누르면 여기로 오나"); //finish()를 해도 이쪽으로 오는듯
        if(!isBackPressed) { //back button을 누른 경우는 제외. - back button을 누른 경우는 계속 진행되도록 할 것임
            //onPause상태에 들어왔다는 것을 체크
            isAfterOnPause = true;
            playingPos = Main2Activity.mVoicePlayer.stopPlaying();
        }

        isBackPressed = false;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed() 전에 setResult()를 해줘야 resultCode가 전달됨!!
        Intent intent = new Intent();
        intent.putExtra("key", playingPos);
        setResult(-100, intent);

        System.out.println("여기에는 onback!!");

        isBackPressed = true;
        super.onBackPressed();

        //PlayActivity 호출 -> back button을 눌렀을 때는 intent에 -1을 보내서 처음부터 재생이 되도록 한다.
        //PlayActivity 호출 -> back button을 눌렀을 때는 intent에 -100을 보내서 현재 진행중인 상태 그대로 이어나가도록 한다
//        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
//        intent.putExtra("playcount", -100);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //PlayActivity위의 activity 모두 삭제
//        startActivity(intent);

        finish();
    }

    /**
     * 재생 목록을 만드는 메소드, 재생 목록에는 각 컨텐츠들이 보여진다.
     */
    public void makeList2() {
        adapter = new PlaylistAdapter();
        ContentAnalysis contentAnalysis = new ContentAnalysis();

        String[] contentNameArr = db.getAllContent();
        String[] alarmTimeArr = db.getAllAlarmTime();
        String[] fileNameArr = db.getAllFileName();
        int playCount = contentNameArr.length;

        System.out.println("Play Count : " + playCount);
        for (int i = playCount - 1; i >= 0; i--) {
            //각 녹음 파일의 일정 내용을 목록에 출력하는 코드.
            contentNameArr[i] = contentAnalysis.Analysis(contentNameArr[i]);

            if (contentNameArr[i].equals("")) {
                adapter.addItem(new Playlist((i + 1) + ". " + "내용 없음", alarmTimeArr[i], R.drawable.alarm));
            } else {
                if(alarmTimeArr[i].equals("일반 메모"))
                    adapter.addItem(new Playlist((i + 1) + ". " + contentTime(contentNameArr[i]), "알람정보 없음", R.drawable.memo));
                else
                    adapter.addItem(new Playlist((i + 1) + ". " + contentTime(contentNameArr[i]), timeFormatFunc(alarmTimeArr[i]), R.drawable.alarm));
            }
        }
    }

    // 목록을 관리해주는 adapter
    class PlaylistAdapter extends BaseAdapter {
        ArrayList<Playlist> items = new ArrayList<Playlist>();

        @Override
        public int getCount() {
            return items.size();
        }

        public void addItem(Playlist item) {
            items.add(item);
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            PlaylistView view = new PlaylistView(getApplicationContext());
            Playlist item = items.get(position);
            view.setContent(item.getContent());
            view.setAlarmTime(item.getAlarmTime());
            view.setImage(item.getResId());

            if (position == tempPos2) {
                view.setBackgroundColor(Color.rgb(92, 224, 189)); // #5CE0BD
            } else {
                view.textView.setBackgroundColor(Color.WHITE);
            }

            return view;
        }
    }

    // make list2에서 사용 (컨텐츠 명으로 나타내기 위해서)
    public String contentTime(String contentName) {
        contentName = contentName.replaceAll(" ", "");
        if (contentName.length() > 9) return contentName.substring(0, 9) + ".. ";
        else return contentName;
    }

    public String timeFormatFunc(String rawTime) {
        String words[] = rawTime.split(":");

        if(words[0].equals("일반 메모"))
            return rawTime;
        else {
            words[1] = Integer.parseInt(words[1]) < 10 ? "0" + words[1] : words[1];
            words[2] = Integer.parseInt(words[2]) < 10 ? "0" + words[2] : words[2];
            words[3] = Integer.parseInt(words[3]) < 10 ? "0" + words[3] : words[3];
            words[4] = Integer.parseInt(words[4]) < 10 ? "0" + words[4] : words[4];
            return words[1] + "-" + words[2] + " " + words[3] + ":" + words[4];
        }
    }
}
