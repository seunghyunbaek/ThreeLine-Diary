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
import com.example.myapplication.com.example.myapplication.data.DiaryCommentReply;
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

public class CommentReplyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int COMMENT_REPLY_EIDT_COMPLETE = 6; // 답글 수정 완료
    private Context mContext; // CommentReplyActivity Context
    private DiaryCommentReplyActivity commentReplyActivity;
    private String useremail; // 사용자 이메일
    private String diarybookkey; // 일기책 키 값
    private DiaryBookSharedPreference diaryBookSharedPreference; // 일기책 데이터 관리
    private int diaryPosition; // 일기 인덱스
    private int commentposition; // 댓글 인덱스
    private UserSharedPreference userSharedPreference; // 유저 데이터 관리
    private DiaryBook diaryBook; // 일기책
    private Diary diary; // 일기
    private DiaryComment diaryComment; // 일기 댓글
    private ArrayList<DiaryCommentReply> diaryCommentReplies; // 일기 답글 목록
    private User user; // 유저

    private FirebaseFirestore db;

    public CommentReplyAdapter(Context mContext, String useremail, String diarybookkey, DiaryBookSharedPreference diaryBookSharedPreference, int diaryPosition, int commentposition, UserSharedPreference userSharedPreference) {
        this.mContext = mContext;
        this.useremail = useremail;
        this.diarybookkey = diarybookkey;
        this.diaryBookSharedPreference = diaryBookSharedPreference;
        this.diaryPosition = diaryPosition;
        this.commentposition = commentposition;
        this.userSharedPreference = userSharedPreference;

        commentReplyActivity = (DiaryCommentReplyActivity) mContext;

        diaryBook = diaryBookSharedPreference.findDiaryBook(diarybookkey); // 일기책 얻기
        diary = diaryBook.getDiaries().get(diaryPosition); // 일기 얻기
        diaryComment = diary.getComments().get(commentposition); // 댓글 얻기
        diaryCommentReplies = diaryComment.getReplies(); // 답글 목록 데이터 얻기

        if (db == null)
            db = FirebaseFirestore.getInstance();
    }

    // 수정된 일기책 데이터 알려주기
    public void setDiaryBook(DiaryBook diaryBook) {
        this.diaryBook = diaryBook;
        diary = diaryBook.getDiaries().get(diaryPosition);
        diaryComment = diary.getComments().get(commentposition);
        diaryCommentReplies = diaryComment.getReplies();
        notifyDataSetChanged(); // 변경된 내용 알리기
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_activity_comment_reply, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(v);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        MyViewHolder myViewHolder = (MyViewHolder) viewHolder;


        DiaryCommentReply diaryCommentReply = diaryCommentReplies.get(position);
        // 답글 작성한 유저 얻기
        User replyUser = userSharedPreference.findUserData(diaryCommentReply.getAuthorityUser());

        if (replyUser.getUserUri() != null) {// 답글 작성한 유저 프로필 이미지 설정하기
//            myViewHolder.commentUserImage.setImageURI(replyUser.getUserUri());
            Glide.with(mContext).load(replyUser.getUserUri()).into(myViewHolder.commentUserImage);
        }
        myViewHolder.commentUserName.setText(replyUser.getUserName()); // 답글 작성한 유저 이름
        myViewHolder.commentText.setText(diaryCommentReplies.get(position).getCommentText()); // 답글 내용 설정하기
        myViewHolder.commentCreate.setText(diaryCommentReplies.get(position).getCreateDate()); // 답글 작성한 날짜 적기

        if (position == 0) // 댓글에 대한 것이기 때문에
            return;

        // 자기가 작성한 댓글만 수정 삭제가 가능합니다
        if (diaryCommentReply.getAuthorityUser().equals(useremail))
            myViewHolder.itemView.setOnCreateContextMenuListener(myViewHolder);
        else
            myViewHolder.itemView.setOnCreateContextMenuListener(null);
    }

    @Override
    public int getItemCount() {
        return diaryCommentReplies.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        private final MenuItem.OnMenuItemClickListener onEditMenu = new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case 1001: // 수정
                        Intent commentEditIntent = new Intent(mContext, CommentReplyEditActivity.class);
                        commentEditIntent.putExtra("diarybookkey", diarybookkey);
                        commentEditIntent.putExtra("diaryPosition", diaryPosition);
                        commentEditIntent.putExtra("commentposition", commentposition);
                        commentEditIntent.putExtra("replyposition", getAdapterPosition());
                        commentReplyActivity.startActivityForResult(commentEditIntent, COMMENT_REPLY_EIDT_COMPLETE);
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
                                    dbDiaryBook.getDiaries().get(diaryPosition).getComments().get(commentposition).getReplies().remove(getAdapterPosition()); // 답글 삭제

                                    final DiaryBook changedDiaryBook = dbDiaryBook;

                                    Map<String, Object> updateMap = new HashMap<>();
                                    updateMap.put(dbDiaryBook.getDiaryBookKey(), convertData.diarybooktoJson(dbDiaryBook));

                                    // 변경한 데이터 저장하기
                                    db.collection("threeline").document("diarybooks").update(updateMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    myProgressDialog.dismiss();
                                                    diaryBook = changedDiaryBook;
                                                    diary = diaryBook.getDiaries().get(diaryPosition); // 일기 얻기
                                                    diaryComment = diary.getComments().get(commentposition); // 댓글 얻기
                                                    diaryCommentReplies = diaryComment.getReplies(); // 답글 목록 데이터 얻기

                                                    diaryBookSharedPreference.saveDiaryBook(diaryBook);
                                                    notifyDataSetChanged();
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
        ImageView commentUserImage;
        TextView commentUserName;
        TextView commentText;
        TextView commentCreate;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            commentUserImage = itemView.findViewById(R.id.item_activity_comment_reply_userimage);
            commentUserName = itemView.findViewById(R.id.item_activity_comment_reply_username);
            commentText = itemView.findViewById(R.id.item_activity_comment_reply_text);
            commentCreate = itemView.findViewById(R.id.item_activity_comment_reply_date);

//            itemView.setOnCreateContextMenuListener(this);
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
