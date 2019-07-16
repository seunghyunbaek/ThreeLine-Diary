package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.Diary;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.DiaryComment;
import com.example.myapplication.com.example.myapplication.data.DiaryCommentReply;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DiaryCommentReplyActivity extends AppCompatActivity {

    public static final int COMMENT_REPLY_EIDT_COMPLETE = 6; // 답글 수정 완료

    private ImageView backImage; // 뒤로가기
    private ImageView attachImage; // 답글 추가하기
    private TextView commentEdit; // 답글 작성

    private RecyclerView recyclerView; // 리사이클러뷰
    private CommentReplyAdapter adapter; // 사이클러뷰 어댑터
    private RecyclerView.LayoutManager layoutManager; // 리사이클러뷰 레이아웃매니저

    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키 값
    private int diaryposition; // 일기 인덱스
    private int commentposition; // 댓글 인덱스
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private UserSharedPreference userSharedPreference; // 유저 데이터 관리

    private DiaryBook diaryBook; // 일기책 데이터
    private Diary diary; // 일기
    private DiaryComment diaryComment; // 일기 댓글
    private ArrayList<DiaryCommentReply> diaryCommentReplies;
    private User user; // 유저 데이터

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_comment_reply);

        if (db == null)
            db = FirebaseFirestore.getInstance();

        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail");
        diarybookkey = receivedIntent.getExtras().getString("diarybookkey");
        diaryposition = receivedIntent.getExtras().getInt("diaryposition");
        commentposition = receivedIntent.getExtras().getInt("commentposition");
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기책 얻기
        diary = diaryBook.getDiaries().get(diaryposition); // 일기 얻기
        diaryComment = diary.getComments().get(commentposition); // 댓글 얻기
        diaryCommentReplies = diaryComment.getReplies(); // 답글 목록 얻기
        userSharedPreference = new UserSharedPreference(getSharedPreferences("users", MODE_PRIVATE));

        // 리사이클러뷰
        recyclerView = (RecyclerView) findViewById(R.id.acivity_diary_comment_reply_recyclerview);
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 레이아웃매니저
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 어댑터
        adapter = new CommentReplyAdapter(this, useremail, diarybookkey, diaryBookSharedPreference, diaryposition, commentposition, userSharedPreference);
        recyclerView.setAdapter(adapter);

        // 답글 작성
        commentEdit = findViewById(R.id.acivity_diary_comment_reply_edittext);
        // 답글 추가하기
        attachImage = findViewById(R.id.acivity_diary_comment_reply_attachimage);
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String commentReply = commentEdit.getText().toString(); // 답글 내용

                // 답글 작성했는지 확인하기
                if (commentReply.equals("")) {// 답글 작성을 아무것도 하지 않았으면 리턴합니다
                    return;
                }

                final MyProgressDialog myProgressDialog = new MyProgressDialog(DiaryCommentReplyActivity.this, "잠시만 기다려주세요...");

                // 디비에 일기책 가져오기
                db.collection("threeline").document("diarybooks").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> dbMap = task.getResult().getData();
                            String diarybookJson = dbMap.get(diaryBook.getDiaryBookKey()).toString();
                            ConvertData convertData = new ConvertData();
                            DiaryBook dbDiaryBook = convertData.jsonToDiarybook(diarybookJson);

                            // 답글 추가하기
                            DiaryCommentReply diaryCommentReply = new DiaryCommentReply(commentReply, useremail);
                            dbDiaryBook.getDiaries().get(diaryposition).getComments().get(commentposition).getReplies().add(diaryCommentReply);

                            final DiaryBook changedDiaryBook = dbDiaryBook;

                            Map<String, Object> updateMap = new HashMap<>();
                            updateMap.put(dbDiaryBook.getDiaryBookKey(), convertData.diarybooktoJson(dbDiaryBook));
                            db.collection("threeline").document("diarybooks").update(updateMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // 디비에 데이터 수정 성공
                                    myProgressDialog.dismiss();
                                    diaryBook = changedDiaryBook;
                                    diary = diaryBook.getDiaries().get(diaryposition);
                                    diaryComment = diary.getComments().get(commentposition); // 댓글 얻기
                                    diaryCommentReplies = diaryComment.getReplies(); // 답글 목록 얻기

                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                    adapter.setDiaryBook(diaryBook);
                                    commentEdit.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 디비에 데이터 수정 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(DiaryCommentReplyActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.acivity_diary_comment_reply_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        // 답글 수정 완료
        if (requestCode == COMMENT_REPLY_EIDT_COMPLETE) {
            diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);
            adapter.setDiaryBook(diaryBook);
        }
    }
}
