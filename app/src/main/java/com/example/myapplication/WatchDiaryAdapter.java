package com.example.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import com.example.myapplication.com.example.myapplication.data.DiaryBook;
import com.example.myapplication.com.example.myapplication.data.DiaryBookSharedPreference;

public class WatchDiaryAdapter extends FragmentStatePagerAdapter {

    private DiaryBook diaryBook;
    private Bundle bundle;

    private DiaryBookSharedPreference diaryBookSharedPreference;

    public WatchDiaryAdapter(FragmentManager fm, String useremail, DiaryBook diaryBook, DiaryBookSharedPreference diaryBookSharedPreference) {
        super(fm);
        this.diaryBook = diaryBook;
        this.diaryBookSharedPreference = diaryBookSharedPreference;

        // 프래그먼트에 넘겨줄 일기책 데이터 번들에 넣기
        bundle = new Bundle();
        bundle.putString("useremail", useremail);
        bundle.putString("diarybookkey", diaryBook.getDiaryBookKey());
    }

    public void removeDiary(int page) {
        diaryBook.getDiaries().remove(page);
        diaryBookSharedPreference.saveDiaryBook(diaryBook);
        notifyDataSetChanged();
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    public void seWatchDIaryBook(DiaryBook diaryBook) {
        this.diaryBook = diaryBook;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                WatchDiaryTitleFragment watchDiaryTitleFragment = new WatchDiaryTitleFragment(); // 일기책 보여줄 프래그먼트 생성
                watchDiaryTitleFragment.setArguments(bundle); // Fragment에 번들 데이터 넘겨주기
                return watchDiaryTitleFragment;
            default:
                WatchDiaryFragment watchDiaryFragment = new WatchDiaryFragment(); // 일기 보여줄 프래그먼트 생성
                bundle.putInt("position", position - 1);
                watchDiaryFragment.setArguments(bundle); // 프래그먼트에 번들 데이터 넘겨주기
                return watchDiaryFragment;
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return diaryBook.getDiaries().size() + 1;
    }
}
