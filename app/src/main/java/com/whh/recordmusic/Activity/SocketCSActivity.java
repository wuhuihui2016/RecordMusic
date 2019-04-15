package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.whh.recordmusic.R;
import com.whh.recordmusic.utils.SocketUtils;
import com.whh.recordmusic.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhuihui on 2019/3/26.
 * 选择客户端/服务端
 */
public class SocketCSActivity extends Activity {

    private final String TAG = "SocketCSActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);

        //用户端
        findViewById(R.id.client).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocketUtils.ID = 1; //设置当前身份
                startActivity(new Intent(getApplicationContext(), SocketClientActivity.class));
            }
        });

        //服务器
        findViewById(R.id.server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SocketUtils.ID = 0; //设置当前身份
                startActivity(new Intent(getApplicationContext(), SocketServerActivity.class));
            }
        });

        List <Integer> list = new ArrayList<>();
        list.add(1);
        list.add(4);
        list.add(2);
        list.add(1);
        list.add(3);
        list.add(2);
        list.add(1);
        list.add(4);
        Utils.comparable(list);
    }
}
