package com.whh.recordmusic.model;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.whh.recordmusic.utils.OnMessageListener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
    public static Socket socket; //用户端
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
                                receiveMessage(socket.getInetAddress() + "：" + str + "\n");
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
    public void sendMessage(final OnMessageListener listener, final String chat) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (socket != null) {
                        PrintWriter out = new PrintWriter(socket.getOutputStream());
                        out.print(chat);
                        out.flush();
                        listener.sendMsg(true);
                    } else listener.sendMsg(false);
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.sendMsg(false);
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

    /**
     * 接收文件
     */
    public void receiveFile() {
        try {
            InputStream nameStream = socket.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(nameStream);
            BufferedReader br = new BufferedReader(streamReader);
            String fileName = br.readLine();
            br.close();
            streamReader.close();
            nameStream.close();
            socket.close();
            sendMessage(null, "正在接收:" + fileName);

            InputStream dataStream = socket.getInputStream();
            String savePath = Environment.getExternalStorageDirectory().getPath() + "/" + fileName;
            FileOutputStream file = new FileOutputStream(savePath, false);
            byte[] buffer = new byte[1024];
            int size = -1;
            while ((size = dataStream.read(buffer)) != -1) {
                file.write(buffer, 0, size);
            }
            file.close();
            dataStream.close();
            socket.close();
            sendMessage(null,fileName + "接收完成");
        } catch (Exception e) {
            sendMessage(null, "接收错误:\n" + e.getMessage());
        }
    }

    /**
     * 发送文件
     *
     * @param fileName
     * @param path
     */
    public void sendFile(String fileName, String path) {
        try {
            OutputStream outputName = socket.getOutputStream();
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputName);
            BufferedWriter bwName = new BufferedWriter(outputWriter);
            bwName.write(fileName);
            bwName.close();
            outputWriter.close();
            outputName.close();
            socket.close();
            sendMessage(null, "正在发送" + fileName);

            OutputStream outputData = socket.getOutputStream();
            FileInputStream fileInput = new FileInputStream(path);
            int size = -1;
            byte[] buffer = new byte[1024];
            while ((size = fileInput.read(buffer, 0, 1024)) != -1) {
                outputData.write(buffer, 0, size);
            }
            outputData.close();
            fileInput.close();
            socket.close();
            sendMessage(null, fileName + "  发送完成");
            sendMessage(null, "文件发送完成!");
        } catch (Exception e) {
            sendMessage(null, "发送错误:\n" + e.getMessage());
        }
    }
}

