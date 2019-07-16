package com.example.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.Diary;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WriteDiaryActivity extends AppCompatActivity {

    private static final String TAG = "WriteDiaryActivity";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;
    private static final int WRITE_HIDDEN_DIARY = 3;

    private File tempFile; // 카메라로 사진 찍을 때 저장할 변수

    private ImageView writeDiaryCamera; // 카메라로 보기
    private ImageView writeDiarySelectImageView; // 갤러리 이미지 선택하기

    private TextView writeDiaryTitle; // 일기 책 제목
    private TextView writeDiaryEdit1; // 첫줄 일기 내용
    private TextView writeDiaryEdit2; // 두줄 일기 내용
    private TextView writeDiaryEdit3; // 세줄 일기 내용
    private TextView wirteDiaryLocation; // 장소 내용
    private TextView writeDiaryDate; // 일기작성 날짜

    private ImageView writeDiaryHiddenDiaryImage; // 숨은 일기 작성하러가기

    private ImageView writeDiaryComplete; // 일기 작성 완료

    private ImageView backImage; // 뒤로가기

    private Uri diaryUri; // 선택한 이미지 Uri
    private String hiddenDiaryEdit; // 숨은 일기 내용

    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키
    private DiaryBookSharedPreference diaryBookSharedPreference;
    private DiaryBook diaryBook; // 일기를 작성할 일기책
    private Diary diary; // 작성한 일기

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);

        if (db == null) // db 인스턴스 생성
            db = FirebaseFirestore.getInstance();
        if (storage == null) // 스토리지 인스턴스 생성
            storage = FirebaseStorage.getInstance();
        if (storageRef == null) // 스토리지 레퍼런스 참조 생성
            storageRef = storage.getReference();

        Intent receivedIntent = getIntent();
        useremail = receivedIntent.getExtras().getString("useremail"); // 사용자 이메일
        diarybookkey = receivedIntent.getExtras().getString("diarybookkey"); // 일기작성할 일기책 키

        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기작성할 일기책 가져오기

        hiddenDiaryEdit = ""; // 숨은 일기 책 내용

        if (diaryUri == null)
            diaryUri = new Uri.Builder().build();

        // 장소 작성하기
        wirteDiaryLocation = (EditText) findViewById(R.id.activity_writediary_location);

        writeDiaryTitle = findViewById(R.id.activity_writediary_title);
        writeDiaryTitle.setText(diaryBook.getDiaryBookTitleHorizontal());

        // 숨은 일기 작성하기
        writeDiaryHiddenDiaryImage = (ImageView) findViewById(R.id.activity_writediary_hidden);
        writeDiaryHiddenDiaryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), WriteHiddenDiaryActivity.class);
                intent.putExtra("hidden", hiddenDiaryEdit);
                startActivityForResult(intent, WRITE_HIDDEN_DIARY);
            }
        });

        // 카메라로 사진 찍기
        writeDiaryCamera = (ImageView) findViewById(R.id.activity_writediary_camera);
        writeDiaryCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // 앨범 이미지 선택하기
        writeDiarySelectImageView = (ImageView) findViewById(R.id.activity_writediary_selectimage);
        writeDiarySelectImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        // 세줄 일기 내용
        writeDiaryEdit1 = (EditText) findViewById(R.id.activity_writediary_edit1);
        writeDiaryEdit1.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        writeDiaryEdit2 = (EditText) findViewById(R.id.activity_writediary_edit2);
        writeDiaryEdit3 = (EditText) findViewById(R.id.activity_writediary_edit3);

        // 일기작성 날짜
        writeDiaryDate = findViewById(R.id.activity_writediary_date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일/ ");
        writeDiaryDate.setText(sdf.format(new Date()));

        // 일기 작성 완료
        writeDiaryComplete = (ImageView) findViewById(R.id.activity_writediary_complete);
        writeDiaryComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MyProgressDialog myProgressDialog = new MyProgressDialog(WriteDiaryActivity.this, "잠시만 기다려주세요...");

                // 세줄 일기 내용
                String writeDiaryContent1 = writeDiaryEdit1.getText().toString();
                String writeDiaryContent2 = writeDiaryEdit2.getText().toString();
                String writeDiaryContent3 = writeDiaryEdit3.getText().toString();

                // 장소 내용
                String location = wirteDiaryLocation.getText().toString();

                // 새 일기 만들어서 일기책에 추가하기
                diary = new Diary(writeDiaryContent1, writeDiaryContent2, writeDiaryContent3, hiddenDiaryEdit, diaryUri.toString(), location, useremail);

                if (diaryUri.toString().equals("")) {
                    // 이미지가 없을 때
                    diaryBook.addDiary(diary); // 일기책에 일기 추가하기

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
                                    myProgressDialog.dismiss();
                                    // 일기책 데이터 shared에 저장하기
                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                    finish();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // db에 저장 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(WriteDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // 이미지가 있을 때
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
                                    diaryBook.addDiary(diary); // 일기책에 일기 추가하기

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
                                                    myProgressDialog.dismiss();
                                                    // 일기책 데이터 shared에 저장하기
                                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                                    finish();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // db에 저장 실패
                                                    myProgressDialog.dismiss();
                                                    Toast.makeText(WriteDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 이미지 다운로드 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(WriteDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // 이미지 업로드 실패
                            myProgressDialog.dismiss();
                            Toast.makeText(WriteDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // 뒤로 -가기
        backImage = (ImageView) findViewById(R.id.activity_writediary_back);
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

    @Override
    public void onBackPressed() {
        if (isWriting())
            DialogSimple();
        else
            super.onBackPressed();
    }

    private void DialogSimple() {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(this);
        alt_bld.setMessage("작성중인 내용이 있습니다.\n종료하시겠습니까?").setCancelable(false).setPositiveButton("종료하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                WriteDiaryActivity.super.onBackPressed();
            }
        }).setNegativeButton("계속하기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        AlertDialog alert = alt_bld.create();
//        alert.setTitle("Title");
        alert.show();
    }

    // 작성중인지 아닌지 확인하기
    private boolean isWriting() {

        String edit1 = writeDiaryEdit1.getText().toString();
        String edit2 = writeDiaryEdit2.getText().toString();
        String edit3 = writeDiaryEdit3.getText().toString();
        String location = wirteDiaryLocation.getText().toString();
        String hidden = hiddenDiaryEdit;
        String uriString = "";
        if (diaryUri != null)
            uriString = diaryUri.toString();

        if (!edit1.equals("") || !edit2.equals("") || !edit3.equals("")
                || !location.equals("") || !hidden.equals("") || !uriString.equals(""))
            return true;

        return false;
    }
}