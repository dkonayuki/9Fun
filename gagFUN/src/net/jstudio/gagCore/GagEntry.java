package net.jstudio.gagCore;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
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
	private NineGAG _gag;
	private List<DownloadFinishedListener> dlFinishListener;
	private DownloadImageTask dlTask;
	
	//Number of likes, loves
	private GetCallback gcbLikes = null, gcbLoves = null;
	private GetNumberOfLikesTask likesTask = null;
	private GetEntryInfoTask infoTask = null;
	
	
	private static final String likeapi_begin = "http://api.facebook.com/method/fql.query?query=select%20total_count%20from%20link_stat%20where%20url=%279";
	private static final String likeapi_end = "%27&format=json";
	private static final String fbcommentapi = "http://www.facebook.com/plugins/comments.php?href=";
	private static final String liked_flag = "love current";
	private static final String like_link = "http://9gag.com/vote/like/id/";
	private static final String unlike_link = "http://9gag.com/vote/unlike/id/";
	//
	public GagEntry(NineGAG gag,
			int id, 
			String entryName,
			String entryUrl,
			String link,
			String loveCount,
			EntryType type
			){
		_gag = gag;
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
		return (_linkImg.contains("nsfw-mask"))||(!_linkImg.contains("jpg"));
	}
	
	public synchronized void addDownloadFinished(DownloadFinishedListener dl){
		dlFinishListener.add(dl);
	}

	public void Like(LikeDisLikeCallback ldk){
		try {
			String request = like_link + Integer.toString(_id);
			HttpGet get = _gag.getHttpGet(request);
			_gag.getHttpClient().execute(get);
			if(ldk != null)
				ldk.OnLikeDisLike();
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
	}
	
	public void UnLike(LikeDisLikeCallback ldk){
		try {
			String request = unlike_link + Integer.toString(_id);
			HttpGet get = _gag.getHttpGet(request);
			_gag.getHttpClient().execute(get);
			if(ldk != null)
				ldk.OnLikeDisLike();
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
	}
	public interface GetCallback{
		public void OnGetCallBackInt(int loves);
		public void OnGetCallBackInfo(int loves, boolean isLiked);
	}
	public interface LikeDisLikeCallback{
		public void OnLikeDisLike();
	}
	
	public void getEntryInfoRealTime(GetCallback gcb){
		if(infoTask != null && infoTask.getStatus() == AsyncTask.Status.RUNNING){
			infoTask.cancel(true);
			infoTask = null;
		}
		gcbLoves = gcb;
		infoTask = new GetEntryInfoTask();
		infoTask.execute(_entryUrl, Integer.toString(_id));
	}
	
	private class ResultInfo{
		public int loves;
		public boolean isLiked;
	}
	private class GetEntryInfoTask extends AsyncTask<String, Void, ResultInfo>{

		@Override
		protected ResultInfo doInBackground(String... params) {
			ResultInfo re = new ResultInfo();
			try{
				String currentID = params[1];
				HttpGet get = _gag.getHttpGet(params[0]);
				String str = Utilities.ReadInputHTTP(_gag.getHttpClient().execute(get));
				//Number of loves
				String strFind = "love_count_" + currentID;
				int f1 = str.indexOf(strFind);
				f1 += strFind.length();
				f1 = str.indexOf(">", f1);
				int f2 = str.indexOf("<", f1);
				if(str.substring(f1 + 1, f2).contains(NineGAG.love_count_dot))
					re.loves = 0;
				re.loves = Integer.parseInt(str.substring(f1 + 1, f2));
				//Is Liked
				re.isLiked = str.contains(liked_flag);

			}catch(Exception e){
				re.loves = Integer.parseInt(_loveCount);
				re.isLiked = false;
			}
			return re;
		}

		@Override     
		protected void onPostExecute(ResultInfo result) {
			if(gcbLoves != null){
				gcbLoves.OnGetCallBackInt(result.loves);
				gcbLoves.OnGetCallBackInfo(result.loves, result.isLiked);
				_loveCount = String.valueOf(result.loves);
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
				HttpResponse response = _gag.getHttpClient().execute(request);
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
				HttpResponse response = _gag.getHttpClient().execute(request);

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
