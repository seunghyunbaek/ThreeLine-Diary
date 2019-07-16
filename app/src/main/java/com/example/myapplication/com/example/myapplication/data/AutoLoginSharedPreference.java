package com.example.myapplication.com.example.myapplication.data;

import android.content.SharedPreferences;

import java.util.Map;

public class AutoLoginSharedPreference extends BaseSharedPreference {
    // 자동로그인 유저 데이터를 관리하는 클래스

    public AutoLoginSharedPreference(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    // 자동로그인에 설정된 유저의 데이터를 가져온다
    public User getData() {
        User user = null;
        String userJson;
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            userJson = sharedPreferences.getString(entry.getKey(), "");
            if (!userJson.equals("")) // 유저 이메일이 있으면 유저객체를 얻는다
                user = gson.fromJson(userJson, User.class);
        }
        return user; // 유저 객체가 없으면 null이 반환된다
    }

    // 자동로그인에 등록된 유저가 있는지 확인한다
    public boolean isUser() {

        if (sharedPreferences.getAll().size() > 0)
            return true; // 등록된 유저가 있으면 true를 반환한다

        return false;
    }

    // 자동로그인에 유저 등록하기
    public void setAutoUser(User user) {
        String userJson = gson.toJson(user, User.class);
        editor.putString(user.getEmail(), userJson);
        editor.commit();
    }

    // 자동 로그인 등록된 유저 지우기
    public void clearData() {
        editor.clear(); // 데이터 모두 지우기
        editor.commit(); // 데이터 변경 저장하기
    }


}
