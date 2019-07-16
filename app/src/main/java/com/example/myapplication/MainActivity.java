package com.example.myapplication;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    //    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
//    BroadcastReceiver mReceiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//            if(TextUtils.equals(action, VOLUME_CHANGED_ACTION)) {
//                Toast.makeText(MainActivity.this, "음량이 변화했습니다", Toast.LENGTH_SHORT).show();
//            }
//        }
//    };
//    ExampleBroadcastReceiver exampleBroadcastReceiver = new ExampleBroadcastReceiver();

    private static final String TAG = "MainActivity";
    private static final int WATCH_DIARY = 11;
    private ViewPager viewPager; // 프래그먼트를 보여줄 뷰 페이저
    private MainPageAdpater mainPageAdpater; // 뷰페이저 어댑터
    private int viewPagerMark; // 뷰페이저의 포지션 값 담는 변수
    private TextView tab_diaries_textView;
    private TextView tab_subscribe_textView;
    private TextView tab_write_textView;
    private TextView tab_profile_textView;
    private LinearLayout tab_diaries; // 사용자들의 일기책들 보는 탭
    private LinearLayout tab_subscribe; // 구독중인 일기책을 보는 탭
    private LinearLayout tab_write; // 일기 작성하기 탭
    private LinearLayout tab_profile; // 사용자의 프로필 탭
    private String useremail; // 로그인한 유저의 이메일

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onStart() {
        super.onStart();
//        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
//        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
//        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
//        registerReceiver(exampleBroadcastReceiver, filter);
        // 모든 유저 데이터 가져오기
        DocumentReference usersDoc = db.collection("threeline").document("users");
        usersDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> dbMap = task.getResult().getData();
                    if (dbMap == null)
                        return;
                    UserSharedPreference userSharedPreference = new UserSharedPreference(getSharedPreferences("users", MODE_PRIVATE));
                    userSharedPreference.clearUses();
                    for (Map.Entry<String, Object> entry : dbMap.entrySet()) {
                        userSharedPreference.saveMapData(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
        });
        // 모든 일기책 데이터 가져오기
        DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
        diarybooksDoc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> dbMap = task.getResult().getData();
                    if (dbMap == null)
                        return;
                    DiaryBookSharedPreference diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
                    diaryBookSharedPreference.clearDiaryBooks();
                    for (Map.Entry<String, Object> entry : dbMap.entrySet()) {
                        diaryBookSharedPreference.saveMapData(entry.getKey(), entry.getValue().toString());
                        mainPageAdpater.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    protected void onStop() {
//        unregisterReceiver(exampleBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_LOW);
//        registerReceiver(mReceiver, intentFilter);0
    }

    @Override
    protected void onPause() {
//        unregisterReceiver(mReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
        loginUserSharedPreference.clearUser();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent receivedIntent = getIntent();
        if (receivedIntent.getExtras().getBoolean("state", false)) {
            Intent loading = new Intent(getApplicationContext(), IntroActivity.class);
            loading.putExtra("state", false);
            startActivity(loading);
        }

        if (db == null)
            db = FirebaseFirestore.getInstance();
        if (storage == null)
            storage = FirebaseStorage.getInstance();
        if (storageRef == null)
            storageRef = storage.getReference();

        LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getSharedPreferences("login", MODE_PRIVATE));
        User loginUser = loginUserSharedPreference.getLoginUser();
        useremail = loginUser.getEmail(); // 로그인한 유저의 이메일
        // 사용자들의 일기책들 보는 탭
        tab_diaries = (LinearLayout) findViewById(R.id.activity_main_tab_diaries);
        tab_diaries_textView = (TextView) findViewById(R.id.activity_main_tab_diaries_text);
        // 구독중인 일기책 보는 탭
        tab_subscribe = (LinearLayout) findViewById(R.id.activity_main_tab_subscribe);
        tab_subscribe_textView = (TextView) findViewById(R.id.activity_main_tab_subscribe_text);
        // 일기 작성하는 탭
        tab_write = (LinearLayout) findViewById(R.id.activity_main_tab_write);
        tab_write_textView = (TextView) findViewById(R.id.activity_main_tab_write_text);
        // 사용자의 프로필 탭
        tab_profile = (LinearLayout) findViewById(R.id.activity_main_tab_profile);
        tab_profile_textView = (TextView) findViewById(R.id.activity_main_tab_profile_text);

        // 사용자들의 일기책들 보는 탭 클릭시
        tab_diaries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        // 구독중인 일기책들 보는 탭 클릭시
        tab_subscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
            }
        });
        // 일기 작성하기 탭 클릭시
        tab_write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 어떤 일기책을 고를지 선택하기
                Intent intent = new Intent(getApplicationContext(), SelectWriteDiaryActivity.class);
                intent.putExtra("useremail", useremail);
                startActivity(intent);
            }
        });
        // 사용자의 프로필 탭 클릭시
        tab_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

        viewPagerMark = 0;
        viewPager = (ViewPager) findViewById(R.id.viewPager);
//        viewPager.setOffscreenPageLimit(2); // 뷰페이저에서 2개까지 옆에 그려놓음
        mainPageAdpater = new MainPageAdpater(getSupportFragmentManager(), useremail); // 뷰페이저에 올라가는어댑터
        viewPager.setAdapter(mainPageAdpater);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                viewPagerMark = position;
                switch (position) {
                    case 0:
                        tab_diaries_textView.setVisibility(View.VISIBLE);
                        tab_subscribe_textView.setVisibility(View.GONE);
                        tab_write_textView.setVisibility(View.GONE);
                        tab_profile_textView.setVisibility(View.GONE);
                        break;
                    case 1:
                        tab_diaries_textView.setVisibility(View.GONE);
                        tab_subscribe_textView.setVisibility(View.VISIBLE);
                        tab_write_textView.setVisibility(View.GONE);
                        tab_profile_textView.setVisibility(View.GONE);
                        break;
                    case 2:
                        tab_diaries_textView.setVisibility(View.GONE);
                        tab_subscribe_textView.setVisibility(View.GONE);
                        tab_write_textView.setVisibility(View.GONE);
                        tab_profile_textView.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        // 사용자에게 권한을 얻기
//        tedPermission();

    }

    // 사용자에게 카메라와 갤러리 권한 얻기
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: ");
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case WATCH_DIARY: // 일기를 삭제했을 때
//
                Bundle receiveBundle = data.getExtras();
                String useremail = receiveBundle.getString("useremail");
                String diarybookkey = receiveBundle.getString("diarybookkey");

                Intent intent = new Intent(getApplicationContext(), WatchDiaryActivity.class);
                intent.putExtra("useremail", useremail);
                intent.putExtra("diarybookkey", diarybookkey);
                startActivity(intent);
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (viewPagerMark == 0) {
            ActivityCompat.finishAffinity(this);
        } else {
            viewPager.setCurrentItem(0);
        }
    }

}
