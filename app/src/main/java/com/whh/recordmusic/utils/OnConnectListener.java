package com.whh.recordmusic.utils;

import java.net.Socket;

/**
 * Created by wuhuihui on 2019/3/27.
 * 连接监听
 */
public interface OnConnectListener {
    void onConnect(Socket socket, boolean isConnected);
}
