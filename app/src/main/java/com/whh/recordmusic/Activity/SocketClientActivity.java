package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.whh.recordmusic.R;
import com.whh.recordmusic.model.SocketClient;
import com.whh.recordmusic.utils.OnConnectListener;
import com.whh.recordmusic.utils.OnMessageListener;
import com.whh.recordmusic.utils.SocketUtils;
import com.whh.recordmusic.utils.Utils;

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
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                Toast.makeText(getApplicationContext(),
                        "连接失败！", Toast.LENGTH_LONG).show();
            } else if (msg.what == 1) { //连接成功
                Toast.makeText(getApplicationContext(),
                        "连接成功！", Toast.LENGTH_LONG).show();
                Utils.hideInput(activity); //隐藏键盘
                connLayout.setVisibility(View.GONE);
                sendLayout.setVisibility(View.VISIBLE);
                txt = (TextView) findViewById(R.id.textView);
                edit = (EditText) findViewById(R.id.edit);
                btn = (Button) findViewById(R.id.btn);

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
                                client.sendMsg(edit.getText().toString(), new OnMessageListener() {
                                    @Override
                                    public void sendMsg(boolean isSucessful) {
                                        if (isSucessful) {
                                            Message msg = Message.obtain();
                                            msg.what = 3;
                                            sendMessage(msg);
                                        } else
                                            Toast.makeText(activity, "消息发送失败，请检查连接是否正常！", Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void receive(String message) {

                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                findViewById(R.id.recordBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, RecordMusicActivity2.class);
                        startActivity(intent);
                    }
                });

                //接收消息
                SocketClient.cHandler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);

                        txt.setText(getIP + "：" + msg.obj.toString());
                        Log.i(TAG, "接收消息==>" + msg.obj.toString());
                    }
                };
            } else if (msg.what == 3) {
                Log.i(TAG, "消息发送成功！");
                edit.setText("");
                btn.setEnabled(false);
            }
        }
    };

    String getIP = null; //连接的IP

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
                getIP = editIP.getText().toString();
                client = new SocketClient();
                client.clientValue(activity, getIP, SocketUtils.port); //设置服务端的IP和端口号
                //开启客户端接收消息线程
                client.openClientThread(new OnConnectListener() {
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
