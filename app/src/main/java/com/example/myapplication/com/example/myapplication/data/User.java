package com.example.myapplication.com.example.myapplication.data;

import android.net.Uri;

public class User {

    // FIELDS
    private String email; // 이메일
    private String password; // 비밀번호
    private String userName; // 유저 이름
    private String userUriString; // 유저 프로필 이미지uri
    private String introduce; // 자기소개 글
    private String userUri;

    // CONSTRUCTOR
    public User() {
        super();
    }

    public User(String email, String password) {
        this.email = email;
        this.password = password;

        userName = "춘이";
        userUriString = "";
        introduce = "";
    }

    // METHOD
    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserUriString() {
        return userUriString;
    }

    public void setUserUriString(String userUri) {
        this.userUriString = userUri;
    }

    public Uri getUserUri() {
        if (!userUriString.equals(""))
            return Uri.parse(userUriString);

        // uri 값 설정이 안되어 있으면 null 값을 리턴합니다
        return null;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public boolean chekPassword(String password) {
        if (this.password.equals(password)) {
            return true; // 같으면 true
        } else {
            return false; // 틀리면 false
        }
    }

    public boolean isLoginWithGoogle() {
        if(password.equals("")) // 비밀번호가 없으면 구글 로그인 유저
            return true;
        return false;
    }
}
