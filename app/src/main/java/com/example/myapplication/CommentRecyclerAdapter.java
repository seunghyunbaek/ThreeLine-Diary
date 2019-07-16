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
import com.example.myapplication.com.example.myapplication.data.ConvertData;
import com.example.myapplication.com.example.myapplication.data.Diary;
import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;
import com.example.myapplication.com.example.myapplication.data.DiaryComment;
import com.example.myapplication.com.example.myapplication.data.MyProgressDialog;
import com.example.myapplication.com.example.myapplication.data.User;
import com.example.myapplication.com.example.myapplication.data.UserSharedPreference;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


// 1-1. 컨텍스트 메뉴를 사용하려면 OnCreateContextMenuListener 를 구현해야합니다
public class CommentRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int COMMENT_EDIT_COMPLETE = 5; // 댓글 수정 완료
    private Context mContext; // CommentActivity 컨택스트
    private CommentActivity commentActivity; // 코멘트 액티비티
    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키값
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private int diaryPosition; // 일기의 인덱스
    private DiaryBook diaryBook; // 변경된 일기책을 저장해야 합니다
    private Diary diary; // 일기에 댓글이 달립니다
    private ArrayList<DiaryComment> comments; // 일기에 달린 댓글들
    private UserSharedPreference userSharedPreference;

    private FirebaseFirestore db;

    public CommentRecyclerAdapter(Context context, String useremail, String diarybookkey, DiaryBookSharedPreference diaryBookSharedPreference, int diaryPosition, UserSharedPreference userSharedPreference) {
        mContext = context;
        commentActivity = (CommentActivity) mContext;

        this.useremail = useremail; // 답글로 넘어갈 때 유저 이메일 필요
        this.diarybookkey = diarybookkey; // 일기책을 저장해야하기 때문에 필요
        this.diaryBookSharedPreference = diaryBookSharedPreference; // 삭제가 이뤄지기 때문에 필요
        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기책 얻기
        diary = diaryBook.getDiaries().get(diaryPosition); // 일기 얻기
        comments = diary.getComments(); // 댓글 데이터 얻기
        this.diaryPosition = diaryPosition; // 일기의 인덱스
        this.userSharedPreference = userSharedPreference; // 유저 데이터 관리

        if (db == null)
            db = FirebaseFirestore.getInstance();
    }

    public void setDiaryBook(DiaryBook diaryBook) { // 변경된 일기책 설정하기
        this.diaryBook = diaryBook; // 일기책 데이터 변경
        diary = diaryBook.getDiaries().get(diaryPosition); // 일기 변경
        comments = diary.getComments(); // 댓글 변경
        notifyDataSetChanged(); // 데이터 변경된것 알리기
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_activity_comment, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;

        DiaryComment diaryComment = comments.get(position);
        User commentUser = userSharedPreference.findUserData(diaryComment.getAuthorityUser()); // 댓글 작성한 유저의 데이터 가져오기 : 유저의 프로필이 변경되었으면 변경된 프로필을 가져오기 위함

        // 사용자가 작성한 댓글이면 수정, 삭제가 가능합니다
        if (diaryComment.getAuthorityUser().equals(useremail)) // 사용자가
            myViewHolder.itemView.setOnCreateContextMenuListener(myViewHolder);
        else
            myViewHolder.itemView.setOnCreateContextMenuListener(null);

        // 댓글단 작성자의 프로필 이미지를 세팅합니다
        if (commentUser.getUserUri() != null) {// 유저가 프로필 이미지를 설정했으면 유저의 프로필 이미지로 설정해줍니다
//            myViewHolder.commentUserImage.setImageURI(commentUser.getUserUri());
            Glide.with(mContext).load(commentUser.getUserUri()).into(myViewHolder.commentUserImage);
        }
        // 댓글단 작성자의 필명을 세팅합니다
        myViewHolder.commentUserName.setText(commentUser.getUserName());
        // 댓글 내용을 세팅합니다
        myViewHolder.commentText.setText(diaryComment.getCommentText());
        // 댓글 작성날짜를 세팅합니다
        myViewHolder.commentCreate.setText(diaryComment.getCreateDate());

        // 답글 작성하러 가기
        myViewHolder.commentReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent replyIntent = new Intent(mContext, DiaryCommentReplyActivity.class);
                replyIntent.putExtra("useremail", useremail);
                replyIntent.putExtra("diarybookkey", diarybookkey);
                replyIntent.putExtra("diaryposition", diaryPosition);
                replyIntent.putExtra("commentposition", position);
                commentActivity.startActivity(replyIntent);
            }
        });

        // 답글 개수
        myViewHolder.commentReplyCount.setText(comments.get(position).getRepliesCount());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setDiaryBookSharedPreference(DiaryBookSharedPreference diaryBookSharedPreference) {
        this.diaryBookSharedPreference = diaryBookSharedPreference;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        // 1-4. 컨텍스트 메뉴에서 항목 클릭시 동작을 설정합니다
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001: // 수정
                        final Intent commentEditIntent = new Intent(mContext, CommentEditActivity.class);
                        commentEditIntent.putExtra("diarybookkey", diarybookkey);
                        commentEditIntent.putExtra("diaryposition", diaryPosition);
                        commentEditIntent.putExtra("commentposition", getAdapterPosition());
                        commentActivity.startActivityForResult(commentEditIntent, COMMENT_EDIT_COMPLETE); // 수정이 완료되면 변경된 결과를 알려주기 위해 startactivityforresult를 사용
                        break;
                    case 1002: // 삭제
                        final MyProgressDialog myProgressDialog = new MyProgressDialog(mContext, "잠시만 기다려주세요...");
                        // 디비에서 불러오기
                        db.collection("threeline").document("diarybooks").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    Map<String, Object> dbMap = task.getResult().getData();
                                    String diarybookJson = dbMap.get(diaryBook.getDiaryBookKey()).toString();
                                    ConvertData convertData = new ConvertData();
                                    DiaryBook dbDiaryBook = convertData.jsonToDiarybook(diarybookJson);
                                    dbDiaryBook.getDiaries().get(diaryPosition).getComments().remove(getAdapterPosition()); // 댓글 삭제

                                    final DiaryBook changedDiaryBook = dbDiaryBook;

                                    Map<String, Object> updateMap = new HashMap<>();
                                    updateMap.put(dbDiaryBook.getDiaryBookKey(), convertData.diarybooktoJson(dbDiaryBook));

                                    // 변경한 데이터 저장하기
                                    db.collection("threeline").document("diarybooks").update(updateMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    myProgressDialog.dismiss();
                                                    setDiaryBook(changedDiaryBook);
//                                                    diaryBook = changedDiaryBook;
//                                                    diary = diaryBook.getDiaries().get(diaryPosition);
//                                                    comments = diary.getComments();
                                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);

//                                                    notifyDataSetChanged();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    myProgressDialog.dismiss();
                                                    Toast.makeText(mContext, "다시 시도해주세요", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }
                        });
                        break;
                }
                return true;
            }
        };
        ImageView commentUserImage; // 댓글 작성자의 프로필 이미지
        TextView commentUserName; // 댓글의 내용
        TextView commentText; // 댓글의 내용
        TextView commentCreate; // 댓글 작성 날짜
        TextView commentReply; // 답글달기
        TextView commentReplyCount; // 답글 개수

        public MyViewHolder(@NonNull View view) {
            super(view);
            commentUserImage = view.findViewById(R.id.item_activity_comment_userimage);
            commentUserName = view.findViewById(R.id.item_activity_comment_username);
            commentText = view.findViewById(R.id.item_activity_comment_text);
            commentCreate = view.findViewById(R.id.item_activity_comment_date);
            commentReply = view.findViewById(R.id.iteam_activity_comment_reply);
            commentReplyCount = view.findViewById(R.id.item_activity_comment_like);
        }

        // 1-3. 컨텍스트 메뉴를 생성하고 메뉴 항목 선택시 호출되는 리스너를 등록해줍니다. ID 1001, 1002로 어떤 메뉴를 선택했는지 리스너에서 구분하게 됩니다.
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuItem edit = menu.add(menu.NONE, 1001, 1, "수정");
            MenuItem delete = menu.add(menu.NONE, 1002, 2, "삭제");

            edit.setOnMenuItemClickListener(onEditMenu);
            delete.setOnMenuItemClickListener(onEditMenu);
        }
    }
}
