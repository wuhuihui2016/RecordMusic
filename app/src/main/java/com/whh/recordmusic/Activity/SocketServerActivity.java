package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.whh.recordmusic.R;
import com.whh.recordmusic.model.SocketServer;
import com.whh.recordmusic.utils.OnMessageListener;
import com.whh.recordmusic.utils.SocketUtils;

/**
 * Created by wuhuihui on 2019/3/26.
 */

public class SocketServerActivity extends Activity implements OnMessageListener {

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
        type.setText("服务器\nIP：" + SocketUtils.getIPAddress(activity));

        txt = (TextView) findViewById(R.id.textView);
        edit = (EditText) findViewById(R.id.edit);
        btn = (Button) findViewById(R.id.btn);

        Log.i(TAG, "获取服务器的IP地址" + SocketUtils.getIPAddress(activity));
//        server = new SocketServer(SocketUtils.port); //启动服务端端口,服务端IP为手机IP
//        Utils.server = server;
        Log.i(TAG, "服务器已启动，等待客户端连接...");
        server.beginListen(); //socket服务端开始监听

        edit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) btn.setEnabled(true);
                else btn.setEnabled(false);
                //发送消息
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        server.sendMessage(SocketServerActivity.this, edit.getText().toString()); //发送数据
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

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

    @Override
    public void sendMsg(boolean isSucessful) {
        if (isSucessful) {
            Log.i(TAG, "消息发送成功！");
            edit.setText("");
            btn.setEnabled(false);
        } else Toast.makeText(activity, "消息发送失败，请检查连接是否正常！", Toast.LENGTH_LONG).show();
    }

    @Override
    public void receive(String message) {

    }

}
