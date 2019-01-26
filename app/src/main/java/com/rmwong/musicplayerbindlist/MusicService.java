package com.rmwong.musicplayerbindlist;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

public class MusicService extends Service {
	private MediaPlayer meadiaPlayer=null;
	private final IBinder binder = new MyBinder();
	public class MyBinder extends Binder {
		MusicService getService(){
			return MusicService.this;
		}
	}
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(this, "∞Û∂®“Ù¿÷≤•∑≈∆˜",Toast.LENGTH_LONG).show();

	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Toast.makeText(this, "Õ£÷π“Ù¿÷≤•∑≈∆˜",Toast.LENGTH_LONG).show();
		if(meadiaPlayer!=null){
			meadiaPlayer.stop();
			meadiaPlayer.release();
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub		
		return super.onStartCommand(intent, flags, startId);
	}
	
	public void play(String path,int currentListItem,Object[] mMusicList){
		if(meadiaPlayer==null){
			meadiaPlayer = new MediaPlayer();
	        try {
	        	Uri playUri = Uri.parse(path+"/"+mMusicList[currentListItem]);
	        	meadiaPlayer.setDataSource(getApplicationContext(),playUri);
	        	meadiaPlayer.prepare();
	        	meadiaPlayer.start();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		if((!meadiaPlayer.isPlaying())&&meadiaPlayer!=null)
			meadiaPlayer.start();
	}
	public void pause(){
		if((meadiaPlayer.isPlaying())&&meadiaPlayer!=null)
			meadiaPlayer.pause();
	}	
	public void stop(){
		if((meadiaPlayer.isPlaying())&&meadiaPlayer!=null){
			meadiaPlayer.stop();
			meadiaPlayer.release();
			meadiaPlayer=null;
		}
	}	
    public int GetDuration(){
    	if(meadiaPlayer!=null)
    		return meadiaPlayer.getDuration();
    	else
    		return 100;
    }

    public int GetCurrentPosition(){
    	if(meadiaPlayer!=null)
    		return meadiaPlayer.getCurrentPosition();
    	else
    		return 0;
    }
}
