package com.whh.recordmusic.model;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by wuhuihui on 2019/3/26.
 * 服务端socket
 */
public class SocketServer {

    private final String TAG = "SocketServer";

    private ServerSocket server;
    private Socket socket;
    private InputStream in;
    private String str = null;
    public static Handler sHandler;

    /**
     * 绑定端口号 bind()
     *
     * @param port
     */
    public SocketServer(int port) {
        try {
            server = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * socket监听数据 listen()
     */
    public void beginListen() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socket = server.accept(); //接收请求 accept()
                    if (socket != null) {
                        in = socket.getInputStream();
                        while (socket.isConnected()) {
                            byte[] bt = new byte[50];
                            in.read(bt);
                            str = new String(bt, "UTF-8"); //编码方式  解决收到数据乱码
                            if (str != null && str != "exit") {
                                receiveMessage(socket.getInetAddress() + "==>" + str + "\n");
                            } else if (str == null && str == "exit") {
                                break;  //跳出循环结束socket数据接收
                            }

                            Log.i(TAG, "接收消息==>" + str);
                        }
                    } else Log.i(TAG, "socket is null");
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        socket.close();
                        Log.i(TAG, "关闭连接");
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
    }

    /**
     * socket服务器端发送消息
     */
    public void sendMessage(final String chat) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        out.print(chat);
                        out.flush();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    /**
     * socket服务端接收数据并显示
     *
     * @param chat
     */
    public void receiveMessage(String chat) {
        Log.i(TAG, "接收到客户端的消息==>" + chat);
        Message msg = new Message();
        msg.obj = chat;
        sHandler.sendMessage(msg);
    }
}

