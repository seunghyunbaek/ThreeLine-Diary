package com.example.myapplication;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class JoinDiaryBookAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int WATCH_DIARY = 11;
    private static final int EDIT_JOIN_DIARY = 5; // 같이쓰는 일기 만들기
    private Context mContext; // 메인액티비티 컨텍스트
    private ArrayList<DiaryBook> userJoinDiaryBooks; // 유저의 같이쓰는 일기책
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private String useremail; // 사용자 이메일
    private MainActivity mainActivity; // 메인액티비티

    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    public JoinDiaryBookAdapter(Context mContext, ArrayList<DiaryBook> userJoinDiaryBooks, DiaryBookSharedPreference diaryBookSharedPreference, String useremail) {
        this.mContext = mContext;
        this.userJoinDiaryBooks = userJoinDiaryBooks;
        this.diaryBookSharedPreference = diaryBookSharedPreference;
        this.useremail = useremail;

        mainActivity = (MainActivity) mContext;
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    // 같이쓰는 일기책 데이터 변경
    public void setUserJoinDiaryBooks(ArrayList<DiaryBook> userJoinDiaryBooks) {
        this.userJoinDiaryBooks = userJoinDiaryBooks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_diary, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        final MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

        myViewHolder.joinDiaryTitleText.setText(userJoinDiaryBooks.get(position).getDiaryBookTitleVertical());
        Glide.with(mContext).load(userJoinDiaryBooks.get(position).getDiaryBookUri()).into(myViewHolder.joinDiaryTitleImage);

        myViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userJoinDiaryBooks.get(position).isUserActivate(useremail)) { // 활성화시킨 유저목록에 없으면 false가 나옴
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(userJoinDiaryBooks.get(position).getDiaryBookTitleHorizontal());
                    builder.setMessage("같이쓰는 일기를 수락하시겠습니까?");
                    builder.setPositiveButton("수락",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    final MyProgressDialog myProgressDialog = new MyProgressDialog(mContext, "잠시만 기다려주세요...");
                                    userJoinDiaryBooks.get(position).setUserActivate(useremail); // 눌러서 일기책 사용허락하기

                                    // 저장할 데이터 폼 만들기
                                    Map<String, Object> dbMap = new HashMap<>();
                                    ConvertData convertData = new ConvertData();
                                    dbMap.put(userJoinDiaryBooks.get(position).getDiaryBookKey(), convertData.diarybooktoJson(userJoinDiaryBooks.get(position)));
                                    // db에 데이터 저장하기
                                    DocumentReference diarybooksDoc = db.collection("threeline").document("diarybooks");
                                    diarybooksDoc.update(dbMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // db에 저장 성공, shared에 저장하기
                                            myProgressDialog.dismiss();

                                            diaryBookSharedPreference.saveDiaryBook(userJoinDiaryBooks.get(position)); // 변경된 일기책 데이터 저장하기
                                            Toast.makeText(mContext, "같이 쓰는 일기를 수락했습니다", Toast.LENGTH_SHORT).show();
                                            notifyDataSetChanged();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            myProgressDialog.dismiss();
                                            // db에 저장 실패
                                            Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                        }
                                    });
//                                    Toast.makeText(mContext, "우측버튼 클릭됨", Toast.LENGTH_LONG).show();
                                }
                            });
                    builder.setNegativeButton("다음에",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
//                                    Toast.makeText(mContext, "좌측버튼 클릭됨", Toast.LENGTH_LONG).show();
                                }
                            });
                    builder.show();

                    return;
                }

                if (userJoinDiaryBooks.get(position).isUserActivate(useremail)) // 활성화 시킨 유저목록에 있는데
                    if (!userJoinDiaryBooks.get(position).isActivate()) { // 활성화가 되어있지 않으면
                        Toast.makeText(mContext, "상대방이 허락하지 않았습니다", Toast.LENGTH_SHORT).show();
                        return;
                    }

                Intent intent = new Intent(mContext, WatchDiaryActivity.class);
                intent.putExtra("useremail", useremail);
                intent.putExtra("diarybookkey", userJoinDiaryBooks.get(position).getDiaryBookKey());
                mainActivity.startActivityForResult(intent, WATCH_DIARY);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userJoinDiaryBooks.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001: // 수정
                        Intent diaryBookEditIntent = new Intent(mContext, DiaryBookEditActivity.class);
                        diaryBookEditIntent.putExtra("diarybookkey", userJoinDiaryBooks.get(getAdapterPosition()).getDiaryBookKey());
                        mainActivity.startActivityForResult(diaryBookEditIntent, EDIT_JOIN_DIARY);
                        break;
                    case 1002: // 삭제
                        final MyProgressDialog myProgressDialog = new MyProgressDialog(mContext, "잠시만 기다려주세요...");
                        StorageReference diarybooksRef = storageRef.child("diarybooks").child(userJoinDiaryBooks.get(getAdapterPosition()).getDiaryBookKey());
                        diarybooksRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    // 이미지 삭제 성공
                                    // 삭제할 데이터 만들기
                                    Map<String, Object> delMap = new HashMap<>();
                                    delMap.put(userJoinDiaryBooks.get(getAdapterPosition()).getDiaryBookKey(), FieldValue.delete());

                                    DocumentReference diarybookDoc = db.collection("threeline").document("diarybooks");
                                    diarybookDoc.update(delMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                // 디비에서 삭제 성공
                                                myProgressDialog.dismiss();
                                                // shared에서도 삭제하기
                                                diaryBookSharedPreference.removeUserDiaryBook(userJoinDiaryBooks.get(getAdapterPosition()).getDiaryBookKey());
                                                userJoinDiaryBooks = diaryBookSharedPreference.userJoinDiaryBook(useremail);
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
        private ImageView joinDiaryTitleImage;
        private TextView joinDiaryTitleText;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            joinDiaryTitleImage = itemView.findViewById(R.id.item_diary_title_image);
            joinDiaryTitleText = itemView.findViewById(R.id.item_diary_title_text);

            itemView.setOnCreateContextMenuListener(this);
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
