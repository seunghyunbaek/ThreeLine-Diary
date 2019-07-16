package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.SearchView;

import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {

    private ImageView backImage; // 뒤로가기
    private SearchView searchView; // 검색하기

    private RecyclerView recyclerView; // 리사이클러뷰
    private RecyclerView.LayoutManager layoutManager; // 리사이클러뷰 레이아웃
    private SearchAdapter adapter; // 리사이클러뷰 어댑터

    private String useremail; // 사용자 이메일
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private ArrayList<DiaryBook> allDiaryBooks; // 모든 일기책

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail"); // 사용자 이메일 얻기
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        allDiaryBooks = diaryBookSharedPreference.loadOpenAllDiaryBook(); // 모든 일기책 데이터 얻기

        // 리사이클러뷰
        recyclerView = findViewById(R.id.activity_search_recyclerview);
        // 리사이클러뷰 레이아웃 매니저
        layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 어댑터
        adapter = new SearchAdapter(this, useremail, allDiaryBooks);
        recyclerView.setAdapter(adapter);

        searchView = (SearchView) findViewById(R.id.activity_search_searchview);
        searchView.setQueryHint("누구의 삶이 궁금한가요?");
        searchView.setActivated(true);
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
    }
}
