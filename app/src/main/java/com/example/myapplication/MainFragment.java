package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.User;

import java.util.ArrayList;


public class MainFragment extends Fragment {

    private static final int WATCH_DIARY = 11;
    private static final String TAG = "MainFragment";
    private ImageView searchImage; // 일기책 검색하러가기

    private RecyclerView recyclerView; // 리사이클러 뷰
    private RecyclerView.Adapter adapter; // 리사이클러뷰 어댑터
    private RecyclerView.LayoutManager layoutManager; // 리사이클러뷰 레이아웃매니저

    private LoginUserSharedPreference loginUserSharedPreference; // 로그인한 유저 데이터 관리
    private User loginUser; // 로그인한 유저
    private String useremail; // 사용자 이메일
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private ArrayList<DiaryBook> allDiaryBooks; // 모든 일기책

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main2, container, false);

        loginUserSharedPreference = new LoginUserSharedPreference(getContext().getSharedPreferences("login", Context.MODE_PRIVATE));
        loginUser = loginUserSharedPreference.getLoginUser();
        Bundle receivedBundle = getArguments();
        useremail = loginUser.getEmail(); // 사용자 이메일 얻기
        diaryBookSharedPreference = new DiaryBookSharedPreference(getContext().getSharedPreferences("diarybooks", Context.MODE_PRIVATE));
        allDiaryBooks = diaryBookSharedPreference.loadOpenAllDiaryBook(); // 모든 일기책 데이터 얻기

        // 리사이클러뷰 설정
        recyclerView = view.findViewById(R.id.fragment_main_recyclerview);
        recyclerView.setHasFixedSize(true);

        // 리사이클러뷰 레이아웃 매니저 설정
        layoutManager = new GridLayoutManager(view.getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 어댑터 설정
        adapter = new MyAdapterTest2(view.getContext(), useremail, allDiaryBooks);
        recyclerView.setAdapter(adapter);

        // 검색하기
        searchImage = (ImageView) view.findViewById(R.id.fragment_main_search);
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), SearchActivity.class);
                intent.putExtra("useremail", useremail);
                startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
