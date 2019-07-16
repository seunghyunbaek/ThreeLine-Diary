package com.example.myapplication.com.example.myapplication.data;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseSharedPreference {
    // 기본 SharedPreference 클래스
    protected SharedPreferences sharedPreferences;
    protected SharedPreferences.Editor editor;
    protected Gson gson;

    public BaseSharedPreference(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
        editor = sharedPreferences.edit();
        gson = new GsonBuilder().create();
    }
}
