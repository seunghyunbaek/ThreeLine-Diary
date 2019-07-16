package com.example.myapplication.com.example.myapplication.data;

import android.net.Uri;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DiaryBook implements Serializable {

    // FIELDS
    private String diaryTitle1; // 표지 제목1
    private String diaryTitle2; // 표지 제목2
    private String diaryTitle3; // 표지 제목3
    private String diaryBookUriString; // 표지 이미지 주소
    private boolean isOpen; // 공개 여부
    private String createDate; // 일기책이 생성된 날짜 (키값용)
    private ArrayList<String> isActivate; // 일기책을 활성화 시킨 유저의 이메일

    private ArrayList<String> outhorityUsersEmail; // 일기책을 만든 사람, 권한있는 유저이메일 (작성, 수정, 삭제 가능한 유저)
    private ArrayList<String> subscribeUsersEmail; // 구독자 email
    private ArrayList<Diary> diaries; // 작성된 일기들

    // CONTRUCTOR
    public DiaryBook(String diaryTitle1, String diaryTitle2, String diaryBookUri, boolean isOpen, ArrayList<String> outhorityUsersEmail) {
        this.diaryTitle1 = diaryTitle1;
        this.diaryTitle2 = diaryTitle2;
        this.diaryBookUriString = diaryBookUri;
        this.isOpen = isOpen;
        this.outhorityUsersEmail = outhorityUsersEmail; // 권한있는 유저 목록

        diaryTitle3 = "세줄일기";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        createDate = sdf.format(new Date());

        isActivate = new ArrayList<String>(); // 일기책 활성화 상태인가
        isActivate.add(outhorityUsersEmail.get(0));

        subscribeUsersEmail = new ArrayList<String>();
        diaries = new ArrayList<Diary>();
    }

    // METHOD

    // 일기책 표지 Uri 얻기
    public Uri getDiaryBookUri() {
        // 표지 Uri가 없으면 null이 리턴됩니다
        if (diaryBookUriString.equals(""))
            return null;

        return Uri.parse(diaryBookUriString);
    }

    // 일기책 표지 제목 한줄 얻기
    public String getDiaryTitle1() {
        return diaryTitle1;
    }

    // 일기책 표지 제목 수정하기
    public void setDiaryTitle1(String diaryTitle1) {
        this.diaryTitle1 = diaryTitle1;
    }

    public String getDiaryTitle2() {
        return diaryTitle2;
    }

    public void setDiaryTitle2(String diaryTitle2) {
        this.diaryTitle2 = diaryTitle2;
    }

    // 일기책 표지 완전한 제목 얻기 (세로)
    public String getDiaryBookTitleVertical() {
        String title = "";
        if (!diaryTitle1.equals(""))
            title = title + diaryTitle1 + "\n";
        if (!diaryTitle2.equals(""))
            title = title + diaryTitle2 + "\n";
        title = title + diaryTitle3;

        if (!isActivate())
            title = title + "(대기)";

        return title;
    }

    // 일기책 표지 완전한 제목 얻기 (가로)
    public String getDiaryBookTitleHorizontal() {
        String title = "";
        if (!diaryTitle1.equals(""))
            title = title + diaryTitle1 + " ";
        if (!diaryTitle2.equals(""))
            title = title + diaryTitle2 + " ";
        title = title + diaryTitle3;

        return title;
    }

    // 일기책 공개 여부 얻기
    public boolean isOpen() {
        return isOpen;
    }

    // 일기책 공개 여부 수정하기
    public void setOpen(boolean open) {
        isOpen = open;
    }

    // 일기책 생성 날짜 얻기
    public String getCreateDate() {
        String diaryBookCreateDate = "";

        diaryBookCreateDate = diaryBookCreateDate + createDate.substring(0, 4) + "년 ";
        diaryBookCreateDate = diaryBookCreateDate + createDate.substring(4, 6) + "월 ";
        diaryBookCreateDate = diaryBookCreateDate + createDate.substring(6, 8) + "일";

        return diaryBookCreateDate;
    }

    // 작성한 일기목록 얻기
    public ArrayList<Diary> getDiaries() {
        return diaries;
    }

    // 일기책 이미지 Uri 수정하기
    public void setDiaryBookUriString(String diaryBookUriString) {
        this.diaryBookUriString = diaryBookUriString;
    }

    // 일기책에 일기 추가하기
    public void addDiary(Diary diary) {
        // 새 일기 맨 앞에 추가하기
//        ArrayList<Diary> tempDiaries = new ArrayList<Diary>();
//        tempDiaries.add(diary);
//        tempDiaries.addAll(diaries);
//        diaries = tempDiaries;
        // 새 일기 가장 뒤에 추가하기
        diaries.add(diary);
    }

    // 일기책 키 값 얻기
    public String getDiaryBookKey() {
        // 일기책 키 값을 만듭니다
        String primarykey = "";
        for (String key : outhorityUsersEmail) {
            primarykey = primarykey + key;
        }
        // 일기책 키값은 소유자들의 이메일 + 생성된 시각입니다
        primarykey = primarykey + createDate;

        return primarykey;
    }

    // 나의 일기책인지 체크합니다
    public boolean isUserDiaryBook(String useremail) {
        // 소유권자의 자신이 등록되어있는지 확인합니다
        for (String outhorityUser : outhorityUsersEmail) {
            if (outhorityUser.equals(useremail)) // 소유자에 있다면 자신의 일기책입니다
                return true; // 나의 일기책이면 true를 리턴
        }

        return false;
    }

    // 혼자쓰는 일기책인지 체크합니다
    public boolean isAloneDIaryBook() {
        // 일기의 소유자가 2명이상이면 false가 리턴됩니다 ( 혼자쓰는 일기책이 아니면 false를 리턴 )
        if (outhorityUsersEmail.size() > 1)
            return false;

        return true;
    }

    // 일기책을 구독중인지 체크합니다
    public boolean isSubscribeDiaryBook(String useremail) {
        // 구독중인 유저목록에서 확인합니다
        for (String subscribeUser : subscribeUsersEmail) {
            if (subscribeUser.equals(useremail)) // 구독중인 유저목록에 들어있다면 true가 리턴됩니다.
                return true;
        }

        // 구독중인 유저목록에 없다면 false가 리턴됩니다
        return false;
    }

    // 일기책 구독하기
    public void subscribeDiaryBook(String useremail) {
        subscribeUsersEmail.add(useremail);
    }

    // 일기책 구독 취소하기
    public void unSubscribeDiaryBook(String useremail) {
        subscribeUsersEmail.remove(useremail);
    }

    // 일기책 소유자인지 확인하기
    public boolean isOwnDiaryBook(String useremail) {
        // 자신의 일기책이라면 true가 리턴됩니다
        for (String outhorityUser : outhorityUsersEmail) {
            if (outhorityUser.equals(useremail)) // 구독중인 유저목록에 들어있다면 true가 리턴됩니다.
                return true;
        }

        // 자신의 일기책이 아니면 false가 리턴됩니다
        return false;
    }

    // 구독중인 유저의 수를 리턴합니다
    public int getSubscribeUsersCount() {
        return subscribeUsersEmail.size();
    }

    // 일기책 소유자의 목록을 리턴합니다
    public ArrayList<String> getOuthorityUsersEmail() {
        return outhorityUsersEmail;
    }

    // 일기 수정하기
    public void setDiary(int index, Diary diary) {
        diaries.set(index, diary);
    }

    // 일기책이 활성화 상태인지 확인하기
    public boolean isActivate() {
        for (String outhorityUser : outhorityUsersEmail) {
            if (!isActivate.contains(outhorityUser))
                return false; // 비활성화시 false를 리턴합니다
        }
//        for (int i = outhorityUsersEmail.size(); i > 0; i--) {
//            if (!isActivate.contains("test")) { // 활성화 시킨유저 목록에 권한을 가진 유저의 이메일이 없으면 false를 리턴
//                return false;
//            }
//        }
        return true; // 활성화 되었으면 true를 리턴합니다
    }

    // 활성화 시킨 유저목록에 유저가 들어있는지 확인하기
    public boolean isUserActivate(String useremail) {

        for (String activateUser : isActivate) {
            if (isActivate.contains(useremail)) // 활성화 시킨 유저의 목록에 들어있으면 true
                return true;
        }

        return false; // 없으면 false
    }

    public void setUserActivate(String useremail) {
        isActivate.add(useremail);
    }

    // 특정 날짜의 일기 찾기
    public int findDiaryPosition(String date) {
        for (int i = 0; i < diaries.size(); i++) {
            if (diaries.get(i).findDiaryDate(date))
                return i + 1; // 특정 날짜의 일기가 있으면 일기의 포지션을 반환합니다
        }
        return -1; // 특정 날짜의 일기가 없으면 -1을 반환
    }
}
