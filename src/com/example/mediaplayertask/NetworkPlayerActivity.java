package com.example.mediaplayertask;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class NetworkPlayerActivity extends Activity implements
		View.OnClickListener, OnCompletionListener, OnPreparedListener {
	
	
	private NetWorkAudioPlayer netWorkAudioPlayer = new NetWorkAudioPlayer(
			NetworkPlayerActivity.this);
	private LocalAudioPlayer localAudioPlayer = new LocalAudioPlayer(this);
	
	private Button playButton;
	private Button preButton;
	private Button nextButton;
	private Button loopButton;
	private Button jumpButton;
	private SeekBar seekBar;
	private TextView startTextView;
	private TextView endTextView;
	private static final String INITSTRING_STRING = "0:00";
	private static final String PLAY = "Play";
	private static final String PAUSE = "Pause";
	private Intent intent;
	/*
	 * handler 主要接受子线程发送的数据, 并用此数据配合主线程更新UI.
	 * 
	 * 如果此时需要一个耗时的操作，例如: 联网读取数据,或者读取本地较大的一个文件的时候，你不能把这些操作放在
	 * 主线程中，，如果你放在主线程中的话，界面会出现假死现象, 如果5秒钟还没有完成的话，，会收到Android 系统的一个错误提示 "强制关闭".
	 * 这个时候我们需要把这些耗时的操作，放在一个子线程中,因为子线程涉及到
	 * UI更新，，Android主线程是线程不安全的，也就是说，更新UI只能在主线程中更新，子线程中操作是危险的.
	 * 这个时候，Handler就出现了.,来解决这个复杂的问题 , 由于Handler运行在主线程中(UI线程中),
	 * 它与子线程可以通过Message对象来传递数据, 这个时候，Handler就承担着接受子线程传过来的(子线程用
	 * sendMessage()方法传弟)Message对象，(里面包含数据) , 把这些消息放入主线程队列中，配合主 线程进行更新UI。
	 */

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (netWorkAudioPlayer != null
						&& netWorkAudioPlayer.isPlaying()) {

					int totalTime = netWorkAudioPlayer.getDuration();// 获取时间
					int currentTime = netWorkAudioPlayer.getCurrentPosition();// //获取当前播放点
					endTextView.setText(getTimeText(totalTime));
					int seekBarMax = seekBar.getMax();
					if (totalTime > 0 && currentTime > 0 && seekBarMax > 0) {
						startTextView.setText(getTimeText(currentTime));
						seekBar.setProgress((int) (seekBarMax
								* (float) currentTime / totalTime));// 更新进度条
					}
				}
				break;

			}
		}

		private String getTimeText(int time) {
			// TODO Auto-generated method stub
			/*
			 * 传入的time参数单位为milliseconds，即毫秒 这个方法可以将毫秒单位的时间转换为0：00形式的时间
			 */
			int totalSeconds = time / 1000;// 毫秒转化为秒
			int minutes = totalSeconds / 60;// 转化为分钟
			int seconds = totalSeconds % 60;// 转化为秒
			String showTime;
			if (seconds > 9 && seconds < 60) {
				showTime = minutes + ":" + seconds;
			} else {
				showTime = minutes + ":0" + seconds;

			}
			return showTime;
		}

	};

	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.networkplayer);
		initView();
		setListener();
		UpdateSeekBarThread thread = new UpdateSeekBarThread();
		thread.start();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		netWorkAudioPlayer.release();
		netWorkAudioPlayer = null;
		if (intent != null) {
			stopService(intent);
		}
		super.onDestroy();
	}

	public void initView() {
		playButton = (Button) this.findViewById(R.id.playButton);
		preButton = (Button) this.findViewById(R.id.preButton);
		nextButton = (Button) this.findViewById(R.id.nextButton);
		loopButton = (Button) this.findViewById(R.id.loopButton);
		seekBar = (SeekBar) this.findViewById(R.id.seekBar);
		startTextView = (TextView) this.findViewById(R.id.startTextView);
		endTextView = (TextView) this.findViewById(R.id.endTextView);
		jumpButton = (Button) this.findViewById(R.id.jumpButton);
	}

	public void setListener() {
		playButton.setOnClickListener(this);
		preButton.setOnClickListener(this);
		nextButton.setOnClickListener(this);
		loopButton.setOnClickListener(this);
		jumpButton.setOnClickListener(this);
		netWorkAudioPlayer.setOnPreparedListener(this);
		netWorkAudioPlayer.setOnCompletionListener(this);
		seekBar.setOnSeekBarChangeListener(new ProgressBarListener());

	}

	private void setOnSeekBarChangeListener(
			ProgressBarListener progressBarListener) {
		// TODO Auto-generated method stub

	}

	// 耗时操作开始子线程
	public class UpdateSeekBarThread extends Thread {
		public void run() {
			while (netWorkAudioPlayer != null) {
				try {
					Thread.sleep(30);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendEmptyMessage(0);
			}
		}
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		netWorkAudioPlayer.start();
		netWorkAudioPlayer.setPaused(false);
		netWorkAudioPlayer.setPrepared(true);
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		// TODO Auto-generated method stub
		playButton.setText(PLAY);

	}

	private class ProgressBarListener implements OnSeekBarChangeListener {

		@Override
		/*
		 * 参数 seekBar The SeekBar whose progress has changed progress The
		 * current progress level. This will be in the range 0..max where max
		 * was set by setMax(int). (The default value for max is 100.) fromUser
		 * True if the progress change was initiated by the user.
		 */
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			if (fromUser) {
				netWorkAudioPlayer.seekTo((int) (netWorkAudioPlayer
						.getDuration() * (float) progress / seekBar.getMax()));
				seekBar.setProgress(progress);
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.playButton:
			if (PLAY.equals(playButton.getText())) {
				netWorkAudioPlayer.play();
				playButton.setText(PAUSE);
			} else {
				netWorkAudioPlayer.playPause();
				playButton.setText(PLAY);
			}
			break;

		case R.id.preButton:
			netWorkAudioPlayer.playPrevious();
			//playButton.setText(PAUSE);
			break;
		case R.id.nextButton:
			netWorkAudioPlayer.playNext();
			//playButton.setText(PAUSE);
			break;
		case R.id.loopButton:
			netWorkAudioPlayer.setLooping(true);
			break;
		case R.id.jumpButton:
			Intent intent = new Intent(NetworkPlayerActivity.this,
					LocalAudioPlayerActivity.class);// 从网络获取音频转到本地播放音频跳
			startActivity(intent);
			localAudioPlayer.playPrepared();
			break;
		}
		intent = new Intent(NetworkPlayerActivity.this, Myservice.class);
		intent.putExtra("songName", "This is networkPlayer's song.");// Add extended data to the intent.
		startActivity(intent);
	}

}
