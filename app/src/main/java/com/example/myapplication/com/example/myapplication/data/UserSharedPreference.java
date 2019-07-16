package com.example.myapplication.com.example.myapplication.data;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Map;

public class UserSharedPreference extends BaseSharedPreference {
    // 유저 데이터를 관리하는 클래스
    // shared에 저장하는 클래스

    // 생성자
    public UserSharedPreference(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    // 유저 데이터 생성하기
    public boolean createUserData(User user) {
        // 유저 데이터가 있으면 저장하지 않는다
        if (!sharedPreferences.getString(user.getEmail(), "").equals(""))
            return false;

        // 유저의 데이터가 없으면 저장합니다
        String userJson = gson.toJson(user, User.class);
        editor.putString(user.getEmail(), userJson);
        editor.commit();
        return true;
    }

    // 유저 데이터 저장하기
    public void saveUserData(User user) {
        String userJson = gson.toJson(user, User.class);
        editor.putString(user.getEmail(), userJson);
        editor.commit();
    }

    public void saveMapData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    // 유저 데이터 얻기
    public User findUserData(String email) {
        String userJson = sharedPreferences.getString(email, "");
        // 이메일에 맞는 유저가 없을 경우 null값을 리턴
        if (userJson.equals(""))
            return null;
        User user = gson.fromJson(userJson, User.class);
        return user;
    }

    // 모든 유저 데이터 얻기
    public ArrayList<User> loadAllUser() {
        ArrayList<User> users = new ArrayList<User>();

        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String userJson = sharedPreferences.getString(entry.getKey(), "");
            User user = gson.fromJson(userJson, User.class);
            users.add(user);
        }

        return users;
    }

    // 유저 데이터 삭제하기
    public void removeUser(String useremail) {
        editor.remove(useremail);
        editor.commit();
    }

    public void clearUses() {
        editor.clear();
        editor.commit();
    }
}
