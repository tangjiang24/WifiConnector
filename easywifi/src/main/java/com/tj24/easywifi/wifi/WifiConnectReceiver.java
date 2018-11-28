package com.tj24.easywifi.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Created by energy on 2018/11/9.
 */

public class WifiConnectReceiver extends BroadcastReceiver{
    private final String TAG = "wifi:";
    boolean isFirst = true;
    private onWifiConnectListner mListner;
    public WifiConnectReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals( WifiManager.NETWORK_STATE_CHANGED_ACTION)){
            if(isFirst){
                isFirst = !isFirst;
                return;
            }
            mListnerDo(context,intent);
        }
    }

    private void mListnerDo(Context context,Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        Log.i(TAG, "接受到网络连接变化的广播，当前网络状态为："+info.getState());
        if(info.getState().equals(NetworkInfo.State.CONNECTED)){
            WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();  //连接到的网络的ssid
            Log.i(TAG, "接受到网络连接变化的广播，已连接的网络的ssid="+ssid);
            mListner.isConnectMyWifi(ssid);
        }
    }

    public interface onWifiConnectListner{
        public void isConnectMyWifi(String ssid);
    }
    public void setOnWifiConnetListner(onWifiConnectListner listner){
        this.mListner = listner;
    }
}
