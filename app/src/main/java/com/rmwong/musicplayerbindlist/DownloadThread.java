package com.rmwong.musicplayerbindlist;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Environment;
import android.os.Handler;

public class DownloadThread implements Runnable{
	private String content,strid;
	private Handler myHandler = null;
	private File sdCardDir,mMp3File;
	String mName,sName;
	public DownloadThread(File sdDir,Handler handler,
			String mp3Name,String singerName) {
		// TODO Auto-generated constructor stub
		sdCardDir = sdDir;
		myHandler = handler;
		mName     = mp3Name;
		sName     = singerName;
	}

			@Override
			public void run() {
				// TODO Auto-generated method stub
				//获取编辑框内容,并将空格用+取代
		    	//创建URL,指明IP地址及参数
		     	String strUrl = "http://box.zhangmen.baidu.com/x?op=12&count=1&title=" +  
		                mName + "$$"+ sName +"$$$$"; 
		     	//创建HTTP连接客户端
		    	DefaultHttpClient httpclient = new DefaultHttpClient();
		    	//使用GET方法建立连接
		    	HttpGet httpget = new HttpGet(strUrl);
		    	//用于处理服务器返回的数据
		    	ResponseHandler<String> responseHandler = new BasicResponseHandler();
		    	try {
		    		//获取服务器返回的数据
					content = httpclient.execute(httpget,responseHandler);
					//发送Handler消息
					myHandler.sendEmptyMessage(MusicUtils.CONNECT_SUCCEED);							
					//获取资源
					getResource();
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					//当连接建立失败时,发送Headler消息,通知主线程
					myHandler.sendEmptyMessage(MusicUtils.CONNECT_FAILED);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					myHandler.sendEmptyMessage(MusicUtils.CONNECT_FAILED);
					e.printStackTrace();
				}
				//关闭连接
				httpclient.getConnectionManager().shutdown();
			};
	
	/**
	 * 当与服务器端建立连接后,解析xml文档获取真正地址并获取资源
	 */
	private void getResource()
	{
		int begin = 0, end = 0, number = 0;
		//获取服务器端返回的xml文档
		String sb = content;
		//获取<encode>标签的位置
		begin = sb.indexOf("<encode>");
		if (begin != -1) {  
			//获取</encode>标签的位置
            end = sb.indexOf("</encode>", begin); 
            //获取标签对<encode>的内容
            strid = sb.substring(begin + 17, end - 3);
            //获取strid中最后一个"/"的位置
            number = strid.lastIndexOf("/");
            //获取IP地址的路径
            strid = strid.substring(0, number);
        }
		//获取<decode>标签的位置
		begin = sb.indexOf("<decode>");
		if(begin!=-1)
		{
				//获取</encode>标签的位置
			end = sb.indexOf("</decode>",begin);
				//获取参数,从而形成完整的URL地址
			strid+="/"+sb.substring(begin +17, end - 3);	
		}

		//创建新线程,用于实现网络连接
		new Thread(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				super.run();
				//创建HTTP连接客户端
				DefaultHttpClient httpclient = new DefaultHttpClient();
				//通过GET方式获取MP3文件
				HttpGet httpget = new HttpGet(strid);
				try {
					//执行HTTP连接
					HttpResponse httpres = httpclient.execute(httpget);
					//获取服务器端返回的数据
					HttpEntity entity = httpres.getEntity();
					//当数据不为空时,获取数据
					if(entity!=null)
					{
						//创建输入流,用于获取服务器端返回的数据
						InputStream input = entity.getContent();
						//创建输入缓冲区
						byte[] buffer = new byte[4*1024];
					/*	//获取SD卡中Music的路径
						String sdCardPath= Environment.getExternalStorageDirectory()+"/Music";
						sdCardDir = new File(sdCardPath);
						//当文件夹不存在时，创建文件夹
				        if(!sdCardDir.exists())
				        	sdCardDir.mkdirs();	
				     */						
						//创建File对象,指定存储路径及存储名称及类型
						mMp3File = new File(sdCardDir.getAbsolutePath()+"/"+
								mName+"-"
								+sName+".mp3");
						//创建新的文件
						mMp3File.createNewFile();
						//创建输出流,用于向SD卡写入数据
						FileOutputStream output = new FileOutputStream(mMp3File);
						int len;
						while((len=input.read(buffer))!=-1)
						{
							//向SD卡写入数据
							output.write(buffer,0,len);

						}
						//刷新输出流,确保文件写入完毕
						output.flush();
						//关闭输出流
						output.close();
						//关闭输入流
						input.close();
					}
					myHandler.sendEmptyMessage(MusicUtils.GETFILE_SUCCEED);					
				}//try
				catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					//文件获取失败时,发送Handler信息给主线程进行通知
					myHandler.sendEmptyMessage(MusicUtils.GETFILE_FAILED);
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					myHandler.sendEmptyMessage(MusicUtils.GETFILE_FAILED);
					e.printStackTrace();
				}
				//关闭HTTP连接
				httpclient.getConnectionManager().shutdown();
			}
		}.start();
	}
}
