package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OwnDiaryBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EDIT_OWN_DIARY = 3; // 혼자쓰는 일기 수정하기
    private static final int WATCH_DIARY = 11;
    private Context mContext; // 메인액티비티 컨텍스트
    private ArrayList<DiaryBook> userAloneDiaryBooks; // 혼자쓰는 일기책 목록 데이터
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private String useremail;
    private MainActivity mainActivity; // 메인액티비티
    private Context mContext2;

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // 어댑터 생성자
    public OwnDiaryBookAdapter(Context mContext, Context mContext2, ArrayList<DiaryBook> userAloneDiaryBooks, DiaryBookSharedPreference diaryBookSharedPreference, String useremail) {
        this.mContext = mContext;
        this.mContext2 = mContext2;
        this.userAloneDiaryBooks = userAloneDiaryBooks;
        this.diaryBookSharedPreference = diaryBookSharedPreference;
        this.useremail = useremail;
        mainActivity = (MainActivity) mContext;

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    // 혼자 쓰는 일기책 데이터 변경
    public void setUserAloneDiaryBooks(ArrayList<DiaryBook> userAloneDiaryBooks) {
        this.userAloneDiaryBooks = userAloneDiaryBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        // 뷰 홀더 연결하기
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_diary, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

//        Log.d("어댑터디비", "uri: " + userAloneDiaryBooks.get(position).getDiaryBookUri().toString());

        Glide.with(mContext)
                .load(userAloneDiaryBooks.get(position).getDiaryBookUri())
                .into(myViewHolder.soloDiaryTitleImage);
        myViewHolder.soloDiaryTitleText.setText(userAloneDiaryBooks.get(position).getDiaryBookTitleVertical());

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent watchDiaryIntent = new Intent(mContext, WatchDiaryActivity.class);
                watchDiaryIntent.putExtra("useremail", useremail);
                watchDiaryIntent.putExtra("diarybookkey", userAloneDiaryBooks.get(position).getDiaryBookKey());
                mainActivity.startActivityForResult(watchDiaryIntent, WATCH_DIARY);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userAloneDiaryBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001: // 수정
                        Intent diaryBookEditIntent = new Intent(mContext, DiaryBookEditActivity.class);
                        diaryBookEditIntent.putExtra("diarybookkey", userAloneDiaryBooks.get(getAdapterPosition()).getDiaryBookKey());
                        mainActivity.startActivityForResult(diaryBookEditIntent, EDIT_OWN_DIARY);
                        break;
                    case 1002: // 삭제
                        final MyProgressDialog myProgressDialog = new MyProgressDialog(mContext, "잠시만 기다려주세요...");
                        StorageReference diarybooksRef = storageRef.child("diarybooks").child(userAloneDiaryBooks.get(getAdapterPosition()).getDiaryBookKey());
                        diarybooksRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // 이미지 삭제 성공
                                    // 삭제할 데이터 만들기
                                    Map<String, Object> delMap = new HashMap<>();
                                    delMap.put(userAloneDiaryBooks.get(getAdapterPosition()).getDiaryBookKey(), FieldValue.delete());
                                    DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                    diarybooksDoc.update(delMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // 디비에서 삭제 성공
                                                myProgressDialog.dismiss();
                                                // shared에서도 삭제하기
                                                diaryBookSharedPreference.removeUserDiaryBook(userAloneDiaryBooks.get(getAdapterPosition()).getDiaryBookKey());
                                                userAloneDiaryBooks = diaryBookSharedPreference.userAloneDiaryBook(useremail);
                                                notifyDataSetChanged();
                                            } else {
                                                // 디비에서 삭제 실패
                                                myProgressDialog.dismiss();
                                                Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    // 이미지 삭제 실패
                                    myProgressDialog.dismiss();
                                    Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                        break;
                }
                return true;
            }
        };
        private ImageView soloDiaryTitleImage;
        private TextView soloDiaryTitleText;

        public MyViewHolder(@NonNull View view) {
            super(view);
            soloDiaryTitleImage = (ImageView) view.findViewById(R.id.item_diary_title_image);
            soloDiaryTitleText = (TextView) view.findViewById(R.id.item_diary_title_text);

            view.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem edit = menu.add(menu.NONE, 1001, 1, "수정");
            MenuItem delete = menu.add(menu.NONE, 1002, 2, "삭제");

            edit.setOnMenuItemClickListener(onEditMenu);
            delete.setOnMenuItemClickListener(onEditMenu);
        }
    }
}