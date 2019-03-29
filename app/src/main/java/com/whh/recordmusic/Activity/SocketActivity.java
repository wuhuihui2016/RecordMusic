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
import android.widget.TextView;
import android.widget.Toast;

import com.whh.recordmusic.R;
import com.whh.recordmusic.utils.OnConnectListener;
import com.whh.recordmusic.utils.OnMessageListener;
import com.whh.recordmusic.utils.SocketUtils;
import com.whh.recordmusic.utils.Utils;

/**
 * Created by wuhuihui on 2019/3/26.
 * 请求连接，连接成功后监听、发送消息，发送录音
 */

public class SocketActivity extends Activity {

    private final String TAG = "SocketActivity";

    private Activity activity;
    private String getMyIP; //本机IP

    private EditText editIP;
    private Button connbtn;

    private TextView txt;
    private EditText edit;
    private Button btn, recordBtn;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.i(TAG, "handleMessage=" + msg.what);
            boolean isConnected;
            switch (msg.what) {
                case 0: //连接失败
                    isConnected = false;
                    do4Connected(isConnected);
                case 1: //连接成功
                    isConnected = true;
                    do4Connected(isConnected);
                case 3: //消息发送失败
//                    Toast.makeText(activity,
//                            "消息发送失败，请检查连接是否正常！", Toast.LENGTH_LONG).show();
                case 4: //消息接收成功
                    txt.setText("");
                    btn.setEnabled(false);
                    if (msg.obj != null) {
                        txt.setText(msg.obj.toString());
                    }
//                    else Toast.makeText(activity, "消息已发送", Toast.LENGTH_LONG).show();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket2);

        activity = this;

        //获取本机IP
        TextView myIP = (TextView) findViewById(R.id.myIP);
        getMyIP = SocketUtils.getIPAddress(activity);
        myIP.setText("我的IP：" + getMyIP);

        editIP = (EditText) findViewById(R.id.editIP);
        connbtn = (Button) findViewById(R.id.connbtn);

        txt = (TextView) findViewById(R.id.textView);
        edit = (EditText) findViewById(R.id.edit);
        btn = (Button) findViewById(R.id.btn);
        recordBtn = (Button) findViewById(R.id.recordBtn);

        //监听其他手机发来的消息
        SocketUtils.addMessageListener(new OnConnectListener() {
            @Override
            public void onConnect(boolean isConnected) {
                Message message = Message.obtain();
                if (isConnected) message.what = 1;
                else message.what = 0;
                handler.sendMessage(message);
            }
        }, new OnMessageListener() {
            @Override
            public void sendMsg(boolean isSucessful) {
                Message msg = Message.obtain();
                if (isSucessful) msg.what = 4;
                else msg.what = 3;
                handler.sendMessage(msg);
            }

            @Override
            public void receive(String message) {
                Message msg = Message.obtain();
                msg.what = 4;
                msg.obj = message;
                handler.sendMessage(msg);
            }
        });

        //通过IP连接其他手机
        editIP.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    connbtn.setEnabled(true);
                    connbtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            SocketUtils.addConnectListener(editIP.getText().toString(), new OnConnectListener() {
                                @Override
                                public void onConnect(boolean isConnected) {
                                    Log.i(TAG, "onConnect=" + isConnected);
                                    Message message = Message.obtain();
                                    if (isConnected) message.what = 1;
                                    else message.what = 0;
                                    handler.sendMessage(message);
                                }
                            });
                        }
                    });
                } else connbtn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    /**
     * 有手机连接成功，可以来回通信
     */
    private void do4Connected(boolean isConnected) {
        if (isConnected) {
            Toast.makeText(activity, "连接成功！",
                    Toast.LENGTH_SHORT).show();
            findViewById(R.id.connLayout).setVisibility(View.GONE);
            findViewById(R.id.msgLayout).setVisibility(View.VISIBLE);
            //设置消息发送
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
                            Utils.hideInput(activity); //隐藏键盘
                            SocketUtils.sendMsg(getMyIP + "：" + edit.getText().toString(), new OnMessageListener() {
                                @Override
                                public void sendMsg(boolean isSucessful) {
                                    Message msg = Message.obtain();
                                    if (isSucessful) msg.what = 4;
                                    else msg.what = 3;
                                    handler.sendMessage(msg);
                                }

                                @Override
                                public void receive(String message) {
                                    Message msg = Message.obtain();
                                    msg.what = 4;
                                    msg.obj = message;
                                    handler.sendMessage(msg);
                                }
                            });
                        }
                    });
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            //发送录音
            recordBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(activity, RecordMusicActivity2.class));
                }
            });
        } else {
            Toast.makeText(activity, "连接失败！",
                    Toast.LENGTH_SHORT).show();
            findViewById(R.id.connLayout).setVisibility(View.GONE);
            findViewById(R.id.msgLayout).setVisibility(View.VISIBLE);
        }
    }

}
