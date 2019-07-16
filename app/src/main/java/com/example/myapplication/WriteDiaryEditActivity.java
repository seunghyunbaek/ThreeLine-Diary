package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.Diary;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WriteDiaryEditActivity extends AppCompatActivity {

    private static final String TAG = "WriteDiaryEditActivity";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int WRITE_HIDDEN_DIARY = 3;

    private File tempFile; // 카메라로 사진 찍을 때 저장할 변수

    private ImageView writeDiaryCamera; // 카메라로 보기
    private ImageView writeDiarySelectImageView; // 갤러리 이미지 선택하기

    private TextView writeDiaryTitle; // 일기책 제목
    private TextView writeDiaryEdit1; // 첫줄 일기 내용
    private TextView writeDiaryEdit2; // 두줄 일기 내용
    private TextView writeDiaryEdit3; // 세줄 일기 내용
    private TextView wirteDiaryLocation; // 장소 내용
    private TextView writeDiaryDate; // 일기작성 날짜

    private ImageView writeDiaryHiddenDiaryImage; // 숨은 일기 작성하러가기

    private ImageView writeDiaryComplete; // 일기 작성 완료

    private ImageView backImage; // 뒤로가기

    private Uri diaryUri; // 선택한 이미지 Uri저장
    private String hiddenDiaryEdit; // 숨은 일기 내용

    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키
    private DiaryBookSharedPreference diaryBookSharedPreference;
    private DiaryBook diaryBook; // 일기를 작성할 일기책
    private int diaryposition; // 일기 인덱스
    private Diary diary;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary_edit);

        if (db == null) // db 인스턴스 생성
            db = FirebaseFirestore.getInstance();
        if (storage == null) // 스토리지 인스턴스 생성
            storage = FirebaseStorage.getInstance();
        if (storageRef == null) // 스토리지 레퍼런스 참조 생성
            storageRef = storage.getReference();

        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail"); // 사용자 이메일
        diarybookkey = receivedIntent.getExtras().getString("diarybookkey"); // 일기가 들어있는 일기책 키 값
        diaryposition = receivedIntent.getExtras().getInt("diaryposition"); // 일기 인덱스

        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기들어있는 일기책 가져오기
        diary = diaryBook.getDiaries().get(diaryposition);
        diaryUri = diary.getDiaryUri();

        // 장소 작성하기
        wirteDiaryLocation = (EditText) findViewById(R.id.activity_writediaryedit_location);
        wirteDiaryLocation.setText(diary.getLocation());

        writeDiaryTitle = findViewById(R.id.activity_writediaryedit_title);
        writeDiaryTitle.setText(diaryBook.getDiaryBookTitleHorizontal());

        // 숨은 일기 작성하기
        writeDiaryHiddenDiaryImage = (ImageView) findViewById(R.id.activity_writediaryedit_hidden);
        hiddenDiaryEdit = diary.getHiddenContentText(); // 숨은 일기 내용 얻기
        writeDiaryHiddenDiaryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteHiddenDiaryActivity.class);
                intent.putExtra("hidden", hiddenDiaryEdit);
                startActivityForResult(intent, WRITE_HIDDEN_DIARY);
            }
        });

        // 카메라로 사진 찍기
        writeDiaryCamera = (ImageView) findViewById(R.id.activity_writediaryedit_camera);
        writeDiaryCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // 앨범 이미지 선택하기
        writeDiarySelectImageView = (ImageView) findViewById(R.id.activity_writediaryedit_selectimage);
        if (diary.getDiaryUri() != null) {// 이미지 뷰 세팅하기
//            writeDiarySelectImageView.setImageURI(diary.getDiaryUri());
            Glide.with(WriteDiaryEditActivity.this).load(diary.getDiaryUri()).into(writeDiarySelectImageView);
        }

        writeDiarySelectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        // 세줄 일기 내용
        writeDiaryEdit1 = (EditText) findViewById(R.id.activity_writediaryedit_edit1);
        writeDiaryEdit1.setText(diary.getContentText1());
        writeDiaryEdit2 = (EditText) findViewById(R.id.activity_writediaryedit_edit2);
        writeDiaryEdit2.setText(diary.getContentText2());
        writeDiaryEdit3 = (EditText) findViewById(R.id.activity_writediaryedit_edit3);
        writeDiaryEdit3.setText(diary.getContentText3());

        // 일기작성 날짜
        writeDiaryDate = findViewById(R.id.activity_writediaryedit_date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일/ ");
        writeDiaryDate.setText(sdf.format(new Date()));

        // 일기 작성 완료
        writeDiaryComplete = (ImageView) findViewById(R.id.activity_writediaryedit_complete);
        writeDiaryComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                final MyProgressDialog myProgressDialog = new MyProgressDialog(WriteDiaryEditActivity.this, "잠시만 기다려주세요...");

                // 세줄 일기 내용
                String writeDiaryContent1 = writeDiaryEdit1.getText().toString();
                String writeDiaryContent2 = writeDiaryEdit2.getText().toString();
                String writeDiaryContent3 = writeDiaryEdit3.getText().toString();

                diary.setContentText1(writeDiaryContent1);
                diary.setContentText2(writeDiaryContent2);
                diary.setContentText3(writeDiaryContent3);

                // 장소 내용
                String location = wirteDiaryLocation.getText().toString();
                diary.setLocation(location);

                // 숨은 일기 내용
                diary.setHiddenContentText(hiddenDiaryEdit);

                if (diaryUri == null || diaryUri.toString().equals(diary.getDiaryUriString())) {
                    // 이미지를 안바꿨을 때
                    diaryBook.setDiary(diaryposition, diary); // 일기책에 일기 수정하기

                    // db에 넣을 데이터 폼 만들기
                    Map<String, Object> dbMap = new HashMap<>();
                    ConvertData convertData = new ConvertData();
                    dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));

                    // db에 저장하기
                    DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                    diarybooksDoc.update(dbMap)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // db에 저장성공
//                                    myProgressDialog.dismiss();
                                    // 일기책 데이터 shared에 저장하기
                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // db에 저장 실패
//                                    myProgressDialog.dismiss();
                                    Toast.makeText(WriteDiaryEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // 이미지를 바꿨을 때
                    final StorageReference diaryRef = storageRef.child("diarybooks").child("diary").child(diary.getDiaryKey());
                    diaryRef.putFile(diaryUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // 이미지 업로드 성공
                            diaryRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // 이미지 다운로드 성공
                                    diary.setDiaryUriString(uri.toString());
                                    diaryBook.setDiary(diaryposition, diary); // 일기책에 일기 수정하기

                                    // db에 넣을 데이터 폼 만들기
                                    Map<String, Object> dbMap = new HashMap<>();
                                    ConvertData convertData = new ConvertData();
                                    dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));

                                    // db에 저장하기
                                    DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                    diarybooksDoc.update(dbMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // db에 저장성공
//                                                    myProgressDialog.dismiss();
                                                    // 일기책 데이터 shared에 저장하기
                                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // db에 저장 실패
//                                                    myProgressDialog.dismiss();
                                                    Toast.makeText(WriteDiaryEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 이미지 다운로드 실패
//                                    myProgressDialog.dismiss();
                                    Toast.makeText(WriteDiaryEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // 이미지 업로드 실패
//                            myProgressDialog.dismiss();
                            Toast.makeText(WriteDiaryEditActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // 뒤로 가기
        backImage = (ImageView) findViewById(R.id.activity_writediaryedit_back);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + "삭제 성공");
                        tempFile = null;
                    }
                }
            }
            return;
        }

        switch (requestCode) {
            case PICK_FROM_ALBUM:
                // 갤러리에서 선택한 이미지의 Uri를 받아옵니다
                diaryUri = data.getData();
                writeDiarySelectImageView.setImageURI(diaryUri);
                break;
            case PICK_FROM_CAMERA:
                // 카메라로 찍은 사진의 Uri를 받아옵니다
                diaryUri = Uri.parse(tempFile.toURI().toString());
                writeDiarySelectImageView.setImageURI(diaryUri);
                break;
            case WRITE_HIDDEN_DIARY:
                // 작성한 숨은 일기 내용의 데이터를 가져옵니다
                hiddenDiaryEdit = data.getExtras().get("hidden").toString();
                break;
        }
    }

    // 사진 찍기
    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }

        // 카메라로 보내는 tempFile의 uri를 provider로 감싸주는 로직이 추가되었습니다.
        // 안드로이드 누가 하위 버전에서는 provider로 uri를 감싸주면 동작하지 않는 경우가 있기 때문에 모든 기기에 적용하기 위해서는 버전 구분을 꼭 해줘야 합니다
        if (tempFile != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.myapplication.provider", tempFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
            } else {
                Uri photoUri = Uri.fromFile(tempFile);
                // tempFile의 Uri 경로를 intent에 추가해줘야 합니다.
                // 이는 카메라에서 찍은 사진이 저장될 주소를 의미합니다.
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    // 사진 파일 생성하기
    private File createImageFile() throws IOException {
        // 이미지 파일 이름 ( threelinediary_(시간)_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "threelinediary_" + timeStamp + "_";

        //  이미지가 저장될 폴더 이름 ( threelinediary )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/threelinediary/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 빈 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;
    }
}
