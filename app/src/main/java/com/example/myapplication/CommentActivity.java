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
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {

    public static final int COMMENT_EDIT_COMPLETE = 5; // 댓글 수정 완료

    private ImageView backImage; // 뒤로가기
    private ImageView attachImage; // 댓글 추가하기
    private TextView commentEdit; // 댓글 내용

    private RecyclerView recyclerView; // 리사이클러뷰
    private CommentRecyclerAdapter adapter; // 리사이클러뷰 어댑터
    private RecyclerView.LayoutManager layoutManager; // 리사이클러뷰 레이아웃매니저

    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키 값
    private int diaryposition; // 일기 얻기위한 포지션 값
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private DiaryBook diaryBook; // 일기책
    private Diary diary; // 일기
    private UserSharedPreference userSharedPreference; // 유저 데이터 관리
    private User user; // 유저 데이터 (댓글 달 때 유저의 데이터가 필요합니다)

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        if (db == null)
            db = FirebaseFirestore.getInstance();

        // 일기책, 일기 데이터 얻기
        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail"); // 사용자 이메일
        diarybookkey = receivedIntent.getExtras().getString("diarybookkey"); // 일기책 키 값
        diaryposition = receivedIntent.getExtras().getInt("diaryposition"); // 일기 포지션 값
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기책 얻기
        diary = diaryBook.getDiaries().get(diaryposition); // 일기 얻기
        userSharedPreference = new UserSharedPreference(getSharedPreferences("users", MODE_PRIVATE));
        user = userSharedPreference.findUserData(useremail); // 댓글 작성하는 유저 데이터 얻기

        // 리사이클러 뷰
        recyclerView = (RecyclerView) findViewById(R.id.activity_comment_recyclerview);
        recyclerView.setHasFixedSize(true);
        // 리사이클러뷰 레이아웃매니저
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        // 리사이클러뷰 어댑터
        adapter = new CommentRecyclerAdapter(this, useremail, diarybookkey, diaryBookSharedPreference, diaryposition, userSharedPreference);
        recyclerView.setAdapter(adapter);

        commentEdit = findViewById(R.id.activity_comment_edittext); // 댓글 작성
        attachImage = findViewById(R.id.activity_commnet_attachImage); // 댓글 추가하기
        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // 댓글 추가허기
                final String comment = commentEdit.getText().toString();
                // 댓글 내용을 작성했는지 확인하기
                if (comment.equals("")) // 댓글 내용을 작성하지 않았으면 리턴합니다
                    return;

                final MyProgressDialog myProgressDialog = new MyProgressDialog(CommentActivity.this, "잠시만 기다려주세요...");

                // 디비에 일기책 가져오기
                db.collection("threeline").document("diarybooks").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> dbMap = task.getResult().getData();
                            String diarybookJson = dbMap.get(diaryBook.getDiaryBookKey()).toString();
                            ConvertData convertData = new ConvertData();
                            DiaryBook dbDiaryBook = convertData.jsonToDiarybook(diarybookJson);

                            // 댓글 추가하기
                            DiaryComment diaryComment = new DiaryComment(comment, user.getEmail());
                            dbDiaryBook.getDiaries().get(diaryposition).getComments().add(diaryComment);

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

                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                    adapter.setDiaryBook(diaryBook);
                                    commentEdit.setText("");
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 디비에 데이터 수정 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(CommentActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });


        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_comment_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK)
            return;

        if (requestCode == COMMENT_EDIT_COMPLETE) { // 댓글 수정 완료
            diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 변경된 일기책 받아오기
            adapter.setDiaryBook(diaryBook); // 변경된 일기책 내용 알려주기
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // 모든 일기책 데이터 가져오기
        DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
        diarybooksDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> dbMap = task.getResult().getData();
                    if (dbMap == null)
                        return;
                    DiaryBookSharedPreference tempDiaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
                    tempDiaryBookSharedPreference.clearDiaryBooks();
                    for (Map.Entry<String, Object> entry : dbMap.entrySet()) {
                        tempDiaryBookSharedPreference.saveMapData(entry.getKey(), entry.getValue().toString());
                    }
                    diaryBookSharedPreference = tempDiaryBookSharedPreference;
                    diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);
                    adapter.setDiaryBook(diaryBook);

                }
            }
        });

//        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);
//        adapter.setDiaryBook(diaryBook);
    }
}
