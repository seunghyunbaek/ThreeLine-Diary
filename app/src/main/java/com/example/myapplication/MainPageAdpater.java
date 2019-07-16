package com.example.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

public class MainPageAdpater extends FragmentStatePagerAdapter {

    // 프래그먼트로 데이터 보내줄 번들
    private Bundle bundle;

    public MainPageAdpater(FragmentManager fm, String useremail) {
        super(fm);
        bundle = new Bundle();
        bundle.putString("useremail", useremail); // 각 프래그먼트에서 useremail값을 알 수 있음
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
//        return super.getItemPosition(object);
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        // return이 null일경우 앱이 터지게 된다
        switch (position) {
            case 0:
                MainFragment mainFragment = new MainFragment();
                mainFragment.setArguments(bundle);
                return mainFragment;
            case 1:
                SubscribeFragment subscribeFragment = new SubscribeFragment();
                subscribeFragment.setArguments(bundle);
                return subscribeFragment;
            case 2:
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                return profileFragment;
            default: // null이 리턴될 경우 터지게 된다
                SubscribeFragment subscribeFragment4 = new SubscribeFragment();
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
