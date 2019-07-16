package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;

import java.util.ArrayList;

public class SelectWriteDiaryActivity extends AppCompatActivity {

    private ImageView backImage; // 뒤로가기

    private RecyclerView recyclerView; // 리사이클러 뷰
    private RecyclerView.Adapter adapter; // 리사이클러 뷰 어댑터
    private RecyclerView.LayoutManager layoutManager; // 리사이클러 뷰 레이아웃 매니저

    private String useremail; // 사용자의 이메일
    private ArrayList<DiaryBook> userDiaryBooks; // 사용자의 일기책 목록
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_write_diary);

        // 사용자의 일기책 데이터 얻기
        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail"); // 사용자의 이메일
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        userDiaryBooks = diaryBookSharedPreference.activatedUserDiaryBook(useremail); // 유저의 일기책 목록 가져오기

        // 리사이클러 뷰
        recyclerView = findViewById(R.id.activity_select_write_diary_recyclerview);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new SelectWriteDiaryAdapter(this, useremail, userDiaryBooks);
        recyclerView.setAdapter(adapter);

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_select_write_diary_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
