package com.google.cloud.android.reminderapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class RecNoTimeActivity extends AppCompatActivity {
    String fileName, returnedValue;
    DataBase db;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rec_no_time);

        db = Main2Activity.getDBInstance();
        textView = (TextView) findViewById(R.id.text);

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //메인으로 돌아간다
                finish();
            }
        });

        //RecordActivity로부터 전달 받은 인텐트를 확인한다
        Intent intent = getIntent();
        fileName = intent.getStringExtra("f_name");
        returnedValue = intent.getStringExtra("r_value");
    }

    @Override
    protected void onStart() {
        super.onStart();

        db.insert(fileName, "일반 메모", returnedValue);
        System.out.println("db에 일반메모 저장");
//        textView.setText(recordCutValue(returnedValue.replaceAll(" ", ""), 1));
        textView.setText(returnedValue.replaceAll(" ", ""));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //녹음 후 화면에 표시해주는 것을 설정해준다.
    public String recordCutValue(String contentValue, int i) { //기기에 따라 화면의 한 줄에 들어갈 수 있는 글자 수가 다를 수 있고, 화면도 크기 때문에 사용 안 할 예정.
        String cutvalue = "";

        if (i == 1) {

            if (contentValue.length() > 27) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(8, 18) + "\n" + contentValue.substring(18, 27) + "..";
            }
            else if (contentValue.length() > 18) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "\n" + contentValue.substring(18, contentValue.length());
            }
            else if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        else if (i == 2) {

            //두줄 처리
            if (contentValue.length() > 18) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, 18) + "..";
            }
            else if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 9) + "\n" + contentValue.substring(9, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

            //한줄 처리일 경우

            if (contentValue.length() > 9) {
                cutvalue = contentValue.substring(0, 7) + "..";
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        return cutvalue;
    }
}
