package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.Diary;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WatchDiaryActivity extends AppCompatActivity {

    private static final int DIARY_EDIT_COMPLETE = 9;

    private ViewPager viewPager;
    private WatchDiaryAdapter watchDiaryAdapter;

    private ImageView diaryImageShare; // 이미지 공유하기
    private ImageView diaryImageComment; // 댓글 보러가기
    private TextView diaryCommentCount; // 댓글 개수
    private ImageView diaryLike; // 좋아요 누르기
    private TextView diaryLikeCount; // 좋아요 갯수
    private ImageView diarySubscribe; // 구독 하기
    private TextView diarySubscribeCount; // 구독 갯수
    private TextView diarySubscribeCount2;
    private ImageView backImage; // 뒤로가기
    private ImageView endImage; // 마지막으로가기

    private ImageView diaryCalendar; // 일기 특정 날짜로 이동하기
    private ImageView diaryEdit; // 일기 수정하기

    private int pageMark; // 일기의 인덱스를 알려줍니다

    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키 값
    private DiaryBook diaryBook; // 볼 일기책 데이터
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리

    private FragmentManager fragmentManager;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        Log.d("qwer", "WatchDiary onCreate() : 온크리에이트" + viewPager);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_diary);

        if (db == null) // db 인스턴스 생성
            db = FirebaseFirestore.getInstance();
        if (storage == null) // 스토리지 인스턴스 생성
            storage = FirebaseStorage.getInstance();
        if (storageRef == null) // 스토리지 레퍼런스 참조 생성
            storageRef = storage.getReference();

        // 일기책 데이터 얻기
        Intent receiveIntent = getIntent();
        useremail = receiveIntent.getExtras().getString("useremail");
        diarybookkey = receiveIntent.getExtras().getString("diarybookkey");
        pageMark = receiveIntent.getExtras().getInt("pagemark", -1); // 일기 페이지
        diaryBookSharedPreference = new DiaryBookSharedPreference(getSharedPreferences("diarybooks", MODE_PRIVATE));
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 볼 일기책 데이터 가져오기
        Log.d("asdf", "생성-----------------------------------------------------------------" + pageMark);

        fragmentManager = getSupportFragmentManager();

        // 뷰페이저
        viewPager = findViewById(R.id.activity_watch_diary_viewPager);
        watchDiaryAdapter = new WatchDiaryAdapter(fragmentManager, useremail, diaryBook, diaryBookSharedPreference);
        viewPager.setAdapter(watchDiaryAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        pageMark = position - 1;
                        diarySubscribe.setVisibility(View.VISIBLE);
                        diarySubscribeCount.setVisibility(View.VISIBLE);
                        diarySubscribeCount2.setVisibility(View.VISIBLE);
                        diaryImageComment.setVisibility(View.GONE);
                        diaryCommentCount.setVisibility(View.GONE);
                        diaryLike.setVisibility(View.GONE);
                        diaryLikeCount.setVisibility(View.GONE);
                        diaryCalendar.setVisibility(View.GONE);
                        diaryEdit.setVisibility(View.GONE);
                        endImage.setVisibility(View.VISIBLE);
                        break;
                    default:
                        pageMark = position - 1;
                        diarySubscribe.setVisibility(View.GONE);
                        diarySubscribeCount.setVisibility(View.GONE);
                        diarySubscribeCount2.setVisibility(View.GONE);
                        diaryImageComment.setVisibility(View.VISIBLE);
                        diaryCommentCount.setVisibility(View.VISIBLE);
                        diaryLike.setVisibility(View.VISIBLE);
                        diaryLikeCount.setVisibility(View.VISIBLE);
                        diaryCalendar.setVisibility(View.VISIBLE);
                        endImage.setVisibility(View.VISIBLE);

                        // 좋아요 상태 보여주기
                        if (diaryBook.getDiaries().get(position - 1).isLikeDiary(useremail)) {
                            diaryLike.setImageResource(R.drawable.icon_like_on); // 좋아요 누른 일기라면 좋아요 이미지 세팅하기
                        } else {
                            diaryLike.setImageResource(R.drawable.icon_like_off); // 좋아요 안누른 일기는 일반 이미지 세팅하기
                        }
                        diaryLikeCount.setText(diaryBook.getDiaries().get(position - 1).getLikeUsersCount()); // 좋아요 갯수 설정하기
                        diaryCommentCount.setText(diaryBook.getDiaries().get(position - 1).getCommentCount()); // 댓글 갯수 설정하기

                        // 유저가 작성한 일기일 때 수정하기와 숨음 글보기메뉴가 나타납니다
                        if (diaryBook.getDiaries().get(position - 1).isUserDiary(useremail)) {
                            diaryEdit.setVisibility(View.VISIBLE);
                        } else {
                            diaryEdit.setVisibility(View.GONE);
                        }
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {
                if (i == ViewPager.SCROLL_STATE_SETTLING) {

                }
            }
        });

        // 댓글로 이동하기
        diaryImageComment = (ImageView) findViewById(R.id.activity_watchdiary_comment);
        diaryImageComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // 댓글 클릭시 댓글 보러 가기
                Intent intent = new Intent(WatchDiaryActivity.this, CommentActivity.class);
                intent.putExtra("useremail", useremail);
                intent.putExtra("diarybookkey", diarybookkey);
                intent.putExtra("diaryposition", pageMark);
                startActivity(intent);
            }
        });

        // 일기책 구독
        diarySubscribe = (ImageView) findViewById(R.id.activity_watchdiary_subscribe);

        // 일기책 구독 눌렀는지 체크하기
        if (diaryBook.isSubscribeDiaryBook(useremail)) {
            diarySubscribe.setImageResource(R.drawable.icon_subscribes_on);
        } else {
            diarySubscribe.setImageResource(R.drawable.icon_subscribe_diary_off);
        }

        // 일기책 구독 누르기
        diarySubscribe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // 자신의 일기책이면 리턴합니다
                if (diaryBook.isOwnDiaryBook(useremail))
                    return;

                final MyProgressDialog myProgressDialog = new MyProgressDialog(WatchDiaryActivity.this, "잠시만 기다려주세요...");

                DiaryBook tempDiaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);

                // 일기책을 구독하지 않았으면 구독이 됩니다
                if (!tempDiaryBook.isSubscribeDiaryBook(useremail)) { // 구독중이지 않으면
                    tempDiaryBook.subscribeDiaryBook(useremail); // 일기책 구독자 명단에 추가됩니다
                    diarySubscribe.setImageResource(R.drawable.icon_subscribes_on); // 구독중인 이미지로 변경
                } else { // 일기책을 구독중이면 구독이 취소가 됩니다
                    tempDiaryBook.unSubscribeDiaryBook(useremail); // 일기책 구독자 명단에서 삭제됩니다
                    diarySubscribe.setImageResource(R.drawable.icon_subscribe_diary_off); // 구독안한 이미지로 변경
                }

                /*// 일기책을 구독하지 않았으면 구독이 됩니다
                if (!diaryBook.isSubscribeDiaryBook(useremail)) { // 구독중이지 않으면
                    diaryBook.subscribeDiaryBook(useremail); // 일기책 구독자 명단에 추가됩니다
                    diarySubscribe.setImageResource(R.drawable.icon_subscribes_on); // 구독중인 이미지로 변경
                } else { // 일기책을 구독중이면 구독이 취소가 됩니다
                    diaryBook.unSubscribeDiaryBook(useremail); // 일기책 구독자 명단에서 삭제됩니다
                    diarySubscribe.setImageResource(R.drawable.icon_subscribe_diary_off); // 구독안한 이미지로 변경
                }*/

                final DiaryBook changedDiaryBook = tempDiaryBook; // 데이터가 변한 일기책

                // 디비에 넣을 폼 만들기
                Map<String, Object> dbMap = new HashMap<>();
                ConvertData convertData = new ConvertData();
                dbMap.put(tempDiaryBook.getDiaryBookKey(), convertData.diarybooktoJson(tempDiaryBook));

                // 디비에 저장하기
                DocumentReference diarybookDoc = db.collection("threeline").document("diarybooks");
                diarybookDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 디비에 저장 성공
                        myProgressDialog.dismiss();
                        diaryBook = changedDiaryBook;
                        diarySubscribeCount.setText(String.valueOf(diaryBook.getSubscribeUsersCount())); // 변한 구독자 수 표시
                        diaryBookSharedPreference.saveDiaryBook(diaryBook); // 변경된 일기책 데이터 저장하기
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 디비에 저장 실패
                        myProgressDialog.dismiss();
                        Toast.makeText(WatchDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 일기책 구독자 수
        diarySubscribeCount = (TextView) findViewById(R.id.activity_watachdiary_subscribecount);
        // 일기책 구독자 수 설정하기
        diarySubscribeCount.setText(String.valueOf(diaryBook.getSubscribeUsersCount()));
        diarySubscribeCount2 = (TextView) findViewById(R.id.activity_watachdiary_subscribecount2);

        // 일기 좋아요 누르기
        diaryLike = (ImageView) findViewById(R.id.activity_watchdiary_like);
        // 일기 좋아요 갯수
        diaryLikeCount = (TextView) findViewById(R.id.activity_watchdiary_like_count);
        // 일기 좋아요 눌렀는지 체크하기
        diaryLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (pageMark < 0) // 일기화면인지 체크하기
                    return;

                final Diary diary = diaryBook.getDiaries().get(pageMark); // 현재 보고 있는 일기 가져오기

                // 나의 일기인지 확인하기
                if (diary.isUserDiary(useremail))
                    return; // 나의 일기이면 빠져나갑니다

                final MyProgressDialog myProgressDialog = new MyProgressDialog(WatchDiaryActivity.this, "잠시만 기다려주세요...");

                // 좋아요 눌렀는지 확인하기
                if (diary.isLikeDiary(useremail)) {
                    // 좋아요 에서 취소로 변경
                    diary.unLikeDiary(useremail);// 좋아요 누른 사람 목록에서 삭제됩니다
                    diaryLike.setImageResource(R.drawable.icon_like_off); // 좋아요 안한 이미지로 변경
                } else {
                    // 좋아요 상태로 변경
                    diary.likeDiary(useremail);// 좋아요 누른 사람 목로게 추가됩니다
                    diaryLike.setImageResource(R.drawable.icon_like_on); // 좋아요 이미지로 변경
                }

                diaryBook.setDiary(pageMark, diary); // 변한 일기데이터 저장하기

                final Diary changedDiary = diary;

                // 디비에 넣을 폼 만들기
                Map<String, Object> dbMap = new HashMap<>();
                ConvertData convertData = new ConvertData();
                dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));

                DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                diarybooksDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // 디비에 업로드 성공
                        myProgressDialog.dismiss();
                        diaryLikeCount.setText(changedDiary.getLikeUsersCount()); // 변경된 좋아요 사람 수 설정하기
                        diaryBookSharedPreference.saveDiaryBook(diaryBook); // 일기책 저장하기
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // 디비에 업로드 실패
                        myProgressDialog.dismiss();
                        Toast.makeText(WatchDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // 일기 댓글 갯수
        diaryCommentCount = (TextView) findViewById(R.id.watchDiaryCommentCount);

        final Calendar calendar = Calendar.getInstance();
        diaryCalendar = findViewById(R.id.activity_watchdiary_calendar);
        diaryCalendar.setVisibility(View.GONE);
        diaryCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(WatchDiaryActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
//                        String date = String.format("%d년 %02d월 %02d일", year, month + 1, dayOfMonth);
                        String date = String.format("%d%02d%02d", year, month + 1, dayOfMonth);
                        int diaryPosition = diaryBook.findDiaryPosition(date);
                        if (diaryPosition == -1) {
                            Toast.makeText(WatchDiaryActivity.this, "일기를 작성하지 않았습니다", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        viewPager.setCurrentItem(diaryPosition);
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));

                datePickerDialog.getDatePicker().setMaxDate(new Date().getTime()); // 입력한 날짜 이후 입력안되게
                datePickerDialog.show();
            }
        });

        // 일기 수정하기
        diaryEdit = findViewById(R.id.activity_watchdiary_diaryedit);
        diaryEdit.setVisibility(View.GONE);
        diaryEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // PopupMenu는 API 11레벨부터 제공합니다
                PopupMenu popupMenu = new PopupMenu(getApplicationContext(), v); // (현재 화면의 제어권자, 팝업을 띄울 기준될 위젯)
                getMenuInflater().inflate(R.menu.menu_diary_edit, popupMenu.getMenu());
                // 이벤트 처리
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getTitle().toString().equals("수정")) { // 수정 클릭시
                            Intent intent = new Intent(WatchDiaryActivity.this, WriteDiaryEditActivity.class); // 일기책 수정하러가기
                            intent.putExtra("useremail", useremail);
                            intent.putExtra("diarybookkey", diaryBook.getDiaryBookKey());
                            intent.putExtra("diaryposition", pageMark);
                            startActivity(intent);
                        } else { // 삭제 클릭시
                            // 현재 삭제가 되기는 하나 refresh가 제대로 이뤄지지 않는 상태
                            // FragmentStatePagerAdapter에 대해서 좀 더 찾아봐야 할 것같음
                            // POSITION_NONE은 제대로된 해결방법으로는 부족한 상태

                            // 이미지 없을 때
                            if (diaryBook.getDiaries().get(pageMark).getDiaryUriString().equals("")) {
                                DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                // 삭제할 데이터 세팅하기
                                diaryBook.getDiaries().remove(pageMark);
                                Map<String, Object> dbMap = new HashMap<>();
                                ConvertData convertData = new ConvertData();
                                dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));
                                diarybooksDoc.update(dbMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // 디비에 수정 완료
                                            // shared 수정하기
                                            diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                            Intent intent = new Intent();
                                            intent.putExtra("useremail", useremail);
                                            intent.putExtra("diarybookkey", diarybookkey);
                                            intent.putExtra("pagemark", pageMark);
                                            setResult(RESULT_OK, intent);
                                            finish();
                                        }
                                    }
                                });
                            } else {
                                // 이미지 있을 때
                                StorageReference diaryRef = storageRef.child("diarybooks").child("diary").child(diaryBook.getDiaries().get(pageMark).getDiaryKey());
                                // 일기 이미지 삭제하기
                                diaryRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                            // 삭제할 데이터 세팅하기
                                            diaryBook.getDiaries().remove(pageMark);
                                            Map<String, Object> dbMap = new HashMap<>();
                                            ConvertData convertData = new ConvertData();
                                            dbMap.put(diaryBook.getDiaryBookKey(), convertData.diarybooktoJson(diaryBook));
                                            diarybooksDoc.update(dbMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        // 디비에 수정 완료
                                                        // shared 수정하기
                                                        diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                                        Intent intent = new Intent();
                                                        intent.putExtra("useremail", useremail);
                                                        intent.putExtra("diarybookkey", diarybookkey);
                                                        intent.putExtra("pagemark", pageMark);
                                                        setResult(RESULT_OK, intent);
                                                        finish();
                                                    }
                                                }
                                            });
                                        } else {
                                            // 일기 이미지 삭제 실패
                                            Toast.makeText(WatchDiaryActivity.this, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        return false;
                    }
                });
                popupMenu.show(); // 메뉴를 띄우기
            }
        });

        // 뒤로가기
        backImage = (ImageView) findViewById(R.id.activity_watchdiary_back);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() == 0)
                    Toast.makeText(WatchDiaryActivity.this, "첫번째 페이지입니다", Toast.LENGTH_SHORT).show();
                else
                    viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            }
        });

        // 마지막으로가기
        endImage = findViewById(R.id.activity_watchdiary_end);
        endImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("qqqq", "getFragment.set" + getSupportFragmentManager().getFragments().size());
                if (viewPager.getCurrentItem() == watchDiaryAdapter.getCount() - 1)
                    Toast.makeText(WatchDiaryActivity.this, "마지막 페이지입니다", Toast.LENGTH_SHORT).show();
                else
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
            }
        });
    }

    @Override
    protected void onStart() {
        Log.d("qwer", "WatchDiary onStart() : 온스타트");
        super.onStart();
        if (pageMark < 0) {
            return;
        }
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey);
        diaryCommentCount.setText(diaryBook.getDiaries().get(pageMark).getCommentCount());
    }

    // 뒤로가기 버튼
    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0)
            viewPager.setCurrentItem(0);
        else
            super.onBackPressed();
    }
}
