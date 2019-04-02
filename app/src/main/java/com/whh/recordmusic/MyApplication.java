package com.whh.recordmusic;

import android.app.Application;

import com.whh.recordmusic.utils.CrashHandler;

/**
 * Created by Administrator on 2019/4/2.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(getApplicationContext());//程序崩溃日志输出保存
    }
}
