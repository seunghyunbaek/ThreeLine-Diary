package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

public class WriteHiddenDiaryActivity extends AppCompatActivity {

    private ImageView completeImage;
    private ImageView backImage;
    private EditText writeText;

    private String hiddenEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_hidden_diary);

        Intent receivedIntent = getIntent();
        hiddenEdit = receivedIntent.getExtras().getString("hidden", "");

        // 숨은 일기 내용 작성
        writeText = (EditText) findViewById(R.id.activity_writehiddendiary_edittext);
        writeText.setText(hiddenEdit);

        // 숨은 일기 작성완료
        completeImage = (ImageView) findViewById(R.id.activity_writehiddendiary_complete);
        completeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("hidden", writeText.getText().toString());
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_writehiddendiary_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
