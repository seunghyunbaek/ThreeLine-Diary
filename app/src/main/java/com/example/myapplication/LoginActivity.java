package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.AutoLoginSharedPreference;
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.example.myapplication.com.example.myapplication.data.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final int RC_SIGN_IN = 10;
    private static final String TAG = "LoginActivity";
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth firebaseAuth;

    private FirebaseFirestore db;


    private Button activityLoginButtonWithEmail;
    private Button LoginButtonWithFaceBook;
    private Button LoginButtonWithGoogle;


    private MyProgressDialog myProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        Intent receivedIntent = getIntent();

        if (db == null)
            db = FirebaseFirestore.getInstance();

        if (receivedIntent.getExtras().getBoolean("state", false)) {
            Intent loading = new Intent(getApplicationContext(), IntroActivity.class);
            startActivity(loading);
        }

        // 구글로 로그인하기
        LoginButtonWithGoogle = (Button) findViewById(R.id.activity_login_button_with_google);
        LoginButtonWithGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myProgressDialog = new MyProgressDialog(LoginActivity.this, "잠시만 기다려주세요...");
                SignIn();
            }
        });

        // 페이스북으로 로그인하기
        LoginButtonWithFaceBook = findViewById(R.id.activity_login_button_with_facebook);
        LoginButtonWithFaceBook.setVisibility(View.GONE);

        // 이메일로 로그인하기
        activityLoginButtonWithEmail = (Button) findViewById(R.id.activity_login_button_with_email);
        activityLoginButtonWithEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), EmailLoginActivity.class);
                startActivity(intent);
            }
        });

        // 파이어베이스 인증 객체 선언
        firebaseAuth = FirebaseAuth.getInstance();


        //로그인 시도할 액티비티에서 유저데이터 요청
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1003760306941-e2svtn5cditrrpg24jp49sfaa6cius2b.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

    }


    @Override
    protected void onStart() {
        super.onStart();
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(exampleBroadcastReceiver, filter);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) { // 만약 로그인이 되어있으면 다음 액티비티 실행
            Intent intent = new Intent(getApplicationContext(), EmailLoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onStop() {
//        unregisterReceiver(exampleBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        ActivityCompat.finishAffinity(this);
        super.onDestroy();
    }

    private void SignIn() {
        //이벤트 발생했을때, 구글로그인 버튼에 대한 (구글정보를 인텐트로 넘기는 값)

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            System.out.println("===================================================================");
            Log.d(TAG, "이름 =" + account.getDisplayName());
            Log.d(TAG, "이메일=" + account.getEmail());
            Log.d(TAG, "getId()=" + account.getId());
            Log.d(TAG, "getAccount()=" + account.getAccount());
            Log.d(TAG, "getIdToken()=" + account.getIdToken());
            System.out.println("===================================================================");
            // Signed in successfully, show authenticated UI.
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            System.out.println("====================" + e.getMessage() + "===========================");
            e.printStackTrace();
        }
    }


    //구글 파이어베이스로 값넘기기
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //파이어베이스로 받은 구글사용자가 확인된 이용자의 값을 토큰으로 받고
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);

        final GoogleSignInAccount account = acct;

        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            System.out.println("#############################################################################################");
                            System.out.println(account.getEmail().replace(".", "_"));
                            System.out.println("#############################################################################################");

                            final User user = new User(account.getEmail().replace(".", "_"), "");

                            /*Map<String, Object> dbMap = new HashMap<>();
                            final User user = new User(account.getEmail().replace(".", "_"), "");
                            ConvertData convertData = new ConvertData();
                            dbMap.put(user.getEmail(), convertData.userToJson(user));*/
                            // 구글 로그인 사용자가 db에 들어있는지 확인하기
                            final DocumentReference usersDoc = db.collection("threeline").document("users");
                            usersDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        // db에 유저 데이터 불러오기
                                        Map<String, Object> dbMap = task.getResult().getData();
                                        if (dbMap == null) { // 디비에 아무 데이터가 없을때 ( 가입한 유저가 아무도 없다 )
                                            dbMap = new HashMap<>();
                                            ConvertData convertData = new ConvertData();
                                            dbMap.put(user.getEmail(), convertData.userToJson(user));
                                            usersDoc.update(dbMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        myProgressDialog.dismiss();
                                                        // 다음 번부터는 자동로그인이 되도록 설정
                                                        AutoLoginSharedPreference autoLoginSharedPreference = new AutoLoginSharedPreference(getSharedPreferences("auto", MODE_PRIVATE));
                                                        autoLoginSharedPreference.setAutoUser(user);

                                                        // 로그인 하는 유저의 데이터 저장하기 // LoginShared에 넣기 ( 디비에서 불러와 비밀번호 맞을시 확인하기 )
                                                        LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
                                                        loginUserSharedPreference.clearUser();
                                                        loginUserSharedPreference.setLoginUser(user);

                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        intent.putExtra("useremail", user.getEmail());
                                                        startActivity(intent);
                                                    } else {
                                                        myProgressDialog.dismiss();
                                                        Toast.makeText(LoginActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else { // 디비에 데이터가 있을 때
                                            // 가입했던 적이 있으면 값이 있다
                                            Object dbValue = dbMap.get(user.getEmail());
                                            if (dbValue != null) {
                                                myProgressDialog.dismiss();

                                                // 가입했던 아이디로 로그인하기
                                                ConvertData convertData = new ConvertData();
                                                User loginUser = convertData.jsonToUser(dbValue.toString());
                                                // 다음 번부터는 자동로그인이 되도록 설정
                                                AutoLoginSharedPreference autoLoginSharedPreference = new AutoLoginSharedPreference(getSharedPreferences("auto", MODE_PRIVATE));
                                                autoLoginSharedPreference.setAutoUser(loginUser);

                                                // 로그인 하는 유저의 데이터 저장하기 // LoginShared에 넣기 ( 디비에서 불러와 비밀번호 맞을시 확인하기 )
                                                LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
                                                loginUserSharedPreference.clearUser();
                                                loginUserSharedPreference.setLoginUser(loginUser);

                                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                intent.putExtra("useremail", loginUser.getEmail());
                                                startActivity(intent);
                                            } else {
                                                // 가입하기
                                                ConvertData convertData = new ConvertData();
                                                dbMap.put(user.getEmail(), convertData.userToJson(user));
                                                usersDoc.update(dbMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            myProgressDialog.dismiss();

                                                            // 다음 번부터는 자동로그인이 되도록 설정
                                                            AutoLoginSharedPreference autoLoginSharedPreference = new AutoLoginSharedPreference(getSharedPreferences("auto", MODE_PRIVATE));
                                                            autoLoginSharedPreference.setAutoUser(user);

                                                            // 로그인 하는 유저의 데이터 저장하기 // LoginShared에 넣기 ( 디비에서 불러와 비밀번호 맞을시 확인하기 )
                                                            LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
                                                            loginUserSharedPreference.clearUser();
                                                            loginUserSharedPreference.setLoginUser(user);

                                                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                            intent.putExtra("useremail", user.getEmail());
                                                            startActivity(intent);
                                                        } else {
                                                            myProgressDialog.dismiss();
                                                            Toast.makeText(LoginActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                            });
                        } else {
                            myProgressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "다시 시도해주세요...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
