package com.example.myapplication;

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

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class DiaryBookEditActivity extends AppCompatActivity {

    private static final int PICK_FROM_ALBUM = 1;

    private ImageView backImage; // 뒤로가기
    private ImageView diaryOpenImage; // 일기책 공개 여부 변경
    private ImageView diaryCoverImage; // 일기책 이미지 변경
    private ImageView diaryComplete; // 일기책 수정하기

    private TextView diaryBookEditTitle1; // 일기책 제목 한줄
    private TextView diaryBookEditTitle2; // 일기책 제목 두줄

    private Uri diaryBookCoverUri; // 일기책 표지 Uri
    private boolean isOpen; // 일기책 공개 여부

    private String diaryBookKey; // 일기책 키 값
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private DiaryBook diaryBook; // 수정할 일기책 데이터

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_book_edit);

        // 일기책 데이터 얻기
        Intent receivedIntent = getIntent();
        diaryBookKey = receivedIntent.getExtras().getString("diarybookkey");
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diaryBookKey);

        isOpen = diaryBook.isOpen(); // 일기책 공개 여부
        diaryBookCoverUri = diaryBook.getDiaryBookUri(); // 일기책 표지 Uri

        if (db == null)
            db = FirebaseFirestore.getInstance();
        if (storage == null)
            storage = FirebaseStorage.getInstance();
        if (storageRef == null)
            storageRef = storage.getReference();

        // 일기책 제목 설정하기
        diaryBookEditTitle1 = (TextView) findViewById(R.id.activity_diary_book_edit_edit1);
        diaryBookEditTitle2 = (TextView) findViewById(R.id.activity_diary_book_edit_edit2);
        diaryBookEditTitle1.setText(diaryBook.getDiaryTitle1());
        diaryBookEditTitle2.setText(diaryBook.getDiaryTitle2());

        // 일기책 공개여부 설정하기
        diaryOpenImage = (ImageView) findViewById(R.id.activity_diary_book_edit_open);
        if (isOpen) { // 일기책 공개 여부 표시
            diaryOpenImage.setImageResource(R.drawable.icon_new_diary_lock_off);
        } else {
            diaryOpenImage.setImageResource(R.drawable.icon_new_diary_lock_on);
        }

        // 일기책 공개 여부 변경하기
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

        // 일기책 이미지 설정하기
        diaryCoverImage = (ImageView) findViewById(R.id.activity_diary_book_edit_cover);
//        diaryCoverImage.setImageURI(diaryBook.getDiaryBookUri());
        Glide.with(DiaryBookEditActivity.this)
                .load(diaryBook.getDiaryBookUri())
                .into(diaryCoverImage);

        // 일기책 표지 이미지 변경하기
        diaryCoverImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        // 일기책 수정 완료하기
        diaryComplete = (ImageView) findViewById(R.id.activity_diary_book_edit_complete);
        diaryComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MyProgressDialog myProgressDialog = new MyProgressDialog(DiaryBookEditActivity.this, "잠시만 기다려주세요...");

                String diaryBookTitle1 = diaryBookEditTitle1.getText().toString();
                String diaryBookTitle2 = diaryBookEditTitle2.getText().toString();

                // 일기책 표지 제목 확인하기
                if (diaryBookTitle1.equals("") && diaryBookTitle2.equals("")) {
                    Toast.makeText(DiaryBookEditActivity.this, "제목을 작성해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 일기책 표지 제목 수정하기
                diaryBook.setDiaryTitle1(diaryBookTitle1);
                diaryBook.setDiaryTitle2(diaryBookTitle2);
                // 일기책 공개 여부 수정하기
                diaryBook.setOpen(isOpen);

                // 일기책 이미지를 안바꿨을때
                if (diaryBookCoverUri.toString().equals(diaryBook.getDiaryBookUri().toString())) {
                    // db에 넣을 데이터 폼 만들기
                    Map<String, Object> dbMap = new HashMap<>();
                    ConvertData convertData = new ConvertData();
                    dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));
                    
                    // db에 저장하기
                    DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                    diarybooksDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            // db에 저장성공
                            myProgressDialog.dismiss();

                            // 일기책 데이터 shared에 저장하기
                            diaryBookSharedPreference.saveDiaryBook(diaryBook);

                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // db에 저장 실패
                            myProgressDialog.dismiss();
                            Toast.makeText(DiaryBookEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else { // 일기책 이미지를 바꿨을때
                    final StorageReference diarybooksRef = storageRef.child("diarybooks").child(diaryBook.getDiaryBookKey());
                    diarybooksRef.putFile(diaryBookCoverUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // 일기책 이미지 업로드 성공
                                    diarybooksRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // 일기책 이미지 다운로드 성공
                                            // 일기책 이미지 uri 변경하기
                                            diaryBook.setDiaryBookUriString(uri.toString());

                                            // db에 넣을 데이터 폼 만들기
                                            Map<String, Object> dbMap = new HashMap<>();
                                            ConvertData convertData = new ConvertData();
                                            dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));

                                            // db에 저장하기
                                            DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                            diarybooksDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // db에 저장성공
                                                    myProgressDialog.dismiss();

                                                    // 일기책 데이터 shared에 저장하기
                                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);

                                                    Intent resultIntent = new Intent();
                                                    setResult(RESULT_OK, resultIntent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // db에 저장 실패
                                                    myProgressDialog.dismiss();
                                                    Toast.makeText(DiaryBookEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 일기책 이미지 다운로드 실패
                                            myProgressDialog.dismiss();
                                            Toast.makeText(DiaryBookEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 일기책 이미지 업로드 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(DiaryBookEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_diary_book_edit_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    // 앨범에서 이미지 가져오기
    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }

    // 앨범에서 이미지 가져오기
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM:
                // 갤러리에서 선택한 이미지의 Uri를 받아옵니다
                diaryBookCoverUri = data.getData();
                // 가져온 이미지 보여주기
                diaryCoverImage.setImageURI(diaryBookCoverUri);
                break;
        }
    }


}