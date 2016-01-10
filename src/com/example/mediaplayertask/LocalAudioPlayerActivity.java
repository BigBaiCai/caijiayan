package com.example.mediaplayertask;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/*
* 1��ǰ̨�ķ�������ȥ֮����ת��activity����������create (fixed)
* 2���ع�AudioPlayer����Ϊ�̳�MediaPlayer (solved)
* 3����������������ʱ�� (solved)
* 4��������ɱ�����Ƶ���ţ���������������Ƶ����
* 5����������ǰ���
* */
public class LocalAudioPlayerActivity extends Activity implements View.OnClickListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "MediaPlayer";
    private LocalAudioPlayer localAudioPlayer = new LocalAudioPlayer(LocalAudioPlayerActivity.this);
   
    private NetWorkAudioPlayer netWorkAudioPlayer = new NetWorkAudioPlayer(this);
    
    private Cursor cursor;
    private Button playButton;
    private Button preButton;
    private Button nextButton;
    private Button loopButton;
    private Button jumpButton;
    private SeekBar seekBar;
    private TextView startTimeText;
    private TextView endTimeText;
    private static final String PLAY = "Play";
    private static final String PAUSE = "Pause";
    private Intent intent;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (localAudioPlayer != null && localAudioPlayer.isPrepared()) {
                        int totalTime = localAudioPlayer.getDuration();
                        int currentTime = localAudioPlayer.getCurrentPosition();
                        int seekBarMax = seekBar.getMax();
                        if (totalTime > 0 && currentTime > 0 && seekBarMax > 0) {
                            startTimeText.setText(getTimeText(currentTime));
                            seekBar.setProgress((int) (seekBarMax * (float) currentTime / totalTime));
                        }
                    }
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.localplayer);
        initView();
        cursor = getCursor();
        localAudioPlayer.setCursor(cursor);
        setListener();
        UpdateSeekBarThread thread = new UpdateSeekBarThread();
        thread.start();

    }

    /*
     * android�г�������ݵĹ�����ͨ��Provider/Resolver���еġ��ṩ���ݣ����ݣ��ľͽ�Provider��
    		Resovler�ṩ�ӿڶ�������ݽ��н����
    	public final Cursor query (Uri uri, String[] projection,String selection,
    	String[] selectionArgs, String sortOrder)
    	����1:Android�ṩ���ݵĽ�Provider����ô��Android�����ָ���Provider����Ҫ��һ��Ψһ�ı�ʶ����ʶ���Provider��Uri���������ʶ��
    	2�������������ProviderҪ���ص����ݣ���Column����
    	3.selection�������������൱��SQL����е�where��null��ʾ������ɸѡ��
    	4.electionArgs�����������Ҫ��ϵ���������ʹ�õģ�������ڵ��������������У�����ô����selectionArgsд�����ݾͻ��滻����
    	5.sortOrder������ʲô���������൱��SQL����е�Order by�� Ĭ��������ASC ����ΪDESC
     */
    
    /*
     * MediaStore�����˶�ý�����ݿ��������Ϣ��������Ƶ����Ƶ��ͼ��,android�����еĶ�ý�����ݿ�ӿڽ����˷�װ�����е����ݿⲻ���Լ����д�����
     * ֱ�ӵ�������ContentResolverȥ������Щ��װ�õĽӿھͿ��Խ������ݿ�Ĳ����ˡ�
     */
    private Cursor getCursor() {
        ContentResolver contentResolver =this.getContentResolver();//��ȡcontentResolver��ʵ��
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//��ȡ�洢��SD���ϵ���Ƶ�ļ���URI
        Cursor cursor = contentResolver.query(uri, null, null, null, null);//�α��ѯ
        if (cursor == null) {
            return null;//��ʾû����Ƶ�ļ�
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        } else {
            return cursor;
        }
    }

    private void setListener() {
        playButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        preButton.setOnClickListener(this);
        loopButton.setOnClickListener(this);
        localAudioPlayer.setOnCompletionListener(this);
        seekBar.setOnSeekBarChangeListener(new ProgressBarListener());
        jumpButton.setOnClickListener(this);
    }

    private void initView() {
        playButton = (Button) findViewById(R.id.playButton);
        nextButton = (Button) findViewById(R.id.nextButton);
        preButton = (Button) findViewById(R.id.preButton);
        loopButton = (Button) findViewById(R.id.loopButton);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        startTimeText = (TextView) findViewById(R.id.startTextView);
        endTimeText = (TextView) findViewById(R.id.endTextView);
       jumpButton = (Button)findViewById(R.id.jumpButton);
    }

    @Override
    protected void onDestroy() {
        localAudioPlayer.release();
        localAudioPlayer = null;
        cursor.close();
        if (intent != null) {
            stopService(intent);
        }
        super.onDestroy();
    }

    public String getTimeText(int time) {
        /*
        �����time������λΪmilliseconds��������
        ����������Խ����뵥λ��ʱ��ת��Ϊ0��00��ʽ��ʱ��
         */
        int totalSeconds = time / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        String showTime;
        if (seconds > 9 && seconds < 60) {
            showTime = minutes + ":" + seconds;
        } else {
            showTime = minutes + ":0" + seconds;
        }
        return showTime;
    }

    
    /*
     * Thread.Sleep(0)�����ã����ǡ���������ϵͳ�������½���һ��CPU�������������Ľ��Ҳ���ǵ�ǰ�߳���Ȼ���CPU����Ȩ��Ҳ��ỻ�ɱ���̻߳�
     * ��CPU����Ȩ����Ҳ�������ڴ�ѭ�����澭����дһ��Thread.Sleep(0) ����Ϊ�����͸��������̻߳��CPU����Ȩ��Ȩ������������Ͳ�����������
     */
    public class UpdateSeekBarThread extends Thread {
        @Override
        public void run() {
            while (localAudioPlayer != null) {
                try {
                    Thread.sleep(30);//��δ����30�����ڲ�����CPU����
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);//��0����handler
            }

        }
    }

    
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.playButton:
                if (PLAY.equals(playButton.getText())) {
                    localAudioPlayer.play();
                    playButton.setText(PAUSE);
                } else if (PAUSE.equals(playButton.getText())) {
                    localAudioPlayer.playPause();
                    playButton.setText(PLAY);
                }
                break;
            case R.id.nextButton:
                localAudioPlayer.playNext();
                if (PLAY.equals(playButton.getText())) {
                    playButton.setText(PAUSE);
                }
                break;
            case R.id.preButton:
                localAudioPlayer.playPrevious();
                if (PLAY.equals(playButton.getText())) {
                    playButton.setText(PAUSE);
                }
                break;
            case R.id.loopButton:
                localAudioPlayer.setLooping(true);
                break;
            case R.id.jumpButton:
                Intent intent = new Intent(LocalAudioPlayerActivity.this, NetworkPlayerActivity.class);//�ӱ��ز�����Ƶ��ת�������ȡ��Ƶ
                startActivity(intent);
                netWorkAudioPlayer.playPrepared();
                break;
        }
        endTimeText.setText(getTimeText(localAudioPlayer.getDuration()));
        intent = new Intent(LocalAudioPlayerActivity.this, Myservice.class);
        
        intent.putExtra("songName", localAudioPlayer.getLocalAudioInfo().getCurrentAudioTitle(cursor));
        
        startService(intent);
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playButton.setText(PLAY);
    }


    private class ProgressBarListener implements SeekBar.OnSeekBarChangeListener {

        /*
        * �����progress����ָ���������seekBar��˵��progress����������Ƶ��progress
        * seekBarĬ���ǵ����ֵ��100,����ĳ���Ƶ�Ĵ�С����ô�����progress����������Ƶ��progress
        * */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                localAudioPlayer.seekTo((int) (localAudioPlayer.getDuration() * (float) progress / seekBar.getMax()));
                seekBar.setProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }
}