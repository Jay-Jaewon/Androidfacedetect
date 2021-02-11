package com.teamnova.jaycameraapp1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

//제일 처음 시작되는 액티비티. 사진촬영과 사진편집을 선택할 수 있다.
public class SelectActivity extends AppCompatActivity {

    private static final String TAG = "SelectActivity";

    private Button take_picture_btn;
    private Button edit_picture_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
        Log.d(TAG,"onCreate 실행");
        init();

    }

    public void init(){
        take_picture_btn = findViewById(R.id.select_take_picture_btn);
        edit_picture_btn = findViewById(R.id.select_edit_picture_btn);

        take_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭시 사진 촬영 액티비티 이동.
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        edit_picture_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //클릭시 사진 편집 액티비티 이동.
                Toast.makeText(SelectActivity.this,"편집", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), EditActivity.class));
            }
        });
    }
}
