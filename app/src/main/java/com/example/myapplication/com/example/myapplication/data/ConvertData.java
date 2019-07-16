package com.example.myapplication.com.example.myapplication.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConvertData {
    // 데이터 형태 변환하는 클래스
    Gson gson;

    public ConvertData() {
        gson = new GsonBuilder().create();
    }

    public String userToJson(User user) {
        String userJson = gson.toJson(user, User.class);
        return userJson;
    }

    public User jsonToUser(String userJson) {
        User user = gson.fromJson(userJson, User.class);
        return user;
    }

    public String diarybooktoJson(DiaryBook diaryBook) {
        String diaryBookJson = gson.toJson(diaryBook, DiaryBook.class);
        return diaryBookJson;
    }

    public DiaryBook jsonToDiarybook(String diarybookJson) {
        DiaryBook diaryBook = gson.fromJson(diarybookJson, DiaryBook.class);
        return diaryBook;
    }

    public String dotToUnderscore(String email) {
        return email.replace(".", "_");
    }
}
