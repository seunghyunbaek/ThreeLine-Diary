package com.example.myapplication;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;

import java.util.ArrayList;


public class SubscribeFragment extends Fragment {

    TextView subscribeDiaryCount; // 구독중인 일기책 수

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    SubscribeAdapter adapter;

    private String useremail; // 사용자 이메일
    private ArrayList<DiaryBook> subscribeDiaryBooks; // 구독중인 일기책
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscribe, container, false);

        // 사용자 이메일 얻기
        Bundle receivedBundle = getArguments();
        useremail = receivedBundle.getString("useremail");
        diaryBookSharedPreference = new DiaryBookSharedPreference(getContext().getSharedPreferences("diarybooks", Context.MODE_PRIVATE));
        subscribeDiaryBooks = diaryBookSharedPreference.userSubscribeDiaryBook(useremail); // 구독중인 일기책 데이터 얻기

        // 구독중인 일기책 수
        subscribeDiaryCount = view.findViewById(R.id.fragment_subscribe_diarycount);
        subscribeDiaryCount.setText(getSubscribeCount(subscribeDiaryBooks.size()));

        // 리사이클러뷰
        recyclerView = view.findViewById(R.id.fragment_subscribe_recyclerview);
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 레이아웃 매니저
        layoutManager = new GridLayoutManager(view.getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 어댑터
        adapter = new SubscribeAdapter(view.getContext(), useremail, subscribeDiaryBooks);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private String getSubscribeCount(int size) { // 구독중인 일기책 총 갯수 알려주기
        String subscribeCount = "총 " + String.valueOf(size) + "권";
        return subscribeCount;
    }

    @Override
    public void onStart() {
        super.onStart();
        subscribeDiaryBooks = diaryBookSharedPreference.userSubscribeDiaryBook(useremail); // 구독중인 일기책 데이터 받아오기
        subscribeDiaryCount.setText(getSubscribeCount(subscribeDiaryBooks.size())); // 총 구독중인 일기 수 설정하기
        adapter.setSubscribeDiaryBooks(subscribeDiaryBooks); // 변경된 데이터 알려주기
    }
}
