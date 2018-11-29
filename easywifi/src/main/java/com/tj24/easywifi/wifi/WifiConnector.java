package com.tj24.easywifi.wifi;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

/**
 * Created by energy on 2018/11/29.
 */

public class WifiConnector implements WifiConnectReceiver.onWifiConnectListner  {
    private final String TAG = "wifi:";
    private String ssid;
    private String pwd;
    private int type;
    private Context context;
    private WifiConnectReceiver wifiConnectReceiver;
    private WifiUtil mWifiUtil;
    private WifiConnectCallBack callBack;
    private boolean mReceiverTag = false;   //广播接受者标识
    public static final int CONNECT_WIFI_SUCESS = 1;
    public static final int CONNECT_WIFI_FAIL = 2;
    private boolean isFirstFail = true;//是否是第一次连接失败  某些条件下需要重连

    public WifiConnector(Context context) {
        this.context = context;
        mWifiUtil = new WifiUtil(context);
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==CONNECT_WIFI_SUCESS){
                Log.i(TAG,"连接成功！");
                callBack.onConnectSucess();
                unRegistWifiReceiver();
            }else if(msg.what==CONNECT_WIFI_FAIL){
                Log.i(TAG,"连接失败！原因："+msg.obj);
                String failMsg = (String) msg.obj;
                callBack.onConnectFail(failMsg);
                unRegistWifiReceiver();
            }
        }
    };

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

    public void connectWifi(String ssid,String pwd,int type,WifiConnectCallBack callBack){
        registWifiConnectReceiver();
        handler.removeMessages(CONNECT_WIFI_SUCESS);
        handler.removeMessages(CONNECT_WIFI_FAIL);

        this.ssid = ssid;
        this.pwd = pwd;
        this.type = type;
        this.callBack = callBack;

        WifiUtil wifiUtil = new WifiUtil(context);
        Log.i(TAG,"是否已经是WiFi连接："+wifiUtil.isWiFiConnected()+"已连接的ssid:"+wifiUtil.getSSID());
        if(wifiUtil.isWiFiConnected() && ("\"" + ssid + "\"").equals(wifiUtil.getSSID())){
            handler.sendEmptyMessage(CONNECT_WIFI_SUCESS);
            return;
        }

        if(Build.VERSION.SDK_INT >= 23){
            checkLocatePermission();
        }else {
            startConnect();
        }
    }

    private void startConnect() {
        boolean isConnected = mWifiUtil.connectWifi(ssid, pwd, type);
        if(!isConnected) {
            sendFailMessage("连接失败，请重新连接！");
        }else {
            sendFailMessage("连接超时！",12000);
        }
    }

    private void registWifiConnectReceiver() {
        if (!mReceiverTag) {
            wifiConnectReceiver = new WifiConnectReceiver();
            mReceiverTag = true;    //标识值 赋值为 true 表示广播已被注册
            wifiConnectReceiver.setOnWifiConnetListner(this);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            context.registerReceiver(wifiConnectReceiver, intentFilter);
        }
    }

    private void unRegistWifiReceiver(){
        if (mReceiverTag) {   //判断广播是否注册
            mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
            context.unregisterReceiver(wifiConnectReceiver);   //注销广播
        }
    }


    /**
     * 检测GPS、位置权限
     */
    private void checkLocatePermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            openGPS();
        }
    }

    private void openGPS(){
        LocationManager lm = (LocationManager) context.getSystemService(context.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!ok) {
            Toast.makeText(context, "请打开gps", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            ((Activity) context).startActivityForResult(intent, 2);
        }else {
            startConnect();
        }
    }

    private void sendFailMessage(String failMsg){
        if(isFirstFail){
            isFirstFail = !isFirstFail;
            Log.i(TAG,"第一次连接失败，开始第二次连接");
            connectWifi(ssid,pwd,type,callBack);
            return;
        }
        handler.removeMessages(CONNECT_WIFI_FAIL);
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

    public interface WifiConnectCallBack{
        public void onConnectSucess();
        public void onConnectFail(String msg);
    }
}
