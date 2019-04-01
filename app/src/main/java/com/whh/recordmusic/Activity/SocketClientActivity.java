package com.whh.recordmusic.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuhuihui on 2019/3/26.
 * 客户端
 */
public class SocketClientActivity extends Activity implements View.OnClickListener {

    private final String TAG = "SocketClientActivity";
    private Activity activity;

    private SocketClient client;
    private Socket getSocket;
    private LinearLayout connLayout, sendLayout;
    private TextView txt;
    private EditText edit;
    private Button btn;

    private String getIP;

    private TextView statusTextView, amplitudeTextView, info;
    private Button recordingAndSend, stopRecording, playRecording;

    private boolean isRecording; //标识当前录制状态
    private AudioRecord audioRecord; //录制器
    private MediaPlayer player; //播放器
    private File audioFile; //录制完成后保存的AMR文件

    //录制音频参数
    int frequence = 44100; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);

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

                checkPermissions();

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

                statusTextView = (TextView) findViewById(R.id.statusTextView);
                statusTextView.setText("Ready");
                amplitudeTextView = (TextView) findViewById(R.id.amplitudeTextView);
                amplitudeTextView.setText("0");

                info = (TextView) findViewById(R.id.info); //录制完成显示文件保存路径

                recordingAndSend = (Button) findViewById(R.id.recordingAndSend); //录制并发送
                stopRecording = (Button) findViewById(R.id.stopRecording); //停止录制
                playRecording = (Button) findViewById(R.id.playRecording); //播放已录制

                stopRecording.setEnabled(false);
                playRecording.setEnabled(false);

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
                //开启客户端接收消息线程
                client.openClientThread(activity, getIP, SocketUtils.port, new OnConnectListener() {
                    @Override
                    public void onConnect(Socket socket, boolean isConnected) {

                        Message message = Message.obtain();
                        if (isConnected) {
                            getSocket = socket;
                            message.what = 1;
                        } else
                            message.what = 0;
                        clientHander.sendMessage(message);
                    }
                });
            }
        });


    }


    /**
     * 检查获取权限
     */
    private static int PERMISSON_REQUESTCODE = 0;//权限请求码

    private void checkPermissions() {
        //需要进行检测的权限数组
        String[] permissions = {
                Manifest.permission.WRITE_SETTINGS, //设置权限
                Manifest.permission.WRITE_EXTERNAL_STORAGE, //写文件
                Manifest.permission.READ_EXTERNAL_STORAGE, //读文件
                Manifest.permission.RECORD_AUDIO, //录音权限
                Manifest.permission.CAPTURE_AUDIO_OUTPUT //音频输出
        };
        //获取权限集中需要申请权限的列表
        List<String> needRequestPermissonList = new ArrayList<String>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                needRequestPermissonList.add(perm);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                    needRequestPermissonList.add(perm);
                }
            }
        }
        //请求权限
        if (null != needRequestPermissonList && needRequestPermissonList.size() > 0) {
            ActivityCompat.requestPermissions(this,
                    needRequestPermissonList.toArray(
                            new String[needRequestPermissonList.size()]),
                    PERMISSON_REQUESTCODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] paramArrayOfInt) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            //检测是否说有的权限都已经授权
            boolean flag = true;
            for (int result : paramArrayOfInt) {
                if (result != PackageManager.PERMISSION_GRANTED) flag = false;
            }
            if (flag) {
                //权限获取成功！
            } else {
//                Toast.makeText(MainActivity.this, "期待更多的功能，请先允许权限！", Toast.LENGTH_SHORT).show();
//                checkPermissions();
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v == recordingAndSend) {
            recordMusic();
        } else if (v == stopRecording) { //停止录制
            isRecording = false;

            statusTextView.setText("Ready to Play");
            info.setText("录制完成！文件已保存：" + audioFile.getAbsolutePath()); //录制完成显示音频文件路径
            playRecording.setEnabled(true);
            recordingAndSend.setEnabled(true);
            stopRecording.setEnabled(false);

        } else if (v == playRecording) { //播放已录制
            try {
                //初始化播放器
                player = new MediaPlayer();
                FileInputStream fis = new FileInputStream(audioFile);
                player.setDataSource(fis.getFD()); //设置最新录制完的音频文件，准备播放
                player.prepare();
                if (player != null && audioFile.exists()) {
                    player.start();
                    statusTextView.setText("Playing");

                    playRecording.setEnabled(false);
                    recordingAndSend.setEnabled(false);
                    stopRecording.setEnabled(false);
                }

                //设置播放器播放完成事件
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        statusTextView.setText("Ready");

                        playRecording.setEnabled(true);
                        recordingAndSend.setEnabled(true);
                        stopRecording.setEnabled(false);
                    }
                });
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /**
     * 录制音频，或发送
     */
    private void recordMusic() { //开始录制
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Toast.makeText(getApplicationContext(), "SD卡不存在，请插入SD卡！", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Log.i(TAG, "startRecording开始录制。。。。");

            isRecording = true;
            statusTextView.setText("Recording");
            playRecording.setEnabled(false);
            stopRecording.setEnabled(true);
            recordingAndSend.setEnabled(false);

            audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    frequence, channelConfig, audioEncoding, bufferSize);

            audioRecord.startRecording(); //开始录制
            File path = new File(Environment.getExternalStorageDirectory() + "/recordMusic"); //保存录制完成的音频文件夹
            path.mkdirs();
            audioFile = File.createTempFile("recording", ".pcm", path);
            final String filePath = audioFile.getAbsolutePath();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
//                        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);//设置线程的优先级，音频优先级最高
                        FileOutputStream os = new FileOutputStream(filePath);

                        //向服务器发送数据
                        client.sendMsg("1234567890", new OnMessageListener() {
                            @Override
                            public void sendMsg(boolean isSucessful) {
                                if (isSucessful) {
                                    Message msg = Message.obtain();
                                    msg.what = 3;
                                    clientHander.sendMessage(msg);
                                } else
                                    Toast.makeText(activity, "消息发送失败，请检查连接是否正常！", Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void receive(String message) {

                            }
                        });


                        DataOutputStream dos = null;
                        if (getSocket != null && !getSocket.isClosed()) {
                            dos = new DataOutputStream(getSocket.getOutputStream());
                            byte[] buffer = new byte[50];
                            while (isRecording && audioRecord != null) {
                                int readSize = audioRecord.read(buffer, 0, buffer.length);
                                if (AudioRecord.ERROR_INVALID_OPERATION != readSize) {
                                    os.write(buffer); //写入文件

                                    Log.i(TAG, "向服务器发送数据==>socket写入数据");
                                    dos.write(buffer);

                                    //播放音频数据
//                                DataInputStream dis = new DataInputStream(new FileInputStream(audioFile));
//                                int mMinBufferSize = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);//计算最小缓冲区
//                                AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100, AudioFormat.CHANNEL_CONFIGURATION_MONO,
//                                        AudioFormat.ENCODING_PCM_16BIT, mMinBufferSize, AudioTrack.MODE_STREAM);
//                                while (dis.read(buffer) != -1) {
//                                    Log.i(TAG, "播放音频数据。。。" + dis.read(buffer));
//                                    audioTrack.play();
//                                    audioTrack.write(buffer, 0, buffer.length);
//                                }
//                                audioTrack.stop();
//                                audioTrack.release();
                                }

                            }

                            Log.d(TAG, "isRecording==>exit loop");
                            os.flush();
                            os.close();
                            dos.flush();
                            dos.close();

                        } else {
                            Log.i(TAG, "socket is null and is closed!");
                            Message message = Message.obtain();
                            message.what = 0;
                            clientHander.sendMessage(message);
                        }

                        audioRecord.stop();
                        audioRecord.release();
                        Log.d(TAG, "isRecording==>clean up");
                        Log.i(TAG, "isRecording==>AudioRecord released!");

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "isRecording==>Dump PCM to file failed");
        }

    }

}
