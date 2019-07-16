package com.example.myapplication.com.example.myapplication.data;

import android.content.SharedPreferences;

import java.util.Map;

public class LoginUserSharedPreference extends BaseSharedPreference {
    // 로그인한 유저의 데이터가 담겨있는 클래스
    public LoginUserSharedPreference(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    // 어플을 나가면 유저의 데이터를 날립니다
    public void clearUser() {
        editor.clear();
        editor.commit();
    }

    // 로그인한 유저의 데이터를 얻습니다
    public User getLoginUser() {
        User user = null;
        String userJson;
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            userJson = sharedPreferences.getString(entry.getKey(), "");
            user = gson.fromJson(userJson, User.class);
        }

        return user; // 로그인한 유저의 데이터를 반환합니다
    }

    // 로그인하는 유저의 데이터를 담는다
    public void setLoginUser(User user) {
        String userJson = gson.toJson(user, User.class);
        editor.putString(user.getEmail(), userJson);
        editor.commit();
    }
}
