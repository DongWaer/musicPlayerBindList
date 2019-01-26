package com.rmwong.musicplayerbindlist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MusicPlayerBindList extends Activity implements 
       OnClickListener,OnLongClickListener,OnItemClickListener{
	EditText songName,singerName;
	Button play,pause,stop,search;
	ListView itemsList;
	ProgressBar playProgress;
    MusicService musicService=null;
    String sdCardPath;
	File sdCardDir,mp3File;
	File[] mp3Files;
	List<String> musicFileList=new ArrayList<String>();
	Object[] musicArrayList;
	int currentItem;
	Handler myHandler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_player_bind_list);
			// 获取UI控件
		songName = (EditText)findViewById(R.id.songName);
		singerName = (EditText)findViewById(R.id.singerName);
		play = (Button)findViewById(R.id.play);
		pause = (Button)findViewById(R.id.pause);
		stop = (Button)findViewById(R.id.stop);
		search = (Button)findViewById(R.id.search);
		itemsList = (ListView)findViewById(R.id.listView1);
		playProgress = (ProgressBar)findViewById(R.id.progressBar1);
			// 为UI控件绑定事件监听器
		play.setOnClickListener(this);
		pause.setOnClickListener(this);
		stop.setOnClickListener(this);		
		stop.setOnLongClickListener(this);
		search.setOnClickListener(this);
		itemsList.setOnItemClickListener(this);
			// 获取MP3存取路径
		sdCardPath = Environment.getExternalStorageDirectory()+"/Music";
		sdCardDir = new File(sdCardPath);
			//当文件夹不存在时，创建文件夹
        if(!sdCardDir.exists())
        	sdCardDir.mkdirs();	
        	//更新ListView列表
        updateListView();
			// 绑定服务
		Intent it = new Intent("com.rmwong.musicplayerbindlist.musicService");
		bindService(it,sc,Context.BIND_AUTO_CREATE);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		// 建立Handler消息传递和处理
		myHandler = new Handler(){
			public void handleMessage(android.os.Message msg) {
				if(msg.what==MusicUtils.UPDATE_PROGRESS_SCHEDULE && musicService!=null)
				{
					playProgress.setMax(musicService.GetDuration());
					playProgress.setProgress(musicService.GetCurrentPosition());
				}
				else if(msg.what==MusicUtils.CONNECT_SUCCEED)
				{
				//	tv.setText("连接成功,正在下载......");	
					Toast.makeText(MusicPlayerBindList.this, "连接成功,正在下载......", Toast.LENGTH_LONG).show();
				}
				else if(msg.what==MusicUtils.CONNECT_FAILED || msg.what==MusicUtils.GETFILE_FAILED)
				{
				//	tv.setText("发生异常");
					Toast.makeText(MusicPlayerBindList.this, "发生异常", Toast.LENGTH_LONG).show();
				}
				else if(msg.what==MusicUtils.GETFILE_SUCCEED)
				{										
				///	tv.setText("下载完成:"+mp3Path);
					Toast.makeText(MusicPlayerBindList.this, "下载完成", Toast.LENGTH_LONG).show();
					updateListView();
				}				
			};
		};
/*		
		//建立定时器任务，使用定时器触发刷新任务
		TimerTask oneSecTask = new TimerTask() {		
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Message msg = new Message();
				msg.what = UPDATE_PROGRESS_SCHEDULE;
				myHandler.sendMessage(msg);
			}
		};
		new Timer().schedule(oneSecTask, 0, 1000);
*/
		//建立线程任务，使用线程触发刷新任务
		new Thread(){
			public void run() {
				try{
					while(true)
					{
						Thread.sleep(1000);
						Message msg = new Message();
						msg.what = MusicUtils.UPDATE_PROGRESS_SCHEDULE;
						myHandler.sendMessage(msg);
					}					
				}
				catch(Exception e){
					e.printStackTrace();
				}
				
			}
		}.start();
	}
	
	@Override
	public boolean onLongClick(View v) {
		// TODO Auto-generated method stub
		this.finish();
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.play:
			musicService.play(sdCardPath,currentItem,musicArrayList);
			break;
		case R.id.pause:
			musicService.pause();
			break;		
		case R.id.stop:
			musicService.stop();
			break;	
		case R.id.search:
		    String mName = songName.getText().toString().replace(' ', '+');
	    	String sName = singerName.getText().toString().replace(' ', '+');
	    	new Thread(new DownloadThread(sdCardDir,myHandler,
	    			mName,sName)).start();
			break;
		}
	}
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		currentItem = arg2;
		musicService.play(sdCardPath,currentItem,musicArrayList);
	}

	
	private ServiceConnection sc = new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			musicService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			musicService = ((MusicService.MyBinder)(service)).getService();
		}
	};

	public void updateListView(){
	    	//过滤出MP3文件
	    mp3Files = sdCardDir.listFiles(new mp3Filter());
	    	//过滤出的MP3文件名全部放在数组musicArrayList中
	    musicFileList.clear();                   
	    for (int i=0; i<mp3Files.length; i++ )
	    {                        
	    	musicFileList.add(mp3Files[i].getName());
	    }
	    musicArrayList = musicFileList.toArray();  
	    itemsList.setAdapter(new ArrayAdapter<String>(this,
	    		android.R.layout.simple_expandable_list_item_1,musicFileList));
	}
}
