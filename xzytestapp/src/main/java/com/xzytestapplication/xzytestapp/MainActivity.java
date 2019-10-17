package com.xzytestapplication.xzytestapp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.junger.aidl.IPowerKeyInterface;
import com.junger.aidl.mNFCAidlInterface;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver();
        initAidl();
        initUI();
    }
/*
git 测试上传
 */
/////////////////////////////
    private void initUI() {
        tv_versions = findViewById(R.id.tv_versions);
        et_hour = findViewById(R.id.et_hour);
        et_min = findViewById(R.id.et_min);
        et_package = findViewById(R.id.et_package);

        tv_versions.setText("AHF005-20190928_tb8766p1_bsp_user_starmini-v1.5");
    }

    private TextView tv_versions;
    private EditText et_hour,et_min,et_package;
    private void initAidl() {
        Intent intent = new Intent();
        intent.setAction("com.junger.aidl.MyPowerKeyService.ACTION");
        intent.setPackage("com.android.settings");
        bindService(intent, connection, Context.BIND_AUTO_CREATE);

        Intent intent1 = new Intent();
        intent1.setAction("com.junger.aidl.MyNFCService.ACTION");
        intent1.setPackage("com.android.settings");
        bindService(intent1, mNFCconnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        if (connection != null) {
            unbindService(connection);
        }
        if (mNFCconnection != null) {
            unbindService(mNFCconnection);
        }
        super.onDestroy();
    }


    private void registerReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("gpt_short_press_power_action");
        filter.addAction("gpt_long_press_power_action");
        filter.addAction("gpt_call_center_custom_action");
        filter.addAction("gpt_first_press_custom_action");
        filter.addAction("gpt_second_press_custom_action");
      /*  filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addAction("android.intent.action.PACKAGE_REPLACED");
        filter.addAction("android.intent.action.PACKAGE_ADDED_FAIL");
*/
        registerReceiver(mReceiver,filter);
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            ToastMe(action+intent.getStringExtra("packageName"));
        }
    };

    private void ToastMe(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_LONG).show();
    }

    public void gotosleep(View view) {
         try {
            mService.goToSleep();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        handler.sendEmptyMessageDelayed(1,3000);

    }

    public void setShutAlarm(View view) {
        int hour = 0;
        int min =0;

        if (TextUtils.isEmpty(et_hour.getText()) ){
            ToastMe("时间需要指定");
            return;
        }

        hour= Integer.parseInt(et_hour.getText().toString().trim());

        if (!TextUtils.isEmpty(et_min.getText()) ){
            min= Integer.parseInt(et_min.getText().toString().trim());
        }
        Intent intent =new Intent();
        intent.setPackage("com.mediatek.schpwronoff");
        intent.setAction("com.starmini.xzy.alarm.SET_ALARM");
        intent.putExtra("_enabled",true);
        intent.putExtra("_hour",hour);
        intent.putExtra("_minutes",min);
        intent.putExtra("id",2);//shutdown
        sendBroadcast(intent);
    }
    // mBackReceiver  com.starmini.xzy.back

    public void setBootAlarm(View view) {
        int hour = 0;
        int min =0;

        if (TextUtils.isEmpty(et_hour.getText()) ){
            ToastMe("时间需要指定");
            return;
        }
        try {
            hour = Integer.parseInt(et_hour.getText().toString().trim());
            if (!TextUtils.isEmpty(et_min.getText())) {
                min = Integer.parseInt(et_min.getText().toString().trim());
            }
        }catch (Exception e){
            ToastMe("时间格式有错误");
        }
        Intent intent =new Intent();
        intent.setPackage("com.mediatek.schpwronoff");
        intent.setAction("com.starmini.xzy.alarm.SET_ALARM");
        intent.putExtra("_enabled",true);
        intent.putExtra("_hour",hour);
        intent.putExtra("_minutes",min);
        intent.putExtra("id",1);//boot
        sendBroadcast(intent);
    }

    public void newWhiteFile(View view) {
        String msg =null;
        if (TextUtils.isEmpty(et_package.getText()) ){
            ToastMe("包名不能为空");

            return;
        }
        msg = et_package.getText() .toString().trim();

        updateFile(msg,false);
    }

    public void addWhiteFile(View view) {
        String msg =null;

        if (TextUtils.isEmpty(et_package.getText()) ){
            ToastMe("包名不能为空");
            return;
        }
        msg = et_package.getText() .toString().trim();
        //msg ="com.example.yhao.fixedfloatwindow";
        updateFile(msg,true);
    }


    public void getWhiteFile(View view) {
        String msg = getWhiteFileString();
        if (msg!=null){
            ToastMe(msg);
        }

    }

    private  final  Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    try {
                        mService.wakeUp();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    private IPowerKeyInterface mService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IPowerKeyInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    //isOverride，true为追加，flase为覆盖
    public void updateFile(String msg,boolean isAdd) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/whiteList/"+"whitelistapps";
        File file = new File(path);
        FileOutputStream fos = null;
        BufferedWriter bw = null;

        File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +"/whiteList");
        if (!f.exists())
            f.mkdirs();
        if (!f.exists()){
            ToastMe("白名单文件夹创建失败");
            return;
        }
        if (!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            if (!file.exists())
            {

                ToastMe("白名单文件创建失败");
                return;
            }
            fos = new FileOutputStream(path, isAdd);
            bw = new BufferedWriter(new OutputStreamWriter(fos));
            bw.write(msg+"\r\n");
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (bw != null) {
                    bw.close(); //关闭缓冲流
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getWhiteFileString(){
        read();
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/whiteList/"+"whitelistapps";
        File file = new File(path);
        if (!file.exists()){
            return "文件不存在";
        }
        List<String> arr1 = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(path));
            String contentLine ;
            while ((contentLine = br.readLine()) != null) {
                arr1.add(contentLine);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return arr1.toString();
    }

    public void unInstall(View view) {
        Intent intent = new Intent("com.starmini.ACTION_UNINSTALL_APK");
        intent.putExtra("packageName","com.xzytestapplication.xzytestapp");//需要卸载的apk包名
        intent.setPackage("com.android.systemui");
        sendBroadcast(intent);
    }

    public void goBack(View view) {
        //com.starmini.xzy.back
        Intent intent = new Intent("com.starmini.xzy.back");
        sendBroadcast(intent);
    }

    public void install(View view) {
        Intent intent = new Intent("com.starmini.ACTION_INSTALL_APK");
        intent.putExtra("path",Environment.getExternalStorageDirectory().getAbsolutePath()+"/my.apk"); //存放my.apk的绝对路径
        intent.setPackage("com.android.packageinstaller");
        sendBroadcast(intent);
    }

    /**
     * 恢复出厂设置
     * @param view
     */
    public void reset(View view){
        Intent intent = new Intent("com.starmini.action.reset");
        intent.setPackage("com.android.settings");
        sendBroadcast(intent);
    }

    /*** 关闭Android Beam      */
    public void setAndroidBeam_OFF(View view) {
        Intent intent = new Intent("com.starmini.action.android_beam_off");
        intent.setPackage("com.android.settings");
        sendBroadcast(intent);
    }
    /*** 打开Android Beam      */
    public void setAndroidBeam_ON(View view) {
        Intent intent = new Intent("com.starmini.action.android_beam_on");
        intent.setPackage("com.android.settings");
        sendBroadcast(intent);
    }

    /*** 关闭底部导航栏      */
    public void setNV_OFF(View view) {
        Intent intent = new Intent("com.starmini.action.nv_off");
        intent.setPackage("com.android.settings");
        sendBroadcast(intent);
    }

    /*** 打开底部导航栏      */
    public void setNV_ON(View view) {
        Intent intent = new Intent("com.starmini.action.nv_on");
        intent.setPackage("com.android.settings");
        sendBroadcast(intent);
    }

    /*** 打开settigns      */
    public void gotoSettings(View view) {
        startAct();
    }
    private void startAct() {
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.android.settings",
                    "com.android.settings.Settings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            startActivity(intent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void gotoMTKlogger(View view) {
        startMTKlogger();
    }
    private void startMTKlogger() {
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.mediatek.mtklogger",
                    "com.mediatek.mtklogger.MainActivity");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

            startActivity(intent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void gotoNFCapp(View view) {
        try{
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            ComponentName cn = new ComponentName("com.nxp.taginfolite",
                    "com.nxp.taginfo.activities.MainView");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(intent);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setDefault(View view){
        Intent intent1 =new Intent();
        intent1.setPackage("com.android.settings");
        intent1.setAction("com.starmini.action.setdefault");
        sendBroadcast(intent1);
    }

  /*  public void getAlarm(View view) {
        Intent intent = new Intent("com.starmini.xzy.alarm.queryAlarm");
        intent.setPackage("com.mediatek.schpwronoff");
        sendBroadcast(intent);
    }
*/
    private void read(){
        String sdPath = "/storage/emulated/0/whiteList/whitelistapps";
        File sdFile = new File(sdPath);
        final ArrayList<String> whiteListApps = new ArrayList<String>();
        try {
            whiteListApps.clear();
            FileReader fr = new FileReader(sdFile);
            BufferedReader br = new BufferedReader (fr);
//				BufferedReader br = new BufferedReader (new FileReader(whiteListFile));
            String line = "";
            while ((line=br.readLine())!=null) {
                whiteListApps.add(line);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            Log.e("1900", "IO Exception happened while readind whiteListApps");
            e.printStackTrace();

        }
    }

    public void nfc_on(View view) {
        NfcAdapter adapter =NfcAdapter.getDefaultAdapter(this);
        if(!adapter.isEnabled()){
            nfcEnable();
        }
    }

    public void nfc_off(View view){
        NfcAdapter adapter =NfcAdapter.getDefaultAdapter(this);
        if(adapter.isEnabled()){
            nfcDisable();
        }
    }

    public void getNFCStatus(View view){
        NfcAdapter adapter =NfcAdapter.getDefaultAdapter(this);
        ToastMe("nfc 处于"+(adapter.isEnabled()?"开启状态":"关闭状态"));
    }

    private void nfcEnable() {
        try {
            mNFC.enable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private void nfcDisable() {
        try {
            mNFC.disable();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    private mNFCAidlInterface mNFC;
    private ServiceConnection mNFCconnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mNFC = mNFCAidlInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mNFC = null;
        }
    };


    public void lockScreen(View view) {
        startActivity(new Intent(MainActivity.this,LockActivity.class));
    }
}
