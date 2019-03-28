package com.whh.recordmusic.utils;

/**
 * Created by wuhuihui on 2019/3/27.
 * 消息监听
 */
public interface OnMessageListener {
    void sendMsg(boolean isSucessful);

    void receive(String message);
}
