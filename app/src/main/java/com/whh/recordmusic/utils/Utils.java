package com.whh.recordmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

import com.whh.recordmusic.model.SocketServer;

import java.net.Socket;

/**
 * Created by wuhuihui on 2019/3/26.
 */

public class Utils {

    public static int port = 5555;

    public static final int SERVER = 0, CLIENT = 1;
    public static int ID = SERVER; //默认是服务器，0服务器，1用户端

    public static SocketServer server;
    public static Socket connSocket;
    public static String IP = "";

    //隐藏键盘
    public static void hideInput(Activity activity) {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
