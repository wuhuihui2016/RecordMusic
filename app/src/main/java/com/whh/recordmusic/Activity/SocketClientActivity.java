package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whh.recordmusic.R;
import com.whh.recordmusic.utils.Utils;
import com.whh.recordmusic.model.SocketClient;

/**
 * Created by wuhuihui on 2019/3/26.
 * 客户端
 */
public class SocketClientActivity extends Activity {

    private final String TAG = "SocketClientActivity";
    private Activity activity;

    private SocketClient client;
    private LinearLayout connLayout, sendLayout;
    private TextView txt;
    private EditText edit;
    private Button btn;

    /**
     * 判断连接成功与否，刷新界面
     */
    private Handler clientHander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Toast.makeText(getApplicationContext(),
                        "连接失败！", Toast.LENGTH_LONG).show();
            } else if (msg.what == 1) { //连接成功
                Toast.makeText(getApplicationContext(),
                        "连接成功！", Toast.LENGTH_LONG).show();
                connLayout.setVisibility(View.GONE);
                sendLayout.setVisibility(View.VISIBLE);
                txt = (TextView) findViewById(R.id.textView);
                edit = (EditText) findViewById(R.id.edit);
                btn = (Button) findViewById(R.id.btn);

                //发送消息
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        client.sendMsg(edit.getText().toString());
                    }
                });

                //接收消息
                SocketClient.cHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        txt.setText(msg.obj.toString());
                        Log.i(TAG, "接收消息==>" + msg.obj.toString());
                    }
                };
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_client);

        activity = this;

        TextView type = (TextView) findViewById(R.id.type);
        type.setText("用户端");

        connLayout = (LinearLayout) findViewById(R.id.connLayout);
        sendLayout = (LinearLayout) findViewById(R.id.sendLayout);

        final EditText editIP = (EditText) findViewById(R.id.editIP);

        //发送连接
        findViewById(R.id.connbtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String getIP = editIP.getText().toString();
                client = new SocketClient();
                client.clientValue(activity, getIP, Utils.port); //设置服务端的IP和端口号
                //开启客户端接收消息线程
                client.openClientThread(new SocketClient.onConnectListener() {
                    @Override
                    public void onConnect(boolean isConnected) {
                        Message message = Message.obtain();
                        if (isConnected) {
                            message.what = 1;
                        } else
                            message.what = 0;
                        clientHander.sendMessage(message);
                    }
                });
            }
        });


    }
}
