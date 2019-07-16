package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;

import java.util.ArrayList;

public class FindFriendActivity extends AppCompatActivity {

    private static final int MAKE_JOIN_DIARY = 4; // 같이쓰는 일기 만들기

    private ImageView backImage; // 뒤로가기
    private ImageView nextImage; // 다음으로가기
    private SearchView searchView; // 검색하기

    private ImageView userImageView; // 사용자 프로필 이미지
    private TextView userNameView; // 사용자 필명


    private RecyclerView addFriendRecyclerview; // 친구추가 리사이클러뷰
    private AddFriendAdapter addFriendAdapter; // 친구추가 리사이클러뷰 어댑터
    private RecyclerView.LayoutManager addFriendLayoutManager; // 친구추가 리사이클러뷰 레이아웃매니저

    private RecyclerView findFriendRecyclerview; // 친구검색 리사이클러뷰
    private FindFriendAdapter findFriendAdapter; // 친구검색 리사이클러뷰 어댑터
    private RecyclerView.LayoutManager findFriendLayoutManager; // 친구검색 리사이클러뷰 레이아웃매니저

    private String useremail; // 사용자 이메일
    private UserSharedPreference userSharedPreference; // 유저 데이터 관리
    private User user; // 유저 데이터
    private ArrayList<User> allUserList; // 모든 유저 데이터

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail");
        userSharedPreference = new UserSharedPreference(getSharedPreferences("users", MODE_PRIVATE));
        user = userSharedPreference.findUserData(useremail);
        allUserList = userSharedPreference.loadAllUser();

        // 다음으로가기
        nextImage = (ImageView) findViewById(R.id.activiy_find_friend_next);
        nextImage.setVisibility(View.GONE);
        nextImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FindFriendActivity.this, NewDiaryBookActivity.class);
                intent.putExtra("useremail", useremail);
//                intent.putExtra("joinuseremail", addFriendAdapter.getJoinUserEmail()); // 한명이랑 쓸 때
                intent.putStringArrayListExtra("joinusersemail", addFriendAdapter.getJoinUsersEmail());
                startActivityForResult(intent, MAKE_JOIN_DIARY);
            }
        });

        // 친구추가 리사이클러뷰
        addFriendRecyclerview = findViewById(R.id.activity_find_friend_add_recyclerview);
        // 친구추가 리사이클러뷰 레이아웃매니저
        addFriendLayoutManager = new LinearLayoutManager(this, LinearLayout.HORIZONTAL, false);
        addFriendRecyclerview.setLayoutManager(addFriendLayoutManager);
        // 친구추가 리사이클러뷰 어댑터
        addFriendAdapter = new AddFriendAdapter(FindFriendActivity.this, user, useremail, nextImage);
        addFriendRecyclerview.setAdapter(addFriendAdapter);

        // 친구검색 리사이클러뷰
        findFriendRecyclerview = findViewById(R.id.activity_find_friend_recyclerview);
        // 친구검색 리사이클러뷰 레이아웃매니저
        findFriendLayoutManager = new LinearLayoutManager(this);
        findFriendRecyclerview.setLayoutManager(findFriendLayoutManager);
        // 친구검색 리사이클러뷰 어댑터
        findFriendAdapter = new FindFriendAdapter(FindFriendActivity.this, allUserList, addFriendAdapter);
        findFriendRecyclerview.setAdapter(findFriendAdapter);

        // 검색어 입력하기
        searchView = findViewById(R.id.activiy_find_friend_searchview);
//        searchView.setQueryHint("같이 쓰고 싶은 친구의 필명을 검색하세요");
        searchView.setQueryHint("친구의 필명을 검색하세요");
        searchView.setActivated(true);
        searchView.onActionViewExpanded();
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                findFriendAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });


        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activiy_find_friend_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == MAKE_JOIN_DIARY) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
