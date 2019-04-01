package com.whh.recordmusic.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.whh.recordmusic.R;
import com.whh.recordmusic.model.Speex;
import com.whh.recordmusic.utils.SocketUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * AudioRecord录制音频不接收麦克风声音
 */
public class RecordMusicActivity2 extends Activity implements View.OnClickListener {

    private TextView statusTextView, amplitudeTextView, info;
    private Button startRecording, recordingAndSend, stopRecording, playRecording;

    private boolean isRecording; //标识当前录制状态
    private AudioRecord audioRecord; //录制器
    private MediaPlayer player; //播放器
    private File audioFile; //录制完成后保存的AMR文件

    //录制音频参数
    int frequence = 44100; //录制频率，单位hz.这里的值注意了，写的不好，可能实例化AudioRecord对象的时候，会出错。我开始写成11025就不行。这取决于硬件设备
    int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
    int bufferReadSize = 1024;

    private final String TAG = "RecordMusic===>>";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_music);

        checkPermissions(); //检查权限：读写存储卡、录制等权限

        initView(); //初始化View

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

    /**
     * 初始化界面View
     */
    private void initView() {

        Log.i(TAG, Environment.getExternalStorageDirectory() + "/recordMusic");
        Log.i(TAG, "存放录制音频的目录" + Environment.getExternalStorageDirectory() + "/recordMusic");

        float scale = getResources().getDisplayMetrics().density;
        float scaledDensity = getResources().getDisplayMetrics().scaledDensity;

        Log.i(TAG + "scale", scale + "");
        Log.i(TAG + "density", scaledDensity + "");
        Log.i(TAG + "顶部大标题", 36 / scale + 0.5f + "");
        Log.i(TAG + "正文", 32 / scale + 0.5f + "");


        statusTextView = (TextView) findViewById(R.id.statusTextView);
        statusTextView.setText("Ready");
        amplitudeTextView = (TextView) findViewById(R.id.amplitudeTextView);
        amplitudeTextView.setText("0");

        info = (TextView) findViewById(R.id.info); //录制完成显示文件保存路径

        startRecording = (Button) findViewById(R.id.startRecording); //开始录制
        recordingAndSend = (Button) findViewById(R.id.recordingAndSend); //录制并发送
        stopRecording = (Button) findViewById(R.id.stopRecording); //停止录制
        playRecording = (Button) findViewById(R.id.playRecording); //播放已录制

        stopRecording.setEnabled(false);
        playRecording.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        if (v == startRecording) {
            recordMusic(false);
        } else if (v == recordingAndSend) {
            recordMusic(true);
        } else if (v == stopRecording) { //停止录制
            isRecording = false;

            statusTextView.setText("Ready to Play");
            info.setText("录制完成！文件已保存：" + audioFile.getAbsolutePath()); //录制完成显示音频文件路径
            playRecording.setEnabled(true);
            recordingAndSend.setEnabled(true);
            stopRecording.setEnabled(false);
            startRecording.setEnabled(true);

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
                    startRecording.setEnabled(false);
                }

                //设置播放器播放完成事件
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        statusTextView.setText("Ready");

                        playRecording.setEnabled(true);
                        recordingAndSend.setEnabled(true);
                        stopRecording.setEnabled(false);
                        startRecording.setEnabled(true);
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

    private void sendAudioData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    byte[] buffer = new byte[frequence];
                    int buffersize = AudioRecord.getMinBufferSize(frequence, channelConfig, audioEncoding);
                    Socket socket = new Socket(SocketUtils.targetIP, SocketUtils.port);
                    OutputStream outputStream = socket.getOutputStream();
                    while (socket.isConnected() && isRecording) {
                        //从bufferSize中读取字节，这时buffer也就是原始数据
                        int bufferReadResult = audioRecord.read(buffer, 0, buffersize);
                        outputStream.write(buffer, 0, bufferReadResult);
                    }

                    outputStream.flush();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    /**
     * 录制音频，或发送
     *
     * @param isSend 需要发送的标识
     *
     */

    Speex speex;
    private void recordMusic(final boolean isSend) { //开始录制
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
            startRecording.setEnabled(false);
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

                        FileOutputStream os = new FileOutputStream(filePath);

                        //截取音频流，开始压缩
                        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));
                        speex = new Speex();
                        int sizeInShorts = speex.getFrameSize();
                        short[] audioData = new short[sizeInShorts];
                        int sizeInBytes = speex.getFrameSize();


                        Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);//设置线程的优先级，音频优先级最高

                        byte[] buffer = new byte[1024];
                        while (isRecording && audioRecord != null) {
                            int readSize = audioRecord.read(buffer, 0, 1024);
                            Log.d(TAG, "isRecording==>readSize = " + readSize);
                            os.write(buffer, 0, readSize);

                            //开始记录数据
                            int number = audioRecord.read(audioData, 0, sizeInShorts);
                            short[] dst = new short[sizeInBytes];
                            System.arraycopy(audioData, 0, dst, 0, number);
                            byte[] encoded = new byte[sizeInBytes];
                            int count = speex.encode(dst, 0, encoded, number);
                            if (count > 0) {
                                //记录每次录音得到的数据，与播放录音的时候对应
                                Data data = new Data();
                                data.mSize = count;
                                data.mBuffer = encoded;
                                mDatas.add(data);
                                dos.write(encoded, 0, count);
                            }

//                            if (isSend) sendAudioData();
                        }

                        Log.d(TAG, "isRecording==>exit loop");
                        os.flush();
                        os.close();
                        dos.flush();
                        dos.close();

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

    private static final class Data {
        private int mSize;//记录每次编码之后的大小，用做解码时设置每次读取的字节大小
        private byte[] mBuffer;//记录每次编码之后的字节数组，可以用做边录边播放
    }


    private List<Data> mDatas = new ArrayList<Data>(); //录音片段组
    private void palyAudioStream() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    AudioTrack mAudioTrack=new AudioTrack(AudioManager.STREAM_MUSIC, frequence, channelConfig,
                            audioEncoding, bufferSize, AudioTrack.MODE_STREAM);
                    DataInputStream dis = new DataInputStream(
                            new BufferedInputStream(new FileInputStream(audioFile)));
                    int len = 0;
                    //根据每次录音得到的数据，进行播放
                    for(Data data:mDatas) {
                        byte[] encoded = new byte[data.mSize];
                        len = dis.read(encoded, 0, data.mSize);
                        if (len != -1) {
                            short[] lin = new short[speex.getFrameSize()];
                            int size = speex.decode(encoded, lin,
                                    encoded.length);
                            if (size > 0) {
                                mAudioTrack.write(lin, 0, size);
                                mAudioTrack.play();
                            }
                        }
                    }

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


}

