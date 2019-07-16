package com.example.myapplication.com.example.myapplication.data;

import com.google.firebase.firestore.FirebaseFirestore;

public class BaseFirebase {
    private FirebaseFirestore mFirebaseFirestore;

    public BaseFirebase() {
    }

    // 파이어베이스 초기화 인스턴스 얻기
    public void initFirestore() {
        mFirebaseFirestore = FirebaseFirestore.getInstance();
    }


}
