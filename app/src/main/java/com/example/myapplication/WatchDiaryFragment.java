package com.example.myapplication;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.Diary;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;


/**
 * A simple {@link Fragment} subclass.
 */
public class WatchDiaryFragment extends Fragment {

    private static final String TAG = "WatchDiaryFragment";
    private int count = 0;
    private ImageView diaryContentImage; // 일기 이미지
    private TextView diaryContentText; // 일기 내용
    private TextView diaryContentDate; // 일기 작성 날짜
    private ImageView diaryContentHidden; // 일기 이미지
    private String useremail; // 사용자 이메일
    private int position; // 일기 인덱스
    private String diarybookkey; // 일기책 키 값
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private DiaryBook diaryBook; // 일기책 데이터
    private Diary diary; // 보여줄 일기

    public WatchDiaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_watch_diary, container, false);

        // 일기책 데이터 가져오기
        Bundle receivedBundle = getArguments();
        useremail = receivedBundle.getString("useremail");
        position = receivedBundle.getInt("position");
        diarybookkey = receivedBundle.getString("diarybookkey");
        diaryBookSharedPreference = new DiaryBookSharedPreference(getContext().getSharedPreferences("diarybooks", Context.MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기책 가져오기
        diary = diaryBook.getDiaries().get(position); // 보여줄 일기 가져오기

        // 일기 이미지 설정하기
        diaryContentImage = view.findViewById(R.id.fragment_watchdiary_content_image);
        if (diary.getDiaryUri() != null) { // 일기의 이미지가 있으면 설정해줍니다
            Glide.with(this).load(diary.getDiaryUri()).into(diaryContentImage);
        }

        // 일기 내용 설정하기
        diaryContentText = view.findViewById(R.id.fragment_watchdiary_content);
        diaryContentText.setText(diary.getContentText());

        // 일기 작성 날짜 설정하기
        diaryContentDate = view.findViewById(R.id.fragment_watchdiary_content_date);
        diaryContentDate.setText(diary.getCreateDate());

        // 숨은 일기 보기
        diaryContentHidden = view.findViewById(R.id.fragment_watchdiary_content_hidden);
        if (diary.isUserDiary(useremail)) {
            diaryContentHidden.setVisibility(View.VISIBLE);
            diaryContentHidden.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), WatchDiaryHiddenActivity.class);
                    intent.putExtra("hidden", diary.getHiddenContentText().toString());
                    startActivity(intent);
                }
            });
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);
        diary = diaryBook.getDiaries().get(position); // 보여줄 일기 가져오기

        // 일기 이미지 설정하기
        if (diary.getDiaryUri() != null) { // 일기의 이미지가 있으면 설정해줍니다
            Glide.with(this).load(diary.getDiaryUri()).into(diaryContentImage);
        }

        // 일기 내용 설정하기
        diaryContentText.setText(diary.getContentText());

        // 일기 작성 날짜 설정하기
        diaryContentDate.setText(diary.getCreateDate());

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    public void setDiary(Diary diary) {
        this.diary = diary;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
