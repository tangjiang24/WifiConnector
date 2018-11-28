#  WifiConnector

[![](https://jitpack.io/v/tangjiang24/WifiConnector.svg)](https://jitpack.io/#tangjiang24/WifiConnector)  

一个快速连接WiFi功能的库。

目前支持的功能有：

- 给定WiFi名称，连接无密码的网络

- 给定WiFi名称 和密码，连接网络 （需指定密码类型 wpa 或者web ）

## 截图

![](https://github.com/tangjiang24/WifiConnector/blob/master/pic/1.jpg)

##   用法

####   step1

在根`build.gradle`中添加：

```
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```

在使用的模块`build.gradle`中添加：

```
dependencies {
    implementation 'com.github.tangjiang24:WifiConnector:Tag'
}
```

(请替换 Tag 为最新的版本号: [![](https://jitpack.io/v/tangjiang24/WifiConnector.svg)](https://jitpack.io/#tangjiang24/WifiConnector))

####   Step2

将需要连接的activity继承 库中的  WifiActivity ：

```
public class WifiConnectActivity extends WifiActivity{}
```

#### Step3

直接调用父类中的 connectWifi(String ssid, String pwd, int type) 方法：

```
 connectWifi(String ssid,String pwd,int type)；
```

其中ssid为 WiFi名称，pwd 为WiFi密码 ，type为密码三种类型之一。 

三种密码类型：

1.  WifiUtil.TYPE_NO_PWD（无密码）
2.  WifiUtil.TYPE_WEB  (WEB)
3.  WifiUtil.TYPE_WPA（WPA）

#### Step4

重写连接失败成功的抽象方法即可：

```
 @Override
    public void onConnectWifiSucess() {
        ToastUtil.showShortToast(this,"连接成功！！");
    }

    @Override
    public void onConnectWifiFail(String failMsg) {
        ToastUtil.showShortToast(this,failMsg);
    }
```

## 注意：由于Java的但继承性，目前通过继承的方法有很大的局限。以后待改进。

# 欢迎star！
