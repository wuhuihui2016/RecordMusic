package com.whh.recordmusic.model;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.whh.recordmusic.utils.OnConnectListener;
import com.whh.recordmusic.utils.OnMessageListener;
import com.whh.recordmusic.utils.SocketUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by wuhuihui on 2019/3/26.
 * 客户端socket
 */
public class SocketClient implements Serializable {

    private final String TAG = "SocketClient";
    private Socket client;
    private Context context;
    private Thread thread;
    public static Handler cHandler;
    private boolean isClient = false;
    private PrintWriter out;
    private InputStream in;
    private String str;

    /**
     * 开启线程建立连接开启客户端
     */
    public void openClientThread(final Context context, final String site, final int port,
                                 final OnConnectListener listener) {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //connect()步骤
                    client = new Socket(site, port);
                    client.setSoTimeout(5000); //设置超时时间
                    if (client != null) {
                        listener.onConnect(client, true);
                        SocketUtils.targetIP = site;
                        SocketUtils.socket = client;
                        isClient = true;
                        forOut();
                        forIn();
                    } else {
                        listener.onConnect(null, false);
                        isClient = false;
                        Toast.makeText(context, "网络连接失败！", Toast.LENGTH_LONG).show();
                    }
                    Log.i(TAG, "连接地址==>" + "site=" + site + " ,port=" + port);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.i(TAG, "连接失败==>6");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.i(TAG, "连接失败==>7");
                }
            }
        });
        thread.start();
    }

    /**
     * 得到字符串
     */
    public void forOut() {
        try {
            out = new PrintWriter(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "读取失败==>8");
        }
    }

    /**
     * 得到字符串
     */
    public void forIn() {
        while (isClient) {
            try {
                in = client.getInputStream();

                //得到的是16位进制数，需要解析
                byte[] bt = new byte[50];
                in.read(bt);
                str = new String(bt, "UTF-8");

            } catch (IOException e) {
            }

            if (str != null) {
                Message msg = new Message();
                msg.obj = str;
                cHandler.sendMessage(msg);
            }
        }
    }

    /**
     * 发送消息
     */
    public void sendMsg(final String str, final OnMessageListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (client != null) {
                    out.print(str);
                    out.flush();
                    Log.i(TAG, "发送消息==>" + str);
                    listener.sendMsg(true);
                } else {
                    isClient = false;
                    listener.sendMsg(false);
                    Log.i(TAG, "消息发送失败，请检查连接是否正常！");
                }
            }
        }).start();
    }


}
