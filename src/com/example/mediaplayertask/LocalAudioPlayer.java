package com.example.mediaplayertask;

import java.io.IOException;
import java.util.Random;

import tools.Tool;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.sax.StartElementListener;
import android.widget.Toast;

public class LocalAudioPlayer extends MediaPlayer implements IAudioPlayer {

	private Context context;
	private LocalAudioInfo localAudioInfo;// 本地音频特有
	private Cursor cursor;// 本地音频特有
	private boolean prepared = false;
	private boolean paused = false;

	public LocalAudioPlayer(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
		this.localAudioInfo = new LocalAudioInfo();
	}

	public void setCursor(Cursor cursor) {
		this.cursor = cursor;
	}

	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
	}

	public boolean isPrepared() {
		// TODO Auto-generated method stub
		return this.prepared;
	}

	public boolean isPaused() {
		// TODO Auto-generated method stub
		return this.paused;
	}

	public LocalAudioInfo getLocalAudioInfo() {
		return this.localAudioInfo;

	}

	public void playPause() {
		// TODO Auto-generated method stub
		if (isPlaying()) {
			pause();
			this.paused = true;
		} else {
			Toast.makeText(context, "音乐已暂停", Toast.LENGTH_SHORT).show();
		}
	}

	public void playPrevious() {
		if (cursor == null) {
			Toast.makeText(context, "无音乐文件", Toast.LENGTH_SHORT).show();
		} else if (cursor.isFirst()) {
			Toast.makeText(context, "已经是第一首", Toast.LENGTH_SHORT).show();
		} else {
			cursor.moveToPrevious();
			playPrepared();
			start();
		}
	}

	public void playNext() {
		if (cursor == null) {
			Toast.makeText(context, "无音乐文件", Toast.LENGTH_SHORT).show();
		} else if (cursor.isLast()) {
			Toast.makeText(context, "已经是最后一首", Toast.LENGTH_SHORT).show();
		} else {
			cursor.moveToNext();
			playPrepared();
			start();
		}
	}

	public void playPrepared() {
		// TODO Auto-generated method stub
		Uri contentUri = localAudioInfo.getCurrentAudioUri(cursor);
		reset();
		try {
			setDataSource(context.getApplicationContext(), contentUri);
			setAudioStreamType(AudioManager.STREAM_MUSIC);
			prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		prepared = true;
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub
		if (isPaused()) {
			start();
			paused = false;
		} else {
			playPrepared();
			start();
			paused = false;
		}
	}
}
