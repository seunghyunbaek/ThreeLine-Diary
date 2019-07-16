package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.example.myapplication.com.example.myapplication.data.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NewDiaryBookActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 1;

    private ImageView backImage;
    private ImageView diaryOpenImage;
    private ImageView diaryCoverImage;
    private ImageView diaryComplete;

    private TextView diaryBookTitle1;
    private TextView diaryBookTitle2;

    private boolean isOpen;
    private Uri diaryCoverUri;

    private String useremail;
    private String joinuseremail; // 한명이랑 같이 쓸 때
    private ArrayList<String> joinusersemail; // 여러명이랑 같이 쓸 때
    private ArrayList<String> outhorityUsersEmail;

    private LoginUserSharedPreference loginUserSharedPreference;
    private User loginUser;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private DiaryBook diaryBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_diary_book);

        if (db == null)
            db = FirebaseFirestore.getInstance();
        if (storage == null)
            storage = FirebaseStorage.getInstance();
        if (storageReference == null)
            storageReference = storage.getReference();

        loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
        loginUser = loginUserSharedPreference.getLoginUser();

        // 유저이메일 데이터 얻기
        Intent receivedIntent = getIntent();
        useremail = loginUser.getEmail();
        joinusersemail = receivedIntent.getExtras().getStringArrayList("joinusersemail"); // 여러명이랑 같이 쓸 때
        outhorityUsersEmail = new ArrayList<String>();
        outhorityUsersEmail.add(useremail); // 혼자쓸 때

        // 여러명이랑 같이 쓸 때
        if (joinusersemail != null) {
            for (String joinuser : joinusersemail) {
                if (!outhorityUsersEmail.contains(joinuser))
                    outhorityUsersEmail.add(joinuser);
            }
        }

        diaryCoverUri = null;
        isOpen = true;

        // 일기책 표지 제목 한줄
        diaryBookTitle1 = (TextView) findViewById(R.id.activity_newdiarybook_edit1);
        // 일기책 표지 제목 두줄
        diaryBookTitle2 = (TextView) findViewById(R.id.activity_newdiarybook_edit2);

        // 일기책 공개 여부 설정하기
        diaryOpenImage = (ImageView) findViewById(R.id.activity_newdiarybook_open);
        diaryOpenImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isOpen) {
                    diaryOpenImage.setImageResource(R.drawable.icon_new_diary_lock_off);
                    isOpen = !isOpen;
                } else {
                    diaryOpenImage.setImageResource(R.drawable.icon_new_diary_lock_on);
                    isOpen = !isOpen;
                }
            }
        });

        // 일기책 커버 이미지 설정하기
        diaryCoverImage = (ImageView) findViewById(R.id.activity_newdiarybookbook_cover);
        diaryCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        // 일기책 생성하기
        diaryComplete = (ImageView) findViewById(R.id.activity_newdiarybook_complete);
        diaryComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MyProgressDialog myProgressDialog = new MyProgressDialog(NewDiaryBookActivity.this, "잠시만 기다려주세요...");

                String title1 = diaryBookTitle1.getText().toString(); // 표지 제목 한줄
                String title2 = diaryBookTitle2.getText().toString(); // 표지 제목 두줄

                // 일기책 제목을 설정을 해줘야 한다.
                if (title1.equals("") && title2.equals("")) {
                    Toast.makeText(getApplicationContext(), "일기책 제목을 넣어주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 일기책 커버 이미지 설정을 해줘야 한다
                if (diaryCoverUri == null) {
                    Toast.makeText(getApplicationContext(), "일기책 표지 이미지를 넣어주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 일기책 표지 이미지 uri 문자열 얻기
                String diaryCoverUriString = diaryCoverUri.toString();

                // 일기책 생성하기
                diaryBook = new DiaryBook(title1, title2, diaryCoverUriString, isOpen, outhorityUsersEmail);

                // storage에 이미지 저장하기
                final StorageReference diarybooksRef = storageReference.child("diarybooks").child(diaryBook.getDiaryBookKey());
                diarybooksRef.putFile(diaryBook.getDiaryBookUri()).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // 일기책 이미지 업로드 성공
                        diarybooksRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                // 일기책 이미지 다운로드 성공
                                diaryBook.setDiaryBookUriString(uri.toString()); // 일기책 uri 설정하기

                                // 일기책 데이터 가져오기
                                final DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                diarybooksDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            // 일기책 데이터에 새로만든 일기책 추가하기
                                            Map<String, Object> dbMap = task.getResult().getData();
                                            if (dbMap == null) {
                                                dbMap = new HashMap<>();
                                            }
                                            ConvertData convertData = new ConvertData();
                                            dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));

                                            // db에 저장하기
                                            diarybooksDoc.set(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // 일기책 저장 성공
                                                    myProgressDialog.dismiss();

                                                    // 일기책 shared에 저장하기 ( 디비에 저장되면 저장하기 )
                                                    DiaryBookSharedPreference diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
                                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);

                                                    Intent resultIntent = new Intent();
                                                    setResult(RESULT_OK, resultIntent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // 일기책 데이터 저장실패
                                                    myProgressDialog.dismiss();
                                                    Toast.makeText(NewDiaryBookActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            myProgressDialog.dismiss();
                                            Toast.makeText(NewDiaryBookActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // 일기책 이미지 다운로드 실패
                                myProgressDialog.dismiss();
                                Toast.makeText(NewDiaryBookActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 일기책 이미지 업로드 실패
                        myProgressDialog.dismiss();
                        Toast.makeText(NewDiaryBookActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        // 뒤로가기 버튼
        backImage = (ImageView) findViewById(R.id.activity_newdiarybook_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM:
                // 갤러리에서 선택한 이미지의 Uri를 받아옵니다
                diaryCoverUri = data.getData();
                diaryCoverImage.setImageURI(diaryCoverUri);
                break;
        }
    }
}
