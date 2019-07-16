package com.example.myapplication.com.example.myapplication.data;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Map;

public class DiaryBookSharedPreference extends BaseSharedPreference {
    // 일기책 데이터를 관리하는 클래스

    // 생성자
    public DiaryBookSharedPreference(SharedPreferences sharedPreferences) {
        super(sharedPreferences);
    }

    // 일기책 저장하기
    public void saveDiaryBook(DiaryBook diaryBook) {
        // 일기책 데이터를 JSON형식으로 변경해줍니다
        String diaryBookJson = gson.toJson(diaryBook, DiaryBook.class);
        editor.putString(diaryBook.getDiaryBookKey(), diaryBookJson);
        editor.commit();
    }

    public void saveMapData(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    // 일기책 찾기
    public DiaryBook findDiaryBook(String diaryBookKey) {
        String diaryBookJson = sharedPreferences.getString(diaryBookKey, "");
        // 키에 맞는 일기책이 없으면 null값을 리턴
        if (diaryBookJson.equals(""))
            return null;

        // 일기책을 리턴해줍니다
        DiaryBook diaryBook = gson.fromJson(diaryBookJson, DiaryBook.class);
        return diaryBook;
    }

    // 모든 일기책을 돌려줍니다
    public ArrayList<DiaryBook> loadOpenAllDiaryBook() {
        // 저장된 모든 일기책을 가져온다
        ArrayList<DiaryBook> diaryBooks = new ArrayList<DiaryBook>();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String diaryBookJson = sharedPreferences.getString(entry.getKey(), "");
            DiaryBook diaryBook = gson.fromJson(diaryBookJson, DiaryBook.class);
            if (diaryBook.isActivate())
                if (diaryBook.isOpen())
                    diaryBooks.add(diaryBook);
        }
        return diaryBooks;
    }

    // 모든 일기책을 돌려줍니다
    public ArrayList<DiaryBook> loadAllDiaryBook() {
        // 저장된 모든 일기책을 가져온다
        ArrayList<DiaryBook> diaryBooks = new ArrayList<DiaryBook>();
        Map<String, ?> allEntries = sharedPreferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String diaryBookJson = sharedPreferences.getString(entry.getKey(), "");
            DiaryBook diaryBook = gson.fromJson(diaryBookJson, DiaryBook.class);
            diaryBooks.add(diaryBook);
        }
        return diaryBooks;
    }

    // 해당 유저의 모든 일기책 목록을 돌려줍니다
    public ArrayList<DiaryBook> userDiaryBook(String useremail) {
        ArrayList<DiaryBook> diaryBooks = new ArrayList<DiaryBook>(); // 유저의 일기책을 담을 리스트
        ArrayList<DiaryBook> allDiaryBooks = loadAllDiaryBook(); // 모든 일기책을 가져옵니다

        for (DiaryBook diaryBook : allDiaryBooks) {
            if (diaryBook.isUserDiaryBook(useremail)) // 유저의 일기책입니까? true -> 리스트에 추가하기
//                if (diaryBook.isActivate())
                diaryBooks.add(diaryBook);
        }

        return diaryBooks; // 유저의 일기책들을 반환해줍니다
    }

    // 유저의 활성화된 일기책 목록을 돌려줍니다
    public ArrayList<DiaryBook> activatedUserDiaryBook(String useremail) {
        ArrayList<DiaryBook> userDiaryBooks = userDiaryBook(useremail);
        ArrayList<DiaryBook> activatedDiaryBooks = new ArrayList<DiaryBook>();

        for (DiaryBook userDiaryBook : userDiaryBooks) {
            if (userDiaryBook.isActivate()) // 활성화된 일기책이면 활성화된 일기책 목록에 추가해줍니다
                activatedDiaryBooks.add(userDiaryBook);
        }

        return activatedDiaryBooks;
    }

    // 유저의 혼자쓰는 일기책을 돌려줍니다
    public ArrayList<DiaryBook> userAloneDiaryBook(String useremail) {
        ArrayList<DiaryBook> diaryBooks = new ArrayList<DiaryBook>(); // 유저의 혼자쓰는 일기책을 담을 리스트
        ArrayList<DiaryBook> userDiaryBooks = userDiaryBook(useremail); // 유저의 모든 일기책을 가져옵니다

        // 유저의 일기책 중 혼자 쓰는 일기책인지 체크합니다
        for (DiaryBook userDiaryBook : userDiaryBooks) {
            if (userDiaryBook.isAloneDIaryBook()) // 유저의 일기책이 혼자쓰는 일기책입니까? true -> 리스트에 추가하기
                diaryBooks.add(userDiaryBook);
        }


        return diaryBooks;
    }

    // 유저의 같이쓰는 일기책을 돌려줍니다
    public ArrayList<DiaryBook> userJoinDiaryBook(String useremail) {
        ArrayList<DiaryBook> diaryBooks = new ArrayList<DiaryBook>(); // 유저의 같이쓰는 일기책을 담을 리스트
        ArrayList<DiaryBook> userDiaryBooks = userDiaryBook(useremail); // 유저의 모든 일기책을 가져옵니다

        // 유저의 일기책 중 같이쓰는 일기책인지 체크합니다
        for (DiaryBook userDiaryBook : userDiaryBooks) {
            if (!userDiaryBook.isAloneDIaryBook()) // 유저의 일기책이 혼자쓰는 일기책입니까? false -> 같이쓰는 일기책이므로 추가하기
                diaryBooks.add(userDiaryBook);
        }

        return diaryBooks;
    }

    // 유저의 구독중인 일기책을 돌려줍니다
    public ArrayList<DiaryBook> userSubscribeDiaryBook(String useremail) {
        ArrayList<DiaryBook> userSubscribeDiaryBooks = new ArrayList<DiaryBook>();
        ArrayList<DiaryBook> allDiaryBooks = loadOpenAllDiaryBook();

        for (DiaryBook diaryBook : allDiaryBooks) {
            if (diaryBook.isSubscribeDiaryBook(useremail)) // 구독중인 일기책이면
                userSubscribeDiaryBooks.add(diaryBook); // 유저의 구독중인 일기책에 추가해줍니다
        }

        return userSubscribeDiaryBooks;
    }

    // 유저의 일기책 데이터를 삭제합니다
    public void removeUserDiaryBook(String diaryBookKey) {
        editor.remove(diaryBookKey);
        editor.commit();
    }

    public void clearDiaryBooks() {
        editor.clear();
        editor.commit();
    }

}