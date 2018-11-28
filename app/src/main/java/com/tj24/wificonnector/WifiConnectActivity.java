package com.tj24.wificonnector;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.tj24.easywifi.wifi.WifiActivity;
import com.tj24.easywifi.wifi.WifiUtil;

public class WifiConnectActivity extends WifiActivity {
    String ssid = "AndroidAP_1579";
    String pwd = "12345678";


    EditText etSsid;
    EditText etPwd;
    Button btnConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etSsid = findViewById(R.id.et_ssid);
        etPwd = findViewById(R.id.et_pwd);
        btnConnect = findViewById(R.id.btn_connect);
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ssid = etSsid.getText().toString();
                pwd = etPwd.getText().toString();
                connectWifi(ssid,pwd, WifiUtil.TYPE_WPA);
            }
        });
    }

    @Override
    public void onConnectWifiSucess() {
        Toast.makeText(this,"连接成功！！",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectWifiFail(String failMsg) {
        Toast.makeText(this,failMsg,Toast.LENGTH_SHORT).show();
    }

}
