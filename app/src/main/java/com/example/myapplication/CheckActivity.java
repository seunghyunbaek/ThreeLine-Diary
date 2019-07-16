package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.myapplication.com.example.myapplication.data.AutoLoginSharedPreference;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.User;

public class CheckActivity extends AppCompatActivity {


    private AutoLoginSharedPreference autoLoginSharedPreference; // 자동 로그인 유저 데이터 관리
    private LoginUserSharedPreference loginUserSharedPreference; // 로그인한 유저 데이터 관리
    private User user;
    private String useremail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        autoLoginSharedPreference = new AutoLoginSharedPreference(getSharedPreferences("auto", MODE_PRIVATE));
        if (autoLoginSharedPreference.getData() != null) {
            // 자동로그인에 등록된 유저가 있을 경우 로그인한 유저 파일에 등록하고 메인화면으로 이동합니다
            user = autoLoginSharedPreference.getData();
            loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
            loginUserSharedPreference.setLoginUser(user);
            useremail = user.getEmail();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.putExtra("useremail", useremail);
            intent.putExtra("state", true);
            startActivity(intent);
        } else {
            // 자동로그인에 등록된 유저가 없으면 로그인 화면으로 이동합니다
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            intent.putExtra("state", true);
            startActivity(intent);
        }
        finish();
    }
}
