package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class WatchDiaryHiddenActivity extends AppCompatActivity {

    private ImageView backImage;
    private TextView hiddenTextView;

    private String hiddenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_diary_hidden);

        Intent receivedIntent = getIntent();
        hiddenText = receivedIntent.getExtras().getString("hidden");

        hiddenTextView = findViewById(R.id.activity_watch_diary_hidden_text);
        hiddenTextView.setText(hiddenText);

        // 뒤로가기
        backImage = findViewById(R.id.activity_watch_diary_hidden_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
