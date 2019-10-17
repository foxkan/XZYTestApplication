package com.xzytestapplication.xzytestapp;



import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.view.View;
import android.os.PowerManager;
import android.app.KeyguardManager;

import android.os.Looper;
import android.os.Message;
import android.os.Handler;

public class LockActivity extends Activity {
    ComponentName componentName;
    DevicePolicyManager policyManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        componentName = new ComponentName(LockActivity.this,
                DeviceReceiver.class);
        policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
    }
    private void mylock() {
        boolean active = policyManager.isAdminActive(componentName);
        if (!active) { // Without permission
            activeManage(); // To get access             
        }
        active = policyManager.isAdminActive(componentName);
        if (active) {
            policyManager.lockNow();
            //this.finish();
        }
		handler.sendEmptyMessageDelayed(1,3000);
    }
	
	 private Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
					wakeup();
                    break;
            }
        }
    };
	
	private void wakeup(){
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        KeyguardManager.KeyguardLock kl = km.newKeyguardLock("unlock");
        kl.disableKeyguard();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock pw = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK,"bright");
        pw.acquire();
        pw.release();
    }

    private void activeManage() {

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "developers：1900");
        startActivity(intent);
    }

    public void lockNow(View view) {
        mylock();
    }



    public void unregister(View view) {
        policyManager.removeActiveAdmin(componentName);//注销系统组件
        this.finish();
    }
}
