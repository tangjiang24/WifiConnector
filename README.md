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

分两种方法：

##### 第一种：

1.将需要连接的activity继承 库中的  WifiActivity ：

```
public class WifiConnectActivity extends WifiActivity{}
```

2.直接调用父类中的 connectWifi(String ssid, String pwd, int type) 方法：

```
 connectWifi(String ssid,String pwd,int type)；
```

3.重写连接失败成功的抽象方法即可：

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

第二种：

1.构建WifiConnector对象

```
WifiConnector connector = new WifiConnector(context);
```

2.调用其connectWifi () 方法，并在回调中写成功或者失败后的逻辑

```
connector.connectWifi(ssid, pwd, WifiUtil.TYPE_WPA, new WifiConnector.WifiConnectCallBack() {
    @Override
    public void onConnectSucess() {
        Toast.makeText(WifiConnectActivity.this,"连接成功！！",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectFail(String msg) {
        Toast.makeText(WifiConnectActivity.this,msg,Toast.LENGTH_SHORT).show();
    }
});
```

## 参数说明：

|       参数名        |        类型         |   意义    |
| :-----------------: | :-----------------: | :-------: |
|        ssid         |       String        | WiFi名称  |
|         pwd         |       String        | WiFi 密码 |
|        type         |         int         | 密码类型  |
| wifiConnectCallBack | WifiConnectCallBack | 连接回调  |

三种密码类型：

1. WifiUtil.TYPE_NO_PWD（无密码）
2. WifiUtil.TYPE_WEB  (WEB)
3. WifiUtil.TYPE_WPA（WPA）

## 注意：由于Java的单继承性，第一种方法有很大的局限，建议使用第二种方法。

## 欢迎star！ [tangjiang24](https://blog.csdn.net/m0_37103968/article/details/78778090)
