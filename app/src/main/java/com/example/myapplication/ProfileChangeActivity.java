package com.example.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class ProfileChangeActivity extends AppCompatActivity {

    private static final String TAG = "ProfileChangeActivity";

    private static final int PICK_FROM_ALBUM = 1;

    private EditText userName;
    private ImageView userImage;
    private EditText userIntroduce;

    private Button userChangeComplete;

    private ImageView backImage;

    private Uri userUri;

    private LoginUserSharedPreference loginUserSharedPreference;
    private FirebaseFirestore db;
    private User loginUser;

    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        // 로그인한 유저의 데이터 얻기
        loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
        loginUser = loginUserSharedPreference.getLoginUser();

        if (db == null) // 파이어베이스 인스턴스 생성
            db = FirebaseFirestore.getInstance();
        if (storage == null)
            storage = FirebaseStorage.getInstance();
        if (storageRef == null)
            storageRef = storage.getReference();

        if (loginUser.getUserUri() != null)
            userUri = loginUser.getUserUri();
        else
            userUri = new Uri.Builder().build();

        // 유저 이름 세팅
        userName = (EditText) findViewById(R.id.activity_profilechange_edit_name);
        userName.setText(loginUser.getUserName());

        // 유저 이미지 세팅
        userImage = (ImageView) findViewById(R.id.activity_profilechange_profile_image);
        if (loginUser.getUserUri() != null) {
            Glide.with(ProfileChangeActivity.this)
                    .load(loginUser.getUserUri())
                    .into(userImage);
        }

        // 유저 이미지 변경하기
        userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToAlbum();
            }
        });

        // 자기소개 텍스트 뷰
        userIntroduce = (EditText) findViewById(R.id.activity_profilechange_edit_introduce);
        if (!loginUser.getIntroduce().equals(""))
            userIntroduce.setText(loginUser.getIntroduce());

        // 유저 데이터 수정완료
        userChangeComplete = (Button) findViewById(R.id.activity_profilechange_button_complete);
        userChangeComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final MyProgressDialog myProgressDialog = new MyProgressDialog(ProfileChangeActivity.this, "잠시만 기다려주세요...");

                // 변경된 유저 이름 설정하기
                String name = userName.getText().toString();
                if (!name.equals(""))
                    loginUser.setUserName(name);

                // 변경된 유저 소개 설정하기
                String introduce = userIntroduce.getText().toString();
                if (!introduce.equals(""))
                    loginUser.setIntroduce(introduce);

                if (userUri.toString().equals(loginUser.getUserUriString())) {
                    // 이미지가 변경되지 않으면 db에 유저 정보 저장하기
                    // db에 넣을 데이터 폼 만들기
                    Map<String, Object> dbMap = new HashMap<>();
                    ConvertData convertData = new ConvertData();
                    String usertoJson = convertData.userToJson(loginUser);
                    dbMap.put(loginUser.getEmail(), convertData.userToJson(loginUser));

                    // db에 바뀐 유저 정보 저장하기
                    DocumentReference usersDoc = db.collection("threeline").document("users");
                    usersDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            myProgressDialog.dismiss();
                            // 바뀐 유저 정보 저장하기 ( db저장되면 저장하기 )
                            UserSharedPreference userSharedPreference = new UserSharedPreference(getSharedPreferences("users", MODE_PRIVATE));
                            userSharedPreference.saveUserData(loginUser);

                            // 로그인한 유저 정보 변경하기 ( db저장되면 저장하기 )
                            loginUserSharedPreference.setLoginUser(loginUser);
                            Intent resultIntent = new Intent();
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // db에 유저json 저장 실패
                            myProgressDialog.dismiss();
                            Toast.makeText(ProfileChangeActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                        }
                    }); // userDoc.update()
                } else {
                    // db에 이미지 저장하기
                    final StorageReference usersRef = storageRef.child("users").child(loginUser.getEmail());
                    usersRef.putFile(userUri)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) { // 이미지 uri 업로드하기
                                    // db에 올라갔으면 올라간 uri 얻기
                                    usersRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() { // 이미지 uri 다운로드하기
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            // db에 이미지 저장되었으면 userUri 변경해주기
                                            loginUser.setUserUriString(uri.toString());

                                            // db에 넣을 데이터 폼 만들기
                                            Map<String, Object> dbMap = new HashMap<>();
                                            ConvertData convertData = new ConvertData();
                                            String usertoJson = convertData.userToJson(loginUser);
                                            dbMap.put(loginUser.getEmail(), convertData.userToJson(loginUser));

                                            // db에 바뀐 유저 정보 저장하기
                                            DocumentReference usersDoc = db.collection("threeline").document("users");
                                            usersDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    myProgressDialog.dismiss();
                                                    // 바뀐 유저 정보 저장하기 ( db저장되면 저장하기 )
                                                    UserSharedPreference userSharedPreference = new UserSharedPreference(getSharedPreferences("users", MODE_PRIVATE));
                                                    userSharedPreference.saveUserData(loginUser);

                                                    // 로그인한 유저 정보 변경하기 ( db저장되면 저장하기 )
                                                    loginUserSharedPreference.setLoginUser(loginUser);
                                                    Intent resultIntent = new Intent();
                                                    setResult(RESULT_OK, resultIntent);
                                                    finish();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // db에 유저json 저장 실패
                                                    myProgressDialog.dismiss();
                                                    Toast.makeText(ProfileChangeActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            }); // userDoc.update()
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 이미지 url 다운로드 실패
                                            myProgressDialog.dismiss();
                                            Toast.makeText(ProfileChangeActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }).
                            addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // 이미지 uri 업로드 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(ProfileChangeActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });


        // 뒤로가기 이미지 설정
        backImage = (ImageView) findViewById(R.id.activity_profilechange_back);
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
                userUri = data.getData();
                userImage.setImageURI(userUri);
                break;
        }
    }

    private void uploadImage() {
        if (userUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageRef.child("images/test1");
            ref.putFile(userUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileChangeActivity.this, "Upload", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(ProfileChangeActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded" + (int) progress);
                        }
                    });
        }
    }
}
