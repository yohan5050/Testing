package com.google.cloud.android.reminderapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
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
    PlaylistAdapter adapter;

    int tempPos = -1, tempPos2 = -1;
    boolean nowStarted = false;
    boolean isAfterOnPause = false; //onPause 상태였다가 onStart하는 것인지 아닌지 체크하기 위함.
    int playingPos = -1;
    public static Handler phandler; // 재생중인 리스트 처리 핸들러

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_list);

        PLactivity = this; //재생이 모두 끝나면 list화면도 같이 종료하는 데에 사용됨.
        listView = (ListView) findViewById(R.id.listView);
        db = Main2Activity.getDBInstance();

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
                    //같은 position이 여러번 들어오면 한 번만 색깔을 바꾸도록 하기 위함.
                    if (tempPos != position) {
                        tempPos = position;
                    } else {
                        return;
                    }

                    adapter.notifyDataSetChanged(); //adapter 내용 변경 - 리스트뷰 갱신 ***

                }
            }
        };

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
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

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
        //onPause상태에 들어왔다는 것을 체크
        isAfterOnPause = true;
        playingPos = Main2Activity.mVoicePlayer.stopPlaying();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        //PlayActivity 호출 -> back button을 눌렀을 때는 intent에 -1을 보내서 처음부터 재생이 되도록 한다.
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra("playcount", -1);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //PlayActivity위의 activity 모두 삭제
        startActivity(intent);

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
                adapter.addItem(new Playlist((i + 1) + ". " + "내용 없음"));
            } else {
                adapter.addItem(new Playlist((i + 1) + ". " + contentTime(contentNameArr[i])));
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
            view.setName(item.getName());

            if (position == tempPos2) {
                view.setBackgroundColor(Color.GREEN);
            } else {
                view.textView.setBackgroundColor(Color.WHITE);
            }

            return view;
        }
    }

    // make list2에서 사용 (컨텐츠 명으로 나타내기 위해서)
    public String contentTime(String contentName) {
        contentName = contentName.replaceAll(" ", "");
        if (contentName.length() > 8) return contentName.substring(0, 8) + ".. ";
        else return contentName;
    }
}
