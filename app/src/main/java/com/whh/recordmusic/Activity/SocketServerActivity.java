package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.whh.recordmusic.R;
import com.whh.recordmusic.utils.Utils;
import com.whh.recordmusic.model.SocketServer;

/**
 * Created by wuhuihui on 2019/3/26.
 */

public class SocketServerActivity extends Activity {

    private final String TAG = "SocketServerActivity";

    private Activity activity;

    private TextView txt;
    private EditText edit;
    private Button btn;

    private SocketServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_server);

        activity = this;

        TextView type = (TextView) findViewById(R.id.type);
        type.setText("服务器\nIP：" + Utils.getIPAddress(activity));

        txt = (TextView) findViewById(R.id.textView);
        edit = (EditText) findViewById(R.id.edit);
        btn = (Button) findViewById(R.id.btn);

        Log.i(TAG, "获取服务器的IP地址" + Utils.getIPAddress(activity));
        server = new SocketServer(Utils.port); //启动服务端端口,服务端IP为手机IP
        Log.i(TAG, "服务器已启动，等待客户端连接...");
        server.beginListen(); //socket服务端开始监听

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                server.sendMessage(edit.getText().toString()); //发送数据
            }
        });

        SocketServer.sHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                txt.setText(msg.obj.toString());
            }
        };

    }

}
