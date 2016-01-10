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
* 1、前台的服务点击进去之后跳转到activity，但不重新create (fixed)
* 2、重构AudioPlayer，改为继承MediaPlayer (solved)
* 3、进度条两端增加时间 (solved)
* 4、基本完成本地音频播放，构建播放网络音频的类
* 5、尝试连接前后端
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
     * android中程序间数据的共享是通过Provider/Resolver进行的。提供数据（内容）的就叫Provider，
    		Resovler提供接口对这个内容进行解读。
    	public final Cursor query (Uri uri, String[] projection,String selection,
    	String[] selectionArgs, String sortOrder)
    	参数1:Android提供内容的叫Provider，那么在Android中区分各个Provider就需要有一个唯一的标识来标识这个Provider，Uri就是这个标识。
    	2：这个参数告诉Provider要返回的内容（列Column），
    	3.selection，设置条件，相当于SQL语句中的where。null表示不进行筛选。
    	4.electionArgs，这个参数是要配合第三个参数使用的，如果你在第三个参数里面有？，那么你在selectionArgs写的数据就会替换掉？
    	5.sortOrder，按照什么进行排序，相当于SQL语句中的Order by。 默认是升序ASC 降序为DESC
     */
    
    /*
     * MediaStore包括了多媒体数据库的所有信息，包括音频，视频和图像,android把所有的多媒体数据库接口进行了封装，所有的数据库不用自己进行创建，
     * 直接调用利用ContentResolver去掉用那些封装好的接口就可以进行数据库的操作了。
     */
    private Cursor getCursor() {
        ContentResolver contentResolver =this.getContentResolver();//获取contentResolver的实例
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;//获取存储在SD卡上的音频文件的URI
        Cursor cursor = contentResolver.query(uri, null, null, null, null);//游标查询
        if (cursor == null) {
            return null;//表示没有音频文件
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
        传入的time参数单位为milliseconds，即毫秒
        这个方法可以将毫秒单位的时间转换为0：00形式的时间
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
     * Thread.Sleep(0)的作用，就是“触发操作系统立刻重新进行一次CPU竞争”。竞争的结果也许是当前线程仍然获得CPU控制权，也许会换成别的线程获
     * 得CPU控制权。这也是我们在大循环里面经常会写一句Thread.Sleep(0) ，因为这样就给了其他线程获得CPU控制权的权力，这样界面就不会假死在那里。
     */
    public class UpdateSeekBarThread extends Thread {
        @Override
        public void run() {
            while (localAudioPlayer != null) {
                try {
                    Thread.sleep(30);//在未来的30毫秒内不参与CPU竞争
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.sendEmptyMessage(0);//把0传给handler
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
                Intent intent = new Intent(LocalAudioPlayerActivity.this, NetworkPlayerActivity.class);//从本地播放音频跳转到网络获取音频
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
        * 这里的progress参数指的是相对于seekBar来说的progress，而不是音频的progress
        * seekBar默认是的最大值是100,如果改成音频的大小，那么这里的progress参数就是音频的progress
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