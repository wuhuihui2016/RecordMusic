package com.whh.recordmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by wuhuihui on 2019/3/26.
 * 用于连接，接受发送数据
 */
public class SocketUtils {

    private static final String TAG = "SocketUtils";

    private static int port = 5555;
    public static Socket target; //另一手机
    public static String targetIP;  //另一手机IP

    /**
     * 通过IP连接另一个手机，实现互通
     *
     * @param inetAddress  另一手机的IP，默认端口5555
     * @param connListener
     */
    public static void connect(final String inetAddress, final OnConnectListener connListener,
                               final OnMessageListener msgListener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    target = new Socket(inetAddress, port);
                    target.setSoTimeout(5000); //设置超时时间
                    if (target != null) {
                        connListener.onConnect(true);
                        targetIP = inetAddress;
                        sendMsg(inetAddress + " connect is sucessful!", new OnMessageListener() {
                            @Override
                            public void sendMsg(boolean isSucessful) {
                                if (isSucessful) {
                                    //接收消息
                                    while (target.isConnected()) {
                                        try {
                                            InputStream in = target.getInputStream();
                                            //得到的是16位进制数，需要解析
                                            byte[] bt = new byte[50];
                                            in.read(bt);
                                            String message = new String(bt, "UTF-8");
                                            if (message != null) {
                                                msgListener.receive(message);
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    }
                                }
                            }

                            @Override
                            public void receive(String message) {

                            }
                        });



                    } else connListener.onConnect(false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 发送消息
     */
    public static void sendMsg(final String message, final OnMessageListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (target != null && target.isConnected()) {
                    try {
                        PrintWriter out = new PrintWriter(target.getOutputStream());
                        out.print(message);
                        out.flush();
                        Log.i(TAG, "发送消息==>" + message);
                        listener.sendMsg(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        listener.sendMsg(false);
                        Log.i(TAG, "消息发送失败，请检查连接是否正常！");
                    }

                } else {
                    listener.sendMsg(false);
                    Log.i(TAG, "消息发送失败，请检查连接是否正常！");
                }
            }
        }).start();
    }


    /**
     * 获取手机IP
     *
     * @return
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    public static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }


    //隐藏键盘
    public static void hideInput(Activity activity) {
        ((InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }



}
