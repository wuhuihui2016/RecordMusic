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



    //隐藏键盘
    public static void hideInput(Activity activity) {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
