package com.tj24.easywifi.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.List;

/**
 * Created by energy on 2017/11/16.
 */

public class WifiUtil {
    private final String TAG = "wifi:";
    //密码类型
    public static final int TYPE_NO_PWD = 1;
    public static final int TYPE_WEB = 2;
    public static final int TYPE_WPA = 3;

    private Context context;
    // 定义WifiManager对象
    private WifiManager mWifiManager;
    // 定义WifiInfo对象
    private WifiInfo mWifiInfo;
    // 扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    // 网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    // 定义一个WifiLock
    WifiManager.WifiLock mWifiLock;

    // 构造器
    public WifiUtil(Context context) {
        this.context = context;
        // 取得WifiManager对象
        mWifiManager = (WifiManager) context
                .getSystemService(Context.WIFI_SERVICE);
        // 取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }

    /**
     * 打开WiFi
     * @return true 打开成功  false 打开失败
     */
    @SuppressLint("WrongConstant")
    public boolean openWifi() {
        boolean sucess = true;
        if (!mWifiManager.isWifiEnabled()) {
            sucess =  mWifiManager.setWifiEnabled(true);
        }
        return sucess;
    }

    /**
     * 关闭WiFi
     * @param context
     */
    public boolean closeWifi(Context context) {
        boolean sucess = true;
        if (mWifiManager.isWifiEnabled()) {
            sucess =  mWifiManager.setWifiEnabled(false);
        }
        return sucess;
    }

    /**
     * 获取当前的WiFi状态
     * @return
     */
    public int getWifiState(){
        return mWifiManager.getWifiState();
    }
    // 检查当前WIFI状态
    private void checkState(Context context) {
        if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLING) {
            Toast.makeText(context,"Wifi正在关闭", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
            Toast.makeText(context,"Wifi已经关闭", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
            Toast.makeText(context,"Wifi正在开启", Toast.LENGTH_SHORT).show();
        } else if (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            Toast.makeText(context,"Wifi已经开启", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,"没有获取到WiFi状态", Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 扫描WiFi网络
     */
    public  boolean   startScan() {
        return   mWifiManager.startScan();
    }

    // 得到网络列表
    public List<ScanResult> getScanResults() {
        mWifiList = mWifiManager.getScanResults();
        return mWifiList;
    }

    // 得到WifiInfo的所有信息包
    public String getWifiInfo() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }
    // 得到WifiInfo的所有信息包
    public String getSSID() {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
    }

    // 添加一个网络并连接
    private boolean addNetwork(WifiConfiguration wcg) {
        if(wcg==null){
            return false;
        }
        int wcgID = mWifiManager.addNetwork(wcg);
        boolean b =  mWifiManager.enableNetwork(wcgID, true);
        return b;
    }

    /**
     * 通过ssid pwd type连接wifi
     */
    public boolean connectWifi(String ssid,String pwd ,int type){
        if (!this.openWifi()) {
            return false;
        }
        long timeMills = System.currentTimeMillis();
        while (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
            try {
                if(System.currentTimeMillis()-timeMills>2500) break;
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        WifiConfiguration wifiConfiguration = createWifiInfo(ssid,pwd,type);
        boolean c = false;
        c = addNetwork(wifiConfiguration);
        return c;
    }

    /**
     * 忘记某一个wifi密码
     *
     * @param targetSsid
     */
    public  void removeWifiBySsid( String targetSsid) {
        List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            if (ssid.equals("\""+targetSsid+"\"")) {
                removeWifi(wifiConfig.networkId);
            }
        }
    }

    /**
     * 断开指定的WiFi
     * @param targetSsid
     */
    public void disconnectWifi(String targetSsid ){
        List<WifiConfiguration> wifiConfigs = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wifiConfig : wifiConfigs) {
            String ssid = wifiConfig.SSID;
            if (ssid.equals("\""+targetSsid+"\"")) {
                disconnectWifi(wifiConfig.networkId);
            }
        }
    }
    // 断开指定ID的网络
    private void disconnectWifi(int netId) {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
    }

    private void removeWifi(int netId) {
        disconnectWifi(netId);
        mWifiManager.removeNetwork(netId);
        mWifiManager.saveConfiguration();
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, int Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";

        //注意：在低版本可以移除，在6.0以上如果是自己的应用之前连接过，通过removeWifi()可以移除
        //但是 如果是其他应用的configrRation 那么在本应用是不可以移除的 此时 直接连接即可
        //因此会做一个判断，判断removeWifi 后集合的数量是否减少，若没有则表示没有移除成功，直接连接即可
        WifiConfiguration tempConfig = this.IsExsits(SSID);
        List<WifiConfiguration> beforeConfig = mWifiManager.getConfiguredNetworks();
        if(tempConfig!=null) {
            removeWifi(tempConfig.networkId);
        }
        List<WifiConfiguration> afterConfig = mWifiManager.getConfiguredNetworks();
        if( tempConfig!=null && beforeConfig.size()==afterConfig.size()){
            return tempConfig;
        }else {
            if(Type == TYPE_NO_PWD) {  //WIFICIPHER_NOPASS
                //                config.wepKeys[0] = "";
                //                config.wepTxKeyIndex = 0;
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }
            if(Type == TYPE_WEB){ //WIFICIPHER_WEP
                config.hiddenSSID = true;
                config.wepKeys[0]= "\""+Password+"\"";
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                config.wepTxKeyIndex = 0;
            }
            if(Type == TYPE_WPA){ //WIFICIPHER_WPA
                config.preSharedKey = "\""+Password+"\"";
                config.hiddenSSID = true;
                config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                //config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.status = WifiConfiguration.Status.ENABLED;
            }
            return config;
        }
    }

    private WifiConfiguration IsExsits(String SSID) {
        List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
        if(existingConfigs!=null ){
            for (WifiConfiguration existingConfig : existingConfigs) {
                if (existingConfig.SSID.equals("\""+SSID+"\"")) {
                    return existingConfig;
                }
            }
        }
        return null;
    }

    public  boolean isWiFiConnected(){
        ConnectivityManager connectManager =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(networkInfo.isConnected()){
            return true;
        }
        else{
            return false;
        }
    }
}
