package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.Diary;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.DiaryComment;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommentEditActivity extends AppCompatActivity {

    private EditText editText; // 댓글 수정하기
    private ImageView backImage; // 뒤로가기
    private ImageView completeImage; // 댓글 수정 완료

    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private String diarybookkey; // 일기책 키 값
    private int diaryposition; // 댓글 작성하고 있는 일기 인덱스
    private int commentposition; // 수정할 댓글 인덱스
    private DiaryBook diaryBook; // 일기책
    private Diary diary; // 댓글 달고있는 일기
    private DiaryComment diaryComment; // 수정할 댓글

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment_edit);

        if (db == null)
            db = FirebaseFirestore.getInstance();

        // 일기책, 일기 데이터 가져오기
        Intent receivedIntent = getIntent();
        diaryposition = receivedIntent.getExtras().getInt("diaryposition");
        commentposition = receivedIntent.getExtras().getInt("commentposition");
        diarybookkey = receivedIntent.getExtras().getString("diarybookkey");
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기책 데이터 가져오기
        diary = diaryBook.getDiaries().get(diaryposition); // 일기 데이터 가져오기
        diaryComment = diary.getComments().get(commentposition); // 수정할 댓글

        // 작성했던 댓글 설정하기
        editText = (EditText) findViewById(R.id.activity_comment_edit_edittext);
        editText.setText(diaryComment.getCommentText());

        // 댓글 수정하기
        completeImage = (ImageView) findViewById(R.id.activity_comment_edit_complete);
        completeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MyProgressDialog myProgressDialog = new MyProgressDialog(CommentEditActivity.this, "잠시만 기다려주세요...");

                String comment = editText.getText().toString();

                // 수정한 내용이 공백인지 확인하기
                if (comment.equals("")) {
                    Toast.makeText(CommentEditActivity.this, "댓글 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                diaryComment.setCommentText(comment); // 댓글 내용 수정하기 ( 작성 시간도 변경됨 : 일단 시간 수정되는건 주석처리함)
                diary.setDiaryComment(commentposition, diaryComment); // 수정된 댓글 일기에 설정하기
                diaryBook.setDiary(diaryposition, diary); // 수정된 일기 일기책에 설정하기

                // 디비에 저장할 데이터 폼만들기
                Map<String, Object> dbMap = new HashMap<>();
                ConvertData convertData = new ConvertData();
                dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));

                // 변경된 일기책 데이터 디비에 저장하기
                DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                diarybooksDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 디비에 저장 성공
                        myProgressDialog.dismiss();
                        diaryBookSharedPreference.saveDiaryBook(diaryBook); // 변경된 일기책 데이터 저장하기

                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 디비에 저장 실패
                        myProgressDialog.dismiss();
                        Toast.makeText(CommentEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    }
                });

                /*diaryBookSharedPreference.saveDiaryBook(diaryBook); // 변경된 일기책 데이터 저장하기

                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();*/
            }
        });

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_comment_edit_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
