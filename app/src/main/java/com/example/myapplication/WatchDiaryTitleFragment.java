package com.example.myapplication;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class WatchDiaryTitleFragment extends Fragment {

    TextView diaryBookTitle; // 일기책 표지 제목
    ImageView diaryBookCover; // 일기책 커버 이미지
    TextView diaryBookCreateDate; // 일기책 생성한 날짜
    ImageView outhorityUserImage; // 일기책 작성자 이미지
    ImageView outhorityUserImage2; // 일기책 작성자 이미지
    TextView outhorityUserName; // 일기책 작성자 필명
    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키 값
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private DiaryBook diaryBook; // 보여줄 일기책
    private UserSharedPreference userSharedPreference; // 유저 데이터 관리
    private ArrayList<String> outhorityUsersEmail; // 일기책 소유자들의 이메일
    private ArrayList<User> outhorityUsers; // 일기책 소유자의 데이터

    public WatchDiaryTitleFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_diary_title, container, false);

        // 일기책 데이터 얻기
        Bundle receivedBundle = getArguments();
        useremail = receivedBundle.getString("useremail"); // 사용자 이메일
        diarybookkey = receivedBundle.getString("diarybookkey"); // 일기책 키 값

        // 보여줄 일기책 데이터 가져오기
        diaryBookSharedPreference = new DiaryBookSharedPreference(getContext().getSharedPreferences("diarybooks", Context.MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);

        // 작성자 데이터 가져오기
        UserSharedPreference userSharedPreference = new UserSharedPreference(getContext().getSharedPreferences("users", Context.MODE_PRIVATE));
        outhorityUsersEmail = diaryBook.getOuthorityUsersEmail(); // 소유자 이메일을 받아옵니다
        outhorityUsers = new ArrayList<User>();

        // 소유자 데이터를 받아옵니다
        for (String outhorityUserEmail : outhorityUsersEmail) {
            outhorityUsers.add(userSharedPreference.findUserData(outhorityUserEmail));
        }

        diaryBookTitle = view.findViewById(R.id.fragment_watchdiarytitle_diarytitle); // 일기책 표지 제목
        diaryBookTitle.setText(diaryBook.getDiaryBookTitleVertical()); // 일기책 표지 제목 설정하기

        diaryBookCover = view.findViewById(R.id.fragment_watchdiarytitle_cover); // 일기책 커버 이미지
        Glide.with(this).load(diaryBook.getDiaryBookUri()).into(diaryBookCover); // 일기책 커버 이미지 설정하기

        diaryBookCreateDate = (TextView) view.findViewById(R.id.fragment_watchdiarytitle_date); // 일기책 생성한 날짜
        diaryBookCreateDate.setText(diaryBook.getCreateDate()); // 일기책 생성한 날짜 설정하기

        // 소유자의 프로필 이미지 설정
        outhorityUserImage = (ImageView) view.findViewById(R.id.fragment_watchdiarytitle_profileimage);
        outhorityUserImage2 = (ImageView) view.findViewById(R.id.fragment_watchdiarytitle_profileimage2);
        // 소유자의 필명 설정
        outhorityUserName = view.findViewById(R.id.fragment_watchdiarytitle_writer);

        // 혼자 쓰는 일기면 이미지 뷰가 안보이도록 만든다
        if (diaryBook.isAloneDIaryBook()) {
            // 혼자쓰는 일기일 때
            outhorityUserImage2.setVisibility(View.GONE);
            if (outhorityUsers.get(0).getUserUri() != null)
                Glide.with(this).load(outhorityUsers.get(0).getUserUri()).into(outhorityUserImage);
            outhorityUserName.setText(outhorityUsers.get(0).getUserName());
        } else {
            // 같이쓰는 일기일 때
            String outhorityUsersName = ""; // 여러명이랑 쓸 때
            for (int i = 0; i < outhorityUsers.size(); i++) {
                outhorityUsersName = outhorityUsersName + outhorityUsers.get(i).getUserName();
                if (i < outhorityUsers.size() - 1)
                    outhorityUsersName = outhorityUsersName + ", ";
            }
            Glide.with(this).load(outhorityUsers.get(1).getUserUri()).into(outhorityUserImage);
            Glide.with(this).load(outhorityUsers.get(0).getUserUri()).into(outhorityUserImage2);
            outhorityUserName.setText(outhorityUsersName);
        }

        return view;
    }

}
