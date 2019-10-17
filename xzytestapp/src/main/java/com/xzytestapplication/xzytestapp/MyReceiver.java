package com.xzytestapplication.xzytestapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("1900","action:"+intent.getAction());

        String path = intent.getStringExtra("path");
        String pak = intent.getStringExtra("packageName");


        Log.e("1900","path:"+path);
        Log.e("1900","pak:"+pak);


    }


}
