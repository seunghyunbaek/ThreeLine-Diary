package com.example.myapplication.com.example.myapplication.data;

import android.net.Uri;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Diary implements Serializable {

    // FIELDS
    private String contentText1; // 다이어리 내용 1
    private String contentText2; // 다이어리 내용 2
    private String contentText3; // 다이어리 내용 3
    private String hiddenContentText; // 숨겨진 내용
    private String diaryUriString; // 다이어리 이미지
    private String location; // 장소
    private String outhorityUser; // 일기 작성자
    private String createDate; // 생성 날짜

    private ArrayList<String> likeUsersEmail; // 좋아요 누른 유저의 이메일 리스트 .size() 가 좋아요 개수
    private ArrayList<DiaryComment> comments; // comments.size 가 곧 코멘트 개수

    // CONSTRUCTOR
    public Diary(String contentText1, String contentText2, String contentText3, String hiddenContentText, String diaryUriString, String location, String outhorityUser) {
        this.contentText1 = contentText1;
        this.contentText2 = contentText2;
        this.contentText3 = contentText3;
        this.hiddenContentText = hiddenContentText;
        this.diaryUriString = diaryUriString;
        this.location = location;
        this.outhorityUser = outhorityUser;

        // 일기 작성한 날짜
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
//        createDate = sdf.format(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        createDate = sdf.format(new Date());

        likeUsersEmail = new ArrayList<String>();
        comments = new ArrayList<DiaryComment>();
    }

    // METHOD

    // 일기 이미지 uri 얻기
    public Uri getDiaryUri() {
        // 일기 이미지가 없으면 null값을 리턴합니다
        if (diaryUriString.equals(""))
            return null;

        return Uri.parse(diaryUriString);
    }

    public String getDiaryUriString() {
        return diaryUriString;
    }

    // 일기 내용 얻기
    public String getContentText() {
        String contentText = "";
        if (!contentText1.equals(""))
            contentText = contentText + contentText1 + "\n";
        if (!contentText2.equals(""))
            contentText = contentText + contentText2 + "\n";
        if (!contentText3.equals(""))
            contentText = contentText + contentText3;

        return contentText;
    }

    // 일기 작성 날짜 얻기
    public String getCreateDate() {
        String diaryCreateDate = "";

        diaryCreateDate = diaryCreateDate + createDate.substring(0, 4) + "년 ";
        diaryCreateDate = diaryCreateDate + createDate.substring(4, 6) + "월 ";
        diaryCreateDate = diaryCreateDate + createDate.substring(6, 8) + "일";

        return diaryCreateDate + " " + location;
    }

    // 일기 좋아요 수 얻기
    public String getLikeUsersCount() {
        return String.valueOf(likeUsersEmail.size());
    }

    // 일기 댓글 수 얻기
    public String getCommentCount() {
        return String.valueOf(comments.size());
    }

    // 일기 내용 얻기
    public String getContentText1() {
        return contentText1;
    }

    public String getContentText2() {
        return contentText2;
    }

    public String getContentText3() {
        return contentText3;
    }

    // 장소 내용 얻기
    public String getLocation() {
        return location;
    }

    // 숨은 일기 내용 얻기
    public String getHiddenContentText() {
        return hiddenContentText;
    }

    // 나의 일기인지 체크합니다
    public boolean isUserDiary(String useremail) {
        // 나의 일기이면 true를 리턴
        if (outhorityUser.equals(useremail))
            return true;

        return false;
    }

    // 좋아요 눌렀는지 체크하기
    public boolean isLikeDiary(String useremail) {
        for (String likeUser : likeUsersEmail) {
            if (likeUser.equals(useremail))
                return true; // 좋아요 누른 유저 목록에 있다면 true를 리턴합니다
        }

        return false; // 좋아요 누른 사람 목록에 없으면 false를 리턴
    }

    // 좋아요 누르기
    public void likeDiary(String useremail) {
        likeUsersEmail.add(useremail);
    }

    public void unLikeDiary(String useremail) {
        likeUsersEmail.remove(useremail);
    }

    // 댓글단 유저목록 얻기
    public ArrayList<DiaryComment> getComments() {
        return comments;
    }

    // 댓글 추가하기
    public void addDiaryComment(DiaryComment diaryComment) {
        comments.add(diaryComment);
    }

    // 댓글 수정하기
    public void setDiaryComment(int index, DiaryComment diaryComment) {
        comments.set(index, diaryComment);
    }

    // 특정 날짜의 일기 찾기
    public boolean findDiaryDate(String date) {

        if (createDate.substring(0, 8).equals(date))
            return true;
        return false;
    }

    public String getDiaryKey() {
        return outhorityUser + createDate;
    }

    public void setDiaryUriString(String diaryUriString) {
        this.diaryUriString = diaryUriString;
    }


    public void setContentText1(String contentText1) {
        this.contentText1 = contentText1;
    }

    public void setContentText2(String contentText2) {
        this.contentText2 = contentText2;
    }

    public void setContentText3(String contentText3) {
        this.contentText3 = contentText3;
    }

    public void setHiddenContentText(String hiddenContentText) {
        this.hiddenContentText = hiddenContentText;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setCommentArray(ArrayList<DiaryComment> commentArray) {
        this.comments = commentArray;
    }
}
