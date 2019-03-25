package com.whh.recordmusic;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * AudioRecord录制音频不接收麦克风声音
 */
public class RecordMusicActivity2 extends Activity implements View.OnClickListener {

    private TextView statusTextView, amplitudeTextView, info;
    private Button startRecording, stopRecording, playRecording;

    private boolean isRecording; //标识当前录制状态
    private MediaRecorder recorder; //录制器
    private RecordAmplitude recordAmplitude; //录制状态时的刷新页面
    private MediaPlayer player; //播放器
    private File audioFile; //录制完成后保存的AMR文件

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

        Log.i(TAG + "initView", Environment.getExternalStorageDirectory() + "/recordMusic");
        Log.i(TAG + "存放录制音频的目录", Environment.getExternalStorageDirectory() + "/recordMusic");

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
        stopRecording = (Button) findViewById(R.id.stopRecording); //停止录制
        playRecording = (Button) findViewById(R.id.playRecording); //播放已录制

        stopRecording.setEnabled(false);
        playRecording.setEnabled(false);

    }

    @Override
    public void onClick(View v) {
        if (v == startRecording) { //开始录制
            if (!Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                Toast.makeText(getApplicationContext(), "SD卡不存在，请插入SD卡！", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                Log.i(TAG, "startRecording开始录制。。。。");
                int kSampleRate = 44100;
                int kChannelMode = AudioFormat.CHANNEL_IN_STEREO;
                int kEncodeFormat = AudioFormat.ENCODING_PCM_16BIT;

                int minBufferSize = AudioRecord.getMinBufferSize(kSampleRate, kChannelMode,
                        kEncodeFormat);
                AudioRecord mRecord = new AudioRecord(MediaRecorder.AudioSource.REMOTE_SUBMIX,
                        kSampleRate, kChannelMode, kEncodeFormat, minBufferSize * 2);

                int kFrameSize = 2048;
                File path = new File(Environment.getExternalStorageDirectory() + "/recordMusic"); //保存录制完成的音频文件夹
                path.mkdirs();
//                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
//                String time = sdf.format(new Date());
//                Log.i("fileName", time); //介于多次录制，文件名以时间后缀命名
                audioFile = File.createTempFile("recording", ".amr", path);
                String filePath = audioFile.getAbsolutePath();

                FileOutputStream os = null;
                mRecord.startRecording();
                isRecording = true;

                os = new FileOutputStream(filePath);
                byte[] buffer = new byte[kFrameSize];
                int num = 0;
                while (isRecording) {
                    num = mRecord.read(buffer, 0, kFrameSize);
                    Log.d(TAG + "isRecording", "buffer = " + buffer.toString() + ", num = " + num);
                    os.write(buffer, 0, num);
                }

                Log.d(TAG + "isRecording", "exit loop");
                os.close();

                mRecord.stop();
                mRecord.release();
                mRecord = null;
                Log.d(TAG + "isRecording", "clean up");
                Log.i(TAG + "audioRecord", "AudioRecord released!");

                recordAmplitude = new RecordAmplitude();
                recordAmplitude.execute();

                statusTextView.setText("Recording");

                playRecording.setEnabled(false);
                stopRecording.setEnabled(true);
                startRecording.setEnabled(false);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG + "isRecording", "Dump PCM to file failed");
            }

        } else if (v == stopRecording) { //停止录制
            isRecording = false;
            recordAmplitude.cancel(true);

            recorder.stop();
            recorder.release(); //停止并释放录制器

            statusTextView.setText("Ready to Play");
            info.setText("录制完成！文件已保存：" + audioFile.getAbsolutePath()); //录制完成显示音频文件路径
            playRecording.setEnabled(true);
            stopRecording.setEnabled(false);
            startRecording.setEnabled(true);

        } else if (v == playRecording) { //播放已录制
            try {
                //初始化播放器
                player = new MediaPlayer();
                player.setDataSource(audioFile.getAbsolutePath()); //设置最新录制完的音频文件，准备播放
                player.prepare();
                if (player != null && audioFile.exists()) {
                    player.start();
                    statusTextView.setText("Playing");

                    playRecording.setEnabled(false);
                    stopRecording.setEnabled(false);
                    startRecording.setEnabled(false);
                }

                //设置播放器播放完成事件
                player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        statusTextView.setText("Ready");

                        playRecording.setEnabled(true);
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


    /**
     * 刷新UI显示录音振幅
     */
    class RecordAmplitude extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            while (isRecording) {
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                publishProgress(recorder.getMaxAmplitude());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);
            amplitudeTextView.setText(values[0].toString());
        }
    }


}