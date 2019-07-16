package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BatteryBroadcastReceiver extends BroadcastReceiver {
    private final String TAG = "BBR";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: ");
        Toast.makeText(context, "리시버 이벤트 발생!!", Toast.LENGTH_SHORT).show();
    }
}
