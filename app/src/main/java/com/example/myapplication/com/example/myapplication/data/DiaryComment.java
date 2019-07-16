package com.example.myapplication.com.example.myapplication.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DiaryComment implements Serializable {

    // FIELDS
    private String commentText; // 작성한 내용
    private String authorityUser; // 댓글을 작성한 사람의 이메일
    private String createDate; // 작성된 시간 변수

    private ArrayList<DiaryCommentReply> replies; // 답글 리스트

    //    ArrayList<String> likeUsers; // 댓글에 좋아요를 누른 사람 ==> 대댓글타입 변수 리스트로 만들기

    // CONSTRUCTOR
    public DiaryComment(String commentText, String authorityUser) {
        this.commentText = commentText;
        this.authorityUser = authorityUser;

        // 댓글 작성한 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 kk:mm");
        createDate = sdf.format(new Date());

        replies = new ArrayList<DiaryCommentReply>();
        replies.add(new DiaryCommentReply(commentText, authorityUser));
    }

    // METHOD
    // 댓글 내용얻기
    public String getCommentText() {
        return commentText;
    }

    // 댓글 내용 수정하기
    public void setCommentText(String commentText) {
        this.commentText = commentText;
        replies.set(0, new DiaryCommentReply(commentText, authorityUser));
//        setCreateDate();
    }

    // 댓글 작성자 얻기
    public String getAuthorityUser() {
        return authorityUser;
    }

    // 댓글 작성 날짜 얻기
    public String getCreateDate() {
        return createDate;
    }

    public ArrayList<DiaryCommentReply> getReplies() {
        return replies;
    }

    // 답글 목록 수정하기
    public void setReplies(ArrayList<DiaryCommentReply> replies) {
        this.replies = replies;
    }

    // 작성한 날짜 수정하기
    public void setCreateDate() {
        // 댓글 작성한 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 kk:mm");
        createDate = sdf.format(new Date());
    }

    // 답글 수정하기
    public void setDiaryCommentReply(int index, DiaryCommentReply diaryCommentReply) {
        replies.set(index, diaryCommentReply);
    }

    public String getRepliesCount() {
        return String.valueOf(replies.size() - 1);
    }
}
