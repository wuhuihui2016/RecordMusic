package com.whh.recordmusic.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by wuhuihui on 2019/3/26.
 */

public class Utils {


    public static String dirPath = Environment.getExternalStorageDirectory() + "/recordMusic/";

    //隐藏键盘
    public static void hideInput(Activity activity) {
        ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(
                        activity.getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
