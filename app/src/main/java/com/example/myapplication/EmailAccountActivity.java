package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.AutoLoginSharedPreference;
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class EmailAccountActivity extends AppCompatActivity {

    private static final String TAG = "EmailAccountActivity";

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    // 파이어 베이스 DB 객체 생성
    private FirebaseFirestore db;

    private EditText emailEdit; // 이메일 입력 뷰
    private EditText password1Edit; // 비밀번호 입력 뷰
    private EditText password2Edit; // 비밀번호 입력 확인 뷰
    private Button emailAccountComplete; // 생성완료 버튼
    private Button emailLogin;
    private ImageView backImage; // 뒤로가기
    private CheckBox autoCheck; // 자동 로그인

    private SharedPreferences sharedPreferences;
    private LoginUserSharedPreference loginUserSharedPreference; // 로그인한 유저 정보 관리

    private String email; // 입력한 이메일
    private String password1; // 입력한 비밀번호
    private String password2; // 입력한 비밀번호 확인

    private ArrayList<User> dbAllUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_account);

        sharedPreferences = getSharedPreferences("users", MODE_PRIVATE);

        emailEdit = (EditText) findViewById(R.id.activity_email_account_email);
        emailEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidEmail(String.valueOf(s))) {
                    emailEdit.setBackgroundColor(Color.argb(60, 0, 200, 0));
                } else {
                    emailEdit.setBackgroundColor(Color.argb(60, 200, 0, 0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password1Edit = (EditText) findViewById(R.id.activity_email_account_password1);
        password1Edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidPassword(String.valueOf(s))) {
                    password1Edit.setBackgroundColor(Color.argb(60, 0, 200, 0));
                } else {
                    password1Edit.setBackgroundColor(Color.argb(60, 200, 0, 0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password2Edit = (EditText) findViewById(R.id.activity_email_account_password2);
        password2Edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isValidPassword(String.valueOf(s))) {
                    password2Edit.setBackgroundColor(Color.argb(60, 0, 200, 0));
                } else {
                    password2Edit.setBackgroundColor(Color.argb(60, 200, 0, 0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        autoCheck = findViewById(R.id.activity_email_account_auto);

        emailAccountComplete = (Button) findViewById(R.id.activity_emailaccount_complete);
        // 이메일 계정 생성하기
        emailAccountComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 회원 가입하기
                final ProgressDialog progressDialog = new ProgressDialog(EmailAccountActivity.this);
                progressDialog.setTitle("잠시만 기다려주세요...");
                progressDialog.show();

                dbAllUsers = new ArrayList<>();

                // 입력한 데이터
                email = emailEdit.getText().toString();
                password1 = password1Edit.getText().toString();
                password2 = password2Edit.getText().toString();

                // 이메일 입력안했을시
                if (email.equals("")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식이 아닐시
                if (!isValidEmail(email)) {
                    Toast.makeText(getApplicationContext(), "이메일을 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 입력안했을시
                if (password1.equals("") && password2.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 형식이 틀릴시
                if (!isValidPassword(password1)) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 다시 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호가 같지 않으면 넘어가지 않음
                if (!password1.equals(password2)) {
                    Toast.makeText(getApplicationContext(), "비밀번호가 같지 않습니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 유저 객체 생성
                ConvertData convertData = new ConvertData();
                final User signUpUser = new User(convertData.dotToUnderscore(email), password1);

                // db에 유저 데이터 레퍼런스 참조 생성
                DocumentReference usersDoc = db.collection("threeline").document("users");
                // db에 있는 데이터 불러와서 있는지 확인하기
                usersDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            /*ConvertData convertData = new ConvertData(); // 데이터 변환해주는 클래스
                            이렇게 리스트에 모두 추가해서 검사해도 되지만 Map형식이기에 key값을 주고 데이터가 있는지 확인할 수 있다
                            for (Map.Entry<String, ?> entry : task.getResult().getData().entrySet()) {
                                User dbUser = convertData.jsonToUser(entry.getValue().toString());
                                dbAllUsers.add(dbUser);
                            }*/
//                            DocumentSnapshot document = task.getResult();
                            // 가입한 유저인지 검증하기
                            Map<String, Object> dbMap = task.getResult().getData(); // db 데이터 얻기
                            if (dbMap != null) { // 사용을 한번도 안한경우 가져올 데이터가 없어 dbMap에 들어올 값이 없어 null이 된다
                                Object dbValue = dbMap.get(signUpUser.getEmail()); // Map형식이기에 이메일 key값으로 데이터 있는지 확인하기
                                if (dbValue != null) {
                                    // 데이터가 있다면 이미 가입된 유저
                                    Toast.makeText(getApplicationContext(), "계정이 이미 있습니다", Toast.LENGTH_SHORT).show();
                                    return; // 가입 실패
                                }
                            } else {
                                dbMap = new HashMap<>();
                            }
                            // db에 넣을 데이터 폼 만들기
//                            Map<String, Object> map = new HashMap<>();
                            ConvertData convertData = new ConvertData(); // 데이터 변환해주는 클래스
                            dbMap.put(signUpUser.getEmail(), convertData.userToJson(signUpUser)); // 키:email - 밸류:유저json데이터

                            // 디비에 저장하기
                            db.collection("threeline").document("users").set(dbMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // 유저 정보 db에 등록 완료
                                            // 없는 계정일 경우 shared에 저장이 되며 있으면 실패합니다 ( 디비에 들어가면 저장하자 )
                                            UserSharedPreference userSharedPreference = new UserSharedPreference(sharedPreferences);
                                            userSharedPreference.saveUserData(signUpUser);

                                            // 로그인 하는 유저의 데이터 shared 저장하기 ( 디비에 들어가면 저장하자 )
                                            loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
                                            loginUserSharedPreference.clearUser();
                                            loginUserSharedPreference.setLoginUser(signUpUser);

                                            // 자동 로그인 체크했으면 자동로그인 데이터 shared에 넣어주기 ( 디비에 들어가면 저장하자 )
                                            if (autoCheck.isChecked()) {
                                                AutoLoginSharedPreference autoLoginSharedPreference = new AutoLoginSharedPreference(getSharedPreferences("auto", MODE_PRIVATE));
                                                autoLoginSharedPreference.setAutoUser(signUpUser);
                                            }

                                            // 액티비티 이동하기 ( 디비에 들어갔으면 이동하기 )
                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                            intent.putExtra("useremail", signUpUser.getEmail());
                                            Toast.makeText(getApplicationContext(), "세줄일기 가입을 축하합니다", Toast.LENGTH_SHORT).show();
                                            startActivity(intent);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // 유저 정보 db에 등록 실패
                                            progressDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                });
            }
        });

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_emailaccount_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 파이어 베이스 DB 객체 선언
        initFirestore();

    }


    // 이메일 유효성 검사
    private boolean isValidEmail(String email) {
        if (email.isEmpty()) {
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            // 이메일 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    // 비밀번호 유효성 검사
    private boolean isValidPassword(String password) {
        if (password.isEmpty()) {
            return false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            // 비밀번호 형식 불일치
            return false;
        } else {
            return true;
        }
    }

    private void initFirestore() {
        if (db == null)
            db = FirebaseFirestore.getInstance();
    }
}
