package com.example.mediaplayertask;

import android.R.integer;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class LocalAudioInfo {
	private static final String TAG ="LocalAudioInfo";
	public long getCurrentAudioId(Cursor cursor){
		int idColumn =cursor.getColumnIndex(MediaStore.Audio.Media._ID);//����ID
		long thisId =cursor.getLong(idColumn);
		return thisId;
	}
	public String getCurrentAudioTitle(Cursor cursor){
		int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);//��������
		String thisTitle =cursor.getString(titleColumn);
		return thisTitle;
	}
	
	public Uri getCurrentAudioUri(Cursor cursor){
		long thisId =getCurrentAudioId(cursor);
		Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,thisId);//��id��contentUri���ӳ�һ���µ�Uri
		return contentUri;
	}
}
