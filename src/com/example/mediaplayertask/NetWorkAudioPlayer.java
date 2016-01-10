package com.example.mediaplayertask;

import java.io.IOException;
import java.util.Random;

import tools.Tool;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

public class NetWorkAudioPlayer extends MediaPlayer implements IAudioPlayer {
	private Context context;
	private boolean prepared = false;
	private boolean paused = false;
	String urlString1 = "http://sc1.111ttt.com/2015/1/12/08/105081507558.mp3";
	String urlString2 = "http://sc1.111ttt.com/2015/1/12/08/105082233058.mp3";
	String urlString3 = "http://sc1.111ttt.com/2015/1/12/09/105090933364.mp3";

	public NetWorkAudioPlayer(Context context) {
		// TODO Auto-generated constructor stub
		this.context = context;
	}
	//通过url获取网络音频资源
	private String getSource(){
		String[] strs = new String[3];
		strs[0]=urlString1;
		strs[1]=urlString2;
		strs[2]=urlString3;
		//随机获取
		return strs[new Random().nextInt(3)];
	}
	
	
	public void setPrepared(boolean prepared) {
		this.prepared = prepared;
	}
	public void setPaused(boolean paused) {
		this.paused = paused;
	}
	@Override
	public boolean isPrepared() {
		// TODO Auto-generated method stub
		return this.prepared;
	}

	@Override
	public boolean isPaused() {
		// TODO Auto-generated method stub
		return this.paused;
	}

	@Override
	public void playPause() {
		// TODO Auto-generated method stub
		if(isPlaying()){
			pause();
			paused=true;
		}
	}

	@Override
	public void playPrevious() {
		// TODO Auto-generated method stub
		if(Tool.isNetworkAvailable(context)){
			playPrepared();
		}
		
	}

	@Override
	public void playNext() {
		// TODO Auto-generated method stub
		if(Tool.isNetworkAvailable(context)){
			playPrepared();
		}
		
	}

	@Override
	public void playPrepared() {
		// TODO Auto-generated method stub
		if(Tool.isNetworkAvailable(context)){
			reset();
			try {
				setDataSource(getSource());
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
			setAudioStreamType(AudioManager.STREAM_MUSIC);
			prepareAsync();
		}
	}

	@Override
	public void play() {
		// TODO Auto-generated method stub
		if(isPaused()){
			start();
			paused=false;
			
		}else{
			playPrepared();
		}
	}
}
