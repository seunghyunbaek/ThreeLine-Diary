package com.example.myapplication.com.example.myapplication.data;

import android.content.SharedPreferences;

public class TempSharedPreference extends BaseSharedPreference {
    public TempSharedPreference(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    // 데이터 임시 저장하기
    public void saveTempData(Diary diary) {

    }

    // 임시 데이터 불러오기
    public Diary getTempData() {

        return new Diary("", "", "", "", "", "", "");
    }

    // 임시 저장한 데이터 지우기
    public void deleteTempData() {

    }
}
