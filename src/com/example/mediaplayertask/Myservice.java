package com.example.mediaplayertask;

import java.io.IOException;

import android.R.integer;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore.Audio;
import android.widget.MediaController.MediaPlayerControl;

//when using a MediaPlayer from your main thread, you should call prepareAsync()
//rather than prepare(), and implement a MediaPlayer.OnPreparedListener in order
//to be notified when the preparation is complete and you can start playing. 
//使用Service务必去配置文件那里注册
public class Myservice extends Service implements
		MediaPlayer.OnPreparedListener {

	
	
	private LocalAudioPlayer localAudioPlayer = new LocalAudioPlayer(this);
	//private NetWorkAudioPlayer netWorkAudioPlayer = new NetWorkAudioPlayer(this);
	
	
	public static final int NOTIFICATION_ID = 1;
	public Myservice() {

	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		// getStringExtra（）检索数据
		String songName = intent.getStringExtra("songName");
		
		
//		Intent openIntent = new Intent(getApplicationContext(),
//				LocalAudioPlayerActivity.class);
		Intent openIntent = new Intent(getApplicationContext(),
				NetWorkAudioPlayer.class);
		
		
		// FLAG_ACTIVITY_CLEAR_TOP:上一个被打开的活动会被终结掉，实现没有两个相同的活动被同时打开。
		openIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		/*
		 * PendingIntent用这几个getActivity(Context, int, Intent, int),
		 * getActivities(Context, int, Intent[], int), getBroadcast(Context,
		 * int, Intent, int), and getService(Context, int, Intent, int); 来创建实例
		 * ，可以跳转到另一个应用程序
		 * PendingIntent这个类用于处理即将发生的事情。比如在通知Notification中用于跳转页面，但不是马上跳转。
		 * Intent一般是用作Activity、Sercvice、BroadcastReceiver之间传递数据，
		 * 而Pendingintent，一般用在 Notification上，可以理解为延迟执行的intent，
		 * PendingIntent是对Intent一个包装。
		 */
		// 如果PendingIntent已经存在，保留它并且只替换它的extra数据。
		PendingIntent pendingIntent = PendingIntent.getActivity(
				getApplicationContext(), 0, openIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);

		// Notification第一步：获取系统服务
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		// 第二步：创建对象
		Notification notification = new Notification();
		// 第三步：设置事件属性
		notification.tickerText = "This is the tickerText.";// 设置显示在状态栏的文字
		notification.icon = R.drawable.ic_launcher;// 设置图标
		notification.when = System.currentTimeMillis();// 设置发送时间
		notification.defaults = Notification.DEFAULT_ALL;// 设置默认声音，默认振动，默认闪光灯，记得注册事件
		/*
		 * 设置flag位 FLAG_AUTO_CANCEL 打开应用程序后自动清除掉 FLAG_NO_CLEAR 该通知不会自动清除掉
		 * FLAG_ONGOING_EVENT 通知放置在正在运行 FLAG_INSISTENT 是否一直进行，比如音乐一直播放，直到用户响应
		 */
		notification.flags = Notification.FLAG_ONGOING_EVENT;
		/*
		 * 参数 1: The context for your application / activity. 2： The title that
		 * goes in the expanded entry. 3： The text that goes in the expanded
		 * entry. 4： The intent to launch when the user clicks the expanded
		 * notification. If this is an activity, it must include the
		 * FLAG_ACTIVITY_NEW_TASK flag, which requires that you take care of
		 * task management as described in the Tasks and Back Stack document.
		 */
		notification.setLatestEventInfo(getApplicationContext(), "MusicPlayer",
				"Now  Playing:" + songName, pendingIntent);
		// 第四步：发送Notification通知
		notificationManager.notify(NOTIFICATION_ID, notification);
		return super.onStartCommand(intent, flags, startId);

		// startForeground(NOTIFICATION_ID, notification);
		// //开始前台服务，同时将notification和id绑定

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	@Override
	// Called when MediaPlayer is ready
	public void onPrepared(MediaPlayer player) {
		// TODO Auto-generated method stub
		player.start();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Not yet implemented");
	}

}
