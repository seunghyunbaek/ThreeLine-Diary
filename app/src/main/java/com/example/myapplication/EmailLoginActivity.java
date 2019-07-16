package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;
import java.util.regex.Pattern;

public class EmailLoginActivity extends AppCompatActivity {
    // 비밀번호 정규식
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9!@.#$%^&*?_~]{4,16}$");

    // 파이어 베이스 인증 객체 생성\
//    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db;

    private EditText emailEdit;
    private EditText passwordEdit;

    private Button emailLoginComplete;
    private Button emailLoginAccount;
    private ImageView backImage;
    private CheckBox autoCheck;
    private String email;
    private String password;

//    private User signedUser; // 회원가입한유저의 데이터 가져오기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_login);

        email = "";
        password = "";

        emailEdit = (EditText) findViewById(R.id.activity_email_login_emailEdit);
        passwordEdit = (EditText) findViewById(R.id.activity_email_login_passwordEdit);
        autoCheck = findViewById(R.id.activity_email_login_auto);

        // 로그인 버튼
        emailLoginComplete = (Button) findViewById(R.id.activity_emaillogin_complete_btn);
        emailLoginComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 로그인하기
                final ProgressDialog progressDialog = new ProgressDialog(EmailLoginActivity.this);
                progressDialog.setTitle("잠시만 기다려주세요...");
                progressDialog.show();

                email = emailEdit.getText().toString(); // 입력한 이메일
                password = passwordEdit.getText().toString(); // 입력한 비밀번호

                // 이메일을 입력하지 않으면 로그인 실패
                if (email.equals("")) {
                    Toast.makeText(getApplicationContext(), "이메일을 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호를 입력하지 않으면 로그인 실패
                if (password.equals("")) {
                    Toast.makeText(getApplicationContext(), "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // db에 유저 데이터 레퍼런스 참조 생성
                DocumentReference usersDoc = db.collection("threeline").document("users");
                // db에 있는 유저 데이터 불러와서 가입한 유저인지 확인하기
                usersDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();

                            // db에 유저 데이터 불러오기
                            Map<String, Object> dbMap = task.getResult().getData();
                            if (dbMap == null) { // 디비에 아무 데이터가 없을때 ( 가입한 유저가 아무도 없다 )
                                Toast.makeText(EmailLoginActivity.this, "계정이 없습니다", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // db에 유저 데이터 있는지 확인하기
                            ConvertData convertData = new ConvertData();
                            Object dbValue = dbMap.get(convertData.dotToUnderscore(email));
                            if (dbValue == null) {
                                Toast.makeText(getApplicationContext(), "계정이 없습니다", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // db에 유저 데이터가 있으면 유저데이터로 변환하기
                            User signedUser = convertData.jsonToUser(dbValue.toString());

                            // 비밀번호가 맞는지 확인하기
                            if (!signedUser.chekPassword(password)) {
                                Toast.makeText(EmailLoginActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // 자동 로그인체크했는지 확인하기 ( 디비에서 불러와 비밀번호 맞을시 확인하기 )
                            if (autoCheck.isChecked()) {
                                AutoLoginSharedPreference autoLoginSharedPreference = new AutoLoginSharedPreference(getSharedPreferences("auto", MODE_PRIVATE));
                                autoLoginSharedPreference.setAutoUser(signedUser);
                            }

                            // 로그인 하는 유저의 데이터 저장하기 // LoginShared에 넣기 ( 디비에서 불러와 비밀번호 맞을시 확인하기 )
                            LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
                            loginUserSharedPreference.clearUser();
                            loginUserSharedPreference.setLoginUser(signedUser);

                            // 디비에서 불러와 비밀번호 맞고 셰어드에 모두 저장하고 실행하기
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra("useremail", signedUser.getEmail());
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        // 회원가입 버튼
        emailLoginAccount = findViewById(R.id.activity_emaillogin_account);
        emailLoginAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EmailAccountActivity.class);
                startActivity(intent);
            }
        });

        // 뒤로가기 버튼
        backImage = (ImageView) findViewById(R.id.activity_emaillogin_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        initFirestore(); // 파이어베이스 객체 초기화
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    // 이메일 유효성 검사
    private boolean isValidEmail() {
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
    private boolean isValidPassword() {
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
