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
	private String _entryName, _entryUrl, _linkImg, _loveCount;
	private EntryType _type;
	private boolean _isDownloaded;
	private Bitmap m_bmp;
	private HttpClient _httpClient;
	private List<DownloadFinishedListener> dlFinishListener;
	private DownloadImageTask dlTask;
	
	//Number of likes, loves
	private GetCallback gcbLikes = null, gcbLoves = null;
	private GetNumberOfLikesTask likesTask = null;
	private GetNumberOfLovesTask lovesTask = null;
	
	
	private static final String likeapi_begin = "http://api.facebook.com/method/fql.query?query=select%20total_count%20from%20link_stat%20where%20url=%279";
	private static final String likeapi_end = "%27&format=json";
	private static final String fbcommentapi = "http://www.facebook.com/plugins/comments.php?href=";
	//
	public GagEntry(HttpClient client,
			int id, 
			String entryName,
			String entryUrl,
			String link,
			String loveCount,
			EntryType type
			){
		_httpClient = client;
		this._id = id;
		this._entryName = entryName;		
		this._entryUrl = entryUrl;this._type = type;
		this._linkImg = link;
		this._loveCount = loveCount;
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
	public String getFBCommentLink(){return fbcommentapi + _entryUrl;}
	public String getLoveCount(){return _loveCount;}
	
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
	
	public boolean isNSFW(){
		return !_linkImg.contains("jpg");
	}
	
	public synchronized void addDownloadFinished(DownloadFinishedListener dl){
		dlFinishListener.add(dl);
	}

	public interface GetCallback{
		public void OnGetCallBackInt(int value);
	}
	
	public void getLovesRealTime(GetCallback gcb){
		gcbLoves = gcb;
		if(lovesTask != null && lovesTask.getStatus() == AsyncTask.Status.RUNNING){
			lovesTask.cancel(true);
			lovesTask = null;
		}
		lovesTask = new GetNumberOfLovesTask();
		lovesTask.execute(_entryUrl, Integer.toString(_id));
	}
	
	private class GetNumberOfLovesTask extends AsyncTask<String, Void, Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			Integer re = null;
			try{
				String currentID = params[1];
				HttpUriRequest request = new HttpGet(params[0]);				
				HttpResponse response = _httpClient.execute(request);
				HttpEntity entity = response.getEntity();
				if(entity != null){
					StringBuilder str_b = new StringBuilder();
					InputStream in = entity.getContent();
					int i;
					while(( i = in.read()) != -1)
						str_b.append((char)i);					
					String str = str_b.toString();
					//
					String strFind = "love_count_" + currentID;
					int f1 = str.indexOf(strFind);
					f1 += strFind.length();
					f1 = str.indexOf(">", f1);
					int f2 = str.indexOf("<", f1);
					return Integer.parseInt(str.substring(f1 + 1, f2));
				}
			}catch(Exception e){
				re = Integer.parseInt(_loveCount);
			}
			return re;
		}

		@Override     
		protected void onPostExecute(Integer result) {
			if(gcbLoves != null){
				gcbLoves.OnGetCallBackInt(result.intValue());
				_loveCount = result.toString();
			}
		}
		
		
	}
	public void getLikes(GetCallback gcb){
		gcbLikes = gcb;
		likesTask = new GetNumberOfLikesTask();
		String sRequest = likeapi_begin + _entryUrl + likeapi_end;
		likesTask.execute(sRequest);
	}

	private class GetNumberOfLikesTask extends AsyncTask<String, Void, Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			Integer re = null;
			try{
				HttpUriRequest request = new HttpGet(params[0]);				
				HttpResponse response = _httpClient.execute(request);
				HttpEntity entity = response.getEntity();
				if(entity != null){
					StringBuilder str_b = new StringBuilder();
					InputStream in = entity.getContent();
					int i;
					while(( i = in.read()) != -1)
						str_b.append((char)i);					
					String str = str_b.toString();
					int f1 = str.indexOf(":");
					int f2 = str.indexOf("}", f1);
					re = new Integer(str.substring(f1 + 1, f2));
				}
			}catch(Exception e){}
			return re;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if(gcbLikes != null)
				gcbLikes.OnGetCallBackInt(result.intValue());
		}
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
