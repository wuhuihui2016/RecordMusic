package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.whh.recordmusic.R;

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
                startActivity(new Intent(getApplicationContext(), SocketClientActivity.class));
            }
        });

        //服务器
        findViewById(R.id.server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SocketServerActivity.class));
            }
        });
    }
}
