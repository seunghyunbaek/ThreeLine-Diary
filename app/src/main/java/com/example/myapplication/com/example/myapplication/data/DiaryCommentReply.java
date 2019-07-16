package com.example.myapplication.com.example.myapplication.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DiaryCommentReply implements Serializable {

    // FIELDS
    private String commentText; // 작성한 내용
    private String authorityUser; // 댓글을 작성한 사람의 이메일
    private String createDate; // 작성한 시간

    // CONSTRUCTOR
    public DiaryCommentReply(String commentText, String authorityUser) {
        this.commentText = commentText;
        this.authorityUser = authorityUser;

        // 댓글 작성한 날짜
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 kk:mm");
        createDate = sdf.format(new Date());
    }

    // 작성된 답글 내용 얻기
    public String getCommentText() {
        return commentText;
    }

    // 답글 내용 수정하기
    public void setCommentText(String commentText) {
        this.commentText = commentText; // 답글 내용 수정하기

//        // 수정된 시간 설정하기
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 kk:mm");
//        createDate = sdf.format(new Date());
    }

    // 답글 작성자 얻기
    public String getAuthorityUser() {
        return authorityUser;
    }

    // 작성된 시간 얻기
    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }
}
