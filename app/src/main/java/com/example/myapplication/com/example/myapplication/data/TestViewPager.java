package com.example.myapplication.com.example.myapplication.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewPager;

public class TestViewPager extends ViewPager {
    public TestViewPager(@NonNull Context context) {
        super(context);
    }

    @Override
    public void setCurrentItem(int item) {
        super.setCurrentItem(item);
    }

    @Override
    public void setOffscreenPageLimit(int limit) {
        super.setOffscreenPageLimit(limit);
    }
}
