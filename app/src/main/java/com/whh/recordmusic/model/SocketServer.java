package com.whh.recordmusic.model;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.whh.recordmusic.utils.OnMessageListener;

import java.io.DataInputStream;
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
    public static Socket socket; //用户端
    private InputStream in;
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
                    if (server != null) {
                        socket = server.accept(); //接收请求 accept()
                        if (socket != null) {
                            in = socket.getInputStream();
                            while (socket.isConnected()) {
                                Log.i(TAG, "Server接收消息。。。");
                                byte[] bt = new byte[50];
                                in.read(bt);
                                receiveMessage(socket.getInetAddress() + "：" + new String(bt, "UTF-8") + "\n");

                                receiveAudioData(socket); //接收音频数据包

                            }
                        } else Log.i(TAG, "socket is null");
                    }
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
     * 接收audio数据
     * @param socket
     */
    public void receiveAudioData(final Socket socket) {
        try {
            Log.i(TAG, "开始接收音频数据。。。" + socket.getRemoteSocketAddress());
            byte[] buffer = new byte[50];

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            dis.read(buffer);

            Log.i(TAG, "开始接收音频数据。。。" + dis.read(buffer));

            //播放音频数据
            int mMinBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);//计算最小缓冲区
            AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize, AudioTrack.MODE_STREAM);
            while (dis.read(buffer) != -1) {
                Log.i(TAG, "播放音频数据。。。" + dis.read(buffer));
                audioTrack.play();
                audioTrack.write(buffer, 0, buffer.length);
            }
            audioTrack.stop();
            audioTrack.release();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

