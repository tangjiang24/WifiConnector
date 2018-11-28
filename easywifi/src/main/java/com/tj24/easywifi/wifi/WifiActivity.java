package com.tj24.easywifi.wifi;

import android.Manifest;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public abstract class WifiActivity extends AppCompatActivity implements WifiConnectReceiver.onWifiConnectListner {
    private final String TAG = "wifi:";
    private WifiConnectReceiver wifiConnectReceiver;
    private WifiUtil mWifiUtil;
    String ssid;
    String pwd;
    int type;
    private boolean mReceiverTag = false;   //广播接受者标识
    public static final int CONNECT_WIFI_SUCESS = 1;
    public static final int CONNECT_WIFI_FAIL = 2;
    private boolean isFirstFail = true;//是否是第一次连接失败  某些条件下需要重连
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==CONNECT_WIFI_SUCESS){
                Log.i(TAG,"连接成功！");
                onConnectWifiSucess();
                unRegistWifiReceiver();
            }else if(msg.what==CONNECT_WIFI_FAIL){
                Log.i(TAG,"连接失败！原因："+msg.obj);
                String failMsg = (String) msg.obj;
                onConnectWifiFail(failMsg);
                unRegistWifiReceiver();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWifiUtil = new WifiUtil(this);
    }

    public abstract void onConnectWifiSucess();
    public abstract void onConnectWifiFail(String failMsg);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unRegistWifiReceiver();
    }
    @Override
    public void isConnectMyWifi(String s) {
        if (!TextUtils.isEmpty(ssid)) {
            if (s.equals("\"" + ssid + "\"")) {
                handler.removeMessages(CONNECT_WIFI_SUCESS);
                handler.removeMessages(CONNECT_WIFI_FAIL);
                handler.sendEmptyMessageDelayed(CONNECT_WIFI_SUCESS,1000);
            } else {
                Log.i(TAG,"未连接到指定网络！");
                sendFailMessage("未连接到指定网络！",1);
            }
        }
    }

    private void registWifiConnectReceiver() {
        if (!mReceiverTag) {
            wifiConnectReceiver = new WifiConnectReceiver();
            mReceiverTag = true;    //标识值 赋值为 true 表示广播已被注册
            wifiConnectReceiver.setOnWifiConnetListner(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            registerReceiver(wifiConnectReceiver, intentFilter);
        }
    }

    public void connectWifi(String ssid,String pwd,int type){
        registWifiConnectReceiver();
        handler.removeMessages(CONNECT_WIFI_SUCESS);
        handler.removeMessages(CONNECT_WIFI_FAIL);

        WifiUtil wifiUtil = new WifiUtil(this);
        if(wifiUtil.isWiFiConnected() && ("\"" + ssid + "\"").equals(wifiUtil.getSSID())){
            handler.sendEmptyMessage(CONNECT_WIFI_SUCESS);
            return;
        }
        this.ssid = ssid;
        this.pwd = pwd;
        this.type = type;

        if(Build.VERSION.SDK_INT >= 23){
            checkLocatePermission();
        }else {
            startConnect(type);
        }
    }

    private void startConnect(int type) {
        boolean isConnected = mWifiUtil.connectWifi(ssid, pwd, type);
        if(!isConnected) {
            sendFailMessage("连接失败，请重新连接！");
        }else {
            sendFailMessage("连接超时！",12000);
        }
    }

    private void unRegistWifiReceiver(){
        if (mReceiverTag) {   //判断广播是否注册
            mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
            this.unregisterReceiver(wifiConnectReceiver);   //注销广播
        }
    }

    /**
     * 检测GPS、位置权限
     */
    private void checkLocatePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            openGPS();
        }
    }

    private void openGPS(){
        LocationManager lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!ok) {
            Toast.makeText(this, "请打开gps", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 2);
        }else {
            startConnect(type);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==1){
            startConnect(type);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==2){
            startConnect(type);
        }
    }

    private void sendFailMessage(String failMsg){
        if(isFirstFail){
            isFirstFail = !isFirstFail;
            Log.i(TAG,"第一次连接失败，开始第二次连接");
            connectWifi(ssid,pwd,type);
            return;
        }
        Message msg = handler.obtainMessage();
        msg.obj = failMsg;
        msg.what = CONNECT_WIFI_FAIL;
        handler.sendMessage(msg);
    }

    private void sendFailMessage(String failMsg,long delay){
        Message msg = handler.obtainMessage();
        msg.obj = failMsg;
        msg.what = CONNECT_WIFI_FAIL;
        handler.sendMessageDelayed(msg,delay);
    }
}
