package net.jstudio.gagCore;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class GagEntry {
	private int _id;
	private String _entryName, _entryUrl, _linkImg;
	private EntryType _type;
	private boolean _isDownloaded;
	private Bitmap m_bmp;
	private HttpClient _httpClient;
	private List<DownloadFinishedListener> dlFinishListener;
	private DownloadImageTask dlTask;
	public GagEntry(HttpClient client,
			int id, 
			String entryName,
			String entryUrl,
			String link,
			EntryType type
			){
		_httpClient = client;
		this._id = id;
		this._entryName = entryName;		
		this._entryUrl = entryUrl;this._type = type;
		this._linkImg = link;
		_isDownloaded = false;
		dlFinishListener = new ArrayList<DownloadFinishedListener>();
	}	
	
	public int getID(){return _id;}
	public String getEntryName(){return _entryName;}
	public String getEntryUrl(){return _entryUrl;}
	public EntryType getEntryType(){return _type;}
	public String getLink(){return _linkImg;}
	public boolean isDownloaded(){return _isDownloaded;}
	public Bitmap getBitmap(){return m_bmp;}
	public void StartDownloadBitmap(){
		if(!_isDownloaded){
			dlTask = new DownloadImageTask();
			dlTask.execute(_linkImg);
		}
	}
	
	public void DisposeImage(){
		if(_isDownloaded && m_bmp != null){
			m_bmp.recycle();
			m_bmp = null;			
		}else{
			if(dlTask != null && dlTask.getStatus() == AsyncTask.Status.RUNNING){
				dlTask.cancel(true);
				dlTask = null;
			}
		}		
		_isDownloaded = false;
	}
	
	
	public synchronized void addDownloadFinished(DownloadFinishedListener dl){
		dlFinishListener.add(dl);
	}
	
	public interface DownloadFinishedListener{
		public void OnDownloadFinished();
	}
	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = null;
			try {
				HttpUriRequest request = new HttpGet(params[0]);				
				HttpResponse response = _httpClient.execute(request);

				StatusLine statusLine = response.getStatusLine();
				int statusCode = statusLine.getStatusCode();
				if (statusCode == 200) {
					HttpEntity entity = response.getEntity();
					InputStream in = entity.getContent();
					BitmapFactory.Options ops = new BitmapFactory.Options();
					ops.inDensity = 1;
					ops.inTargetDensity = 1;
					bitmap = BitmapFactory.decodeStream(in, null, ops);			
				}
			} catch (Exception e) {
				Log.d("debug", "Download Image Error");
			}
			return bitmap;
		}

		@Override
		protected synchronized void onPostExecute(Bitmap result) {
			if(result != null){
				_isDownloaded = true;
				m_bmp = result;
				if(dlFinishListener != null){
					Iterator<DownloadFinishedListener> i = dlFinishListener.iterator();
					while(i.hasNext()){
						i.next().OnDownloadFinished();
					}
				}
			}
		}

		@Override
		protected void onCancelled() {
			
		}
		
		
	}
}
