package com.whh.recordmusic.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
                //如有手机连接成功，可以向其发送消息
                do4Connected(isConnected);
            }
        }, new OnMessageListener() {
            @Override
            public void sendMsg(boolean isSucessful) {
            }

            @Override
            public void receive(String message) {
                txt.setText(message); //显示消息
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
                                    if (isConnected) Toast.makeText(activity, "连接成功！",
                                            Toast.LENGTH_SHORT).show();
                                    else Toast.makeText(activity, "连接失败！",
                                            Toast.LENGTH_LONG).show();
                                    //如连接成功，可以向其发送消息
                                    do4Connected(isConnected);
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
                            SocketUtils.hideInput(activity); //隐藏键盘
                            SocketUtils.sendMsg(getMyIP + "：" + edit.getText().toString(), new OnMessageListener() {
                                @Override
                                public void sendMsg(boolean isSucessful) {
                                    if (isSucessful) {
                                        Log.i(TAG, "消息发送成功！");
                                        edit.setText("");
                                        btn.setEnabled(false);
                                    } else
                                        Toast.makeText(activity,
                                                "消息发送失败，请检查连接是否正常！", Toast.LENGTH_LONG).show();
                                }

                                @Override
                                public void receive(String message) {
                                }
                            }); //发送数据
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
            findViewById(R.id.connLayout).setVisibility(View.GONE);
            findViewById(R.id.msgLayout).setVisibility(View.VISIBLE);
        }
    }

}
