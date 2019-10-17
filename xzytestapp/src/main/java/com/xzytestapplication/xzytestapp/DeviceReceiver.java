package com.xzytestapplication.xzytestapp;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class DeviceReceiver extends DeviceAdminReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("1900","DeviceReceiver:"+intent.getAction());
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        System.out.println("已经注册成为系统组件");
        Log.e("1900","已经注册成为系统组件");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        System.out.println("已经注销了系统组件");
        Log.e("1900","已经注销了系统组件");
    }

}
