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
        textView.setText(recordCutValue(returnedValue.replaceAll(" ", ""), 1));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    //녹음 후 화면에 표시해주는 것을 설정해준다.
    public String recordCutValue(String contentValue, int i) {
        String cutvalue = "";

        if (i == 1) {

            if (contentValue.length() > 24) {
                cutvalue = contentValue.substring(0, 8) + "\n" + contentValue.substring(8, 16) + "\n" + contentValue.substring(16, 23) + "..";
            }
            else if (contentValue.length() > 16) {
                cutvalue = contentValue.substring(0, 8) + "\n" + contentValue.substring(8, 16) + "\n" + contentValue.substring(16, contentValue.length());
            }
            else if (contentValue.length() > 8) {
                cutvalue = contentValue.substring(0, 8) + "\n" + contentValue.substring(8, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        else if (i == 2) {

            //두줄 처리
            if (contentValue.length() > 16) {
                cutvalue = contentValue.substring(0, 8) + "\n" + contentValue.substring(8, 15) + "..";
            }
            else if (contentValue.length() > 8) {
                cutvalue = contentValue.substring(0, 8) + "\n" + contentValue.substring(8, contentValue.length());
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

            //한줄 처리일 경우

            if (contentValue.length() > 8) {
                cutvalue = contentValue.substring(0, 6) + "..";
            }
            else {
                cutvalue = contentValue.substring(0, contentValue.length());
            }

        }
        return cutvalue;
    }
}
