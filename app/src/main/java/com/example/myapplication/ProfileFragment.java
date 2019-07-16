package com.example.myapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.AutoLoginSharedPreference;
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.LoginUserSharedPreference;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;


public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private static final int COMPLETE_SETTING = 1; // 유저 프로필 변경
    private static final int MAKE_SOLO_DIARY = 2; // 혼자쓰는 일기 만들기
    private static final int EDIT_OWN_DIARY = 3; // 혼자쓰는 일기 수정하기
    private static final int MAKE_JOIN_DIARY = 4; // 같이쓰는 일기 만들기
    private static final int EDIT_JOIN_DIARY = 5; // 같이쓰는 일기 만들기
    private static final int WATCH_DIARY = 11;

    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private Button userSignOut; // 유저 탈퇴하기
    private TextView userNameTextView; // 유저 이름 텍스트 뷰
    private ImageView userImageView; // 유저 이미지 뷰
    private TextView userIntroduceTextView; // 유저 소개글 뷰

    private Button userChangeButton; // 유저 프로필 변경 버튼
    private Button userLogoutButton; // 유저 로그아웃 버튼

    private LinearLayout makeAloneDiary; // 혼자 쓰는 일기 만들기 버튼

    private RecyclerView ownDiaryBookRecyclerView; // 혼자쓰는 일기 리사이클러 뷰
    private OwnDiaryBookAdapter ownDiaryBookAdapter; // 혼자쓰는 일기 어댑터
    private RecyclerView.LayoutManager ownDiaryBookLayoutManager; // 혼자쓰는 일기 레이아웃 매니저

    private LinearLayout makeJoinDiary; // 같이 쓰는 일기 만들기 버튼
    private RecyclerView joinDiaryBookRecyclerView; // 같이쓰는 일기 리사이클러뷰
    private JoinDiaryBookAdapter joinDiaryBookAdapter; // 같이쓰는 일기 어댑터
    private RecyclerView.LayoutManager joinDiaryBookLayoutManager; // 같이쓰는 일기 레이아웃 매니저

    private String useremail; // 유저 이메일 데이터
    private User loginUser; // 로그인한 유저
    private LoginUserSharedPreference loginUserSharedPreference;
    private UserSharedPreference userSharedPreference; // 유저 데이터 관리
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private ArrayList<DiaryBook> userAloneDiaryBooks; // 혼자쓰는 일기책
    private ArrayList<DiaryBook> userJoinDiaryBooks; // 같이쓰는 일기책


    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (db == null)
            db = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();


        // 사용중인 유저 이메일 얻기 ( 로그인한 유저 데이터 가져오기 )
        loginUserSharedPreference = new LoginUserSharedPreference(getContext().getSharedPreferences("login", Context.MODE_PRIVATE));
        loginUser = loginUserSharedPreference.getLoginUser(); // 로그인한 유저데이터 얻기
        useremail = loginUser.getEmail();
//        useremail = getArguments().getString("useremail");

        // 사용중인 유저 데이터 얻기
        userSharedPreference = new UserSharedPreference(getContext().getSharedPreferences("users", Context.MODE_PRIVATE));
        diaryBookSharedPreference = new DiaryBookSharedPreference(getContext().getSharedPreferences("diarybooks", Context.MODE_PRIVATE));
        userAloneDiaryBooks = diaryBookSharedPreference.userAloneDiaryBook(loginUser.getEmail()); // 유저의 혼자쓰는 일기책 데이터 얻기
        userJoinDiaryBooks = diaryBookSharedPreference.userJoinDiaryBook(loginUser.getEmail()); // 유저의 혼자쓰는 일기책 데이터 얻기

        // 유저 이름 세팅하기
        userNameTextView = (TextView) view.findViewById(R.id.fragment_profile_textview_name);
        userNameTextView.setText(loginUser.getUserName());

        // 유저 이미지 세팅하기
        userImageView = (ImageView) view.findViewById(R.id.fragment_profile_profileimage);
//        if (loginUser.getUserUri() != null) userImageView.setImageURI(loginUser.getUserUri());
        if (loginUser.getUserUri() != null) {
            Glide.with(this)
                    .load(loginUser.getUserUri())
                    .into(userImageView);
        }

        userSignOut = view.findViewById(R.id.fragment_profile_sign_out);
        userSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
            }
        });

        // 유저 자기소개 세팅하기
        userIntroduceTextView = (TextView) view.findViewById(R.id.fragment_profile_introduce);
        if (!loginUser.getIntroduce().equals(""))
            userIntroduceTextView.setText(loginUser.getIntroduce());

        // 유저 정보 변경하기
        userChangeButton = (Button) view.findViewById(R.id.btn_profile_change);
        userChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileChangeActivity.class);
                startActivityForResult(intent, COMPLETE_SETTING);
            }
        });

        // 유저 로그아웃하기
        userLogoutButton = view.findViewById(R.id.activity_profile_logout);
        userLogoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (loginUser.isLoginWithGoogle())
                    FirebaseAuth.getInstance().signOut();

                // 자동 로그인 데이터 삭제
                AutoLoginSharedPreference autoLoginSharedPreference = new AutoLoginSharedPreference(getContext().getSharedPreferences("auto", Context.MODE_PRIVATE));
                if (autoLoginSharedPreference.isUser()) // 자동로그인을 체크하고 들어온 유저인지 확인
                    autoLoginSharedPreference.clearData();

                // 로그인한 유저의 데이터 삭제
                LoginUserSharedPreference loginUserSharedPreference = new LoginUserSharedPreference(getContext().getSharedPreferences("login", Context.MODE_PRIVATE));
                loginUserSharedPreference.clearUser();

                // 로그인 화면으로 이동하기
                Intent intent = new Intent(getContext(), LoginActivity.class);
                intent.putExtra("state", false);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);

            }
        });

        // 혼자쓰는 일기책 만들기
        makeAloneDiary = (LinearLayout) view.findViewById(R.id.fragment_profile_make_alone);
        makeAloneDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 혼자쓰는 일기책 만들기
                Intent intent = new Intent(getContext(), NewDiaryBookActivity.class);
                intent.putExtra("useremail", useremail);
                startActivityForResult(intent, MAKE_SOLO_DIARY);
            }
        });

        // 혼자 쓰는 일기책 리사이클러 뷰
        ownDiaryBookRecyclerView = view.findViewById(R.id.fragment_profile_solodiary_recyclerview);
        // 혼자 쓰는 일기책 레이아웃 매니저
        ownDiaryBookLayoutManager = new LinearLayoutManager(view.getContext());
        ownDiaryBookRecyclerView.setLayoutManager(ownDiaryBookLayoutManager);
        // 혼자 쓰는 일기책 어댑터
        ownDiaryBookAdapter = new OwnDiaryBookAdapter(view.getContext(), getContext(), userAloneDiaryBooks, diaryBookSharedPreference, useremail);
        ownDiaryBookRecyclerView.setAdapter(ownDiaryBookAdapter);

        // 같이쓰는 일기책 만들기
        makeJoinDiary = (LinearLayout) view.findViewById(R.id.fragment_profile_make_together);
        makeJoinDiary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FindFriendActivity.class);
                intent.putExtra("useremail", useremail);
                startActivityForResult(intent, MAKE_JOIN_DIARY);
            }
        });

        // 같이쓰는 일기책 리사이클러뷰
        joinDiaryBookRecyclerView = view.findViewById(R.id.fragment_profile_duodiary_recyclerview);
        // 같이쓰는 일기책 레이아웃 매니저
        joinDiaryBookLayoutManager = new LinearLayoutManager(view.getContext());
        joinDiaryBookRecyclerView.setLayoutManager(joinDiaryBookLayoutManager);
        // 같이쓰는 일기책 어댑터
        joinDiaryBookAdapter = new JoinDiaryBookAdapter(view.getContext(), userJoinDiaryBooks, diaryBookSharedPreference, useremail);
        joinDiaryBookRecyclerView.setAdapter(joinDiaryBookAdapter);

        //로그인 시도할 액티비티에서 유저데이터 요청
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1003760306941-e2svtn5cditrrpg24jp49sfaa6cius2b.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(view.getContext(), gso);

        return view;
    }

    private void signOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                    Toast.makeText(getContext(), "sign out", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        userAloneDiaryBooks = diaryBookSharedPreference.userAloneDiaryBook(loginUser.getEmail());
        ownDiaryBookAdapter.setUserAloneDiaryBooks(userAloneDiaryBooks);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult: ");

        if (resultCode != getActivity().RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case COMPLETE_SETTING: // 프로필 변경 완료
                // 유저 프로필 변경된 데이터로 변경하기
                loginUser = loginUserSharedPreference.getLoginUser();
                // 변경된 이름 설정
                userNameTextView.setText(loginUser.getUserName());
                // 변경된 자기소개 설정
                if (!loginUser.getIntroduce().equals(""))
                    userIntroduceTextView.setText(loginUser.getIntroduce());
                // 변경된 uri 설정
                if (loginUser.getUserUri() != null) {
                    Glide.with(this)
                            .load(loginUser.getUserUri())
                            .into(userImageView);
                }
                break;
            case MAKE_SOLO_DIARY:
                UploadDiaryBook uploadDiaryBook = new UploadDiaryBook();
                uploadDiaryBook.execute();
                // 혼자쓰는 일기책 만들고 난 뒤 데이터 적용하기
                userAloneDiaryBooks = diaryBookSharedPreference.userAloneDiaryBook(loginUser.getEmail());
                ownDiaryBookAdapter.setUserAloneDiaryBooks(userAloneDiaryBooks);
                break;
            case EDIT_OWN_DIARY:
                // 혼자쓰는 일기 수정하고 난 뒤 데이터 적용하기
                userAloneDiaryBooks = diaryBookSharedPreference.userAloneDiaryBook(loginUser.getEmail());
                ownDiaryBookAdapter.setUserAloneDiaryBooks(userAloneDiaryBooks);
                break;
            case MAKE_JOIN_DIARY:
                UploadDiaryBook uploadDiaryBook2 = new UploadDiaryBook();
                uploadDiaryBook2.execute();
                // 같이쓰는 일기 만들고 난 뒤 데이터 적용하기
                userJoinDiaryBooks = diaryBookSharedPreference.userJoinDiaryBook(loginUser.getEmail());
                joinDiaryBookAdapter.setUserJoinDiaryBooks(userJoinDiaryBooks);
                break;
            case EDIT_JOIN_DIARY:
                // 같이쓰는 일기 수정하고 난 뒤 데이터 적용하기
                userJoinDiaryBooks = diaryBookSharedPreference.userJoinDiaryBook(loginUser.getEmail());
                joinDiaryBookAdapter.setUserJoinDiaryBooks(userJoinDiaryBooks);
                break;
            case WATCH_DIARY:
                Bundle receiveBundle = data.getExtras();
                String useremail = receiveBundle.getString("useremail");
                String diarybookkey = receiveBundle.getString("diarybookkey");

                Intent intent = new Intent(getContext(), WatchDiaryActivity.class);
                intent.putExtra("useremail", useremail);
                intent.putExtra("diarybookkey", diarybookkey);
                startActivity(intent);
                break;
        }
    }

    ArrayList<DiaryBook> loadAllDiaryBooks;
    ArrayList<DiaryBook> userDiaryBooks;
    ArrayList<DiaryBook> dbUserAloneDiaryBooks;

    // 모든 일기책 데이터 불러오기
    public ArrayList<DiaryBook> loadDBAllDiaryBooks() {
        loadAllDiaryBooks = new ArrayList<>();
        userDiaryBooks = new ArrayList<>();
        dbUserAloneDiaryBooks = new ArrayList<>();

        db.collection("diarybooks").document("diarybooks").get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        for (Map.Entry<String, ?> entry : documentSnapshot.getData().entrySet()) {

                        }
                    }
                });

        db.collection("diarybooks").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // 모든 일기책 가져오기
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ConvertData convertData = new ConvertData();
                                for (Map.Entry<String, ?> entry : document.getData().entrySet()) {
                                    DiaryBook diaryBook = convertData.jsonToDiarybook(entry.getValue().toString());
                                    loadAllDiaryBooks.add(diaryBook);
                                }
                            }

                            Log.d(TAG, "onComplete: " + loadAllDiaryBooks.size());
                            // 로그인한 유저의 모든 일기책
                            for (DiaryBook diaryBook : loadAllDiaryBooks) {
                                if (diaryBook.isUserDiaryBook(loginUser.getEmail())) {
                                    userDiaryBooks.add(diaryBook);
                                }
                            }

                            // 혼자쓰는 일기책들
                            for (DiaryBook diaryBook : userDiaryBooks) {
                                if (diaryBook.isAloneDIaryBook())
                                    dbUserAloneDiaryBooks.add(diaryBook);
                            }
                            userAloneDiaryBooks = dbUserAloneDiaryBooks;
                        }
                    }
                });

        return loadAllDiaryBooks;
    }

    public ArrayList<DiaryBook> userDiaryBooks() {
        ArrayList<DiaryBook> userDiaryBooks = new ArrayList<>();
        ArrayList<DiaryBook> allDiarybooks = loadDBAllDiaryBooks();

        for (DiaryBook diaryBook : allDiarybooks) {
            if (diaryBook.isUserDiaryBook(loginUser.getEmail()))
                userDiaryBooks.add(diaryBook);
        }

        return userDiaryBooks;
    }

    class UploadDiaryBook extends AsyncTask<Void, Void, Void> {

        ProgressDialog asyncDialog = new ProgressDialog(ProfileFragment.this.getContext());

        @Override
        protected void onPreExecute() {
//            asyncDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            asyncDialog.setMessage("일기책을 생성중입니다...");

            asyncDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            int sum = 0;

            try {
                for (int i = 0; sum < 100; i++) {
                    sum = sum + (int) (Math.random() * 10);
                    asyncDialog.setProgress(sum);
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            asyncDialog.dismiss();
            super.onPostExecute(aVoid);
        }
    }

}
