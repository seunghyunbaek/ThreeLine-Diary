package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ExampleBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
//            boolean noConnectivity = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
//            if (noConnectivity) {
//                Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show();
//            }
//            Log.i("Example", "CONNECT: ");
//        }

        if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
            boolean noLow = intent.getBooleanExtra(Intent.ACTION_BATTERY_LOW, false);
//            if(noLow) {
//                Toast.makeText(context, "배터리부족", Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(context, "충전이 필요합니다", Toast.LENGTH_LONG).show();
//            }
            if (!noLow) {
                Toast.makeText(context, "충전이 필요합니다", Toast.LENGTH_LONG).show();
            }
            Log.i("Example", "BATTERY: ");

        }
    }
}
