package net.jstudio.gagCore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jstudio.gagfun.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;


public class NineGAG {
	//Some constants
	private final String _sMainPage = "http://9gag.com/";
	private final String _sImg = "http://9gag.com/new/json?list=";
	private static final int _iUpdate = 5;
	private static final String _sSavedFileName = "saved";
	
	private List<GagEntry> 	l_hot 		= new ArrayList<GagEntry>(),
							l_discover 	= new ArrayList<GagEntry>(),
							l_trending 	= new ArrayList<GagEntry>();
							
	private int point_hot = 0, point_discover = 0, point_trending = 0;
	private DefaultHttpClient httpclient;
	private LoadFirstEntriesFinishedListener loadFinished;
	private ProgressDialog progressDialog;
	private Context _context;
	
	
	public List<GagEntry> getListHot(){return l_hot;}
	public List<GagEntry> getListdiscover(){return l_discover;}
	public List<GagEntry> getListTrending(){return l_trending;}
	public List<GagEntry> getList(EntryType type){
		switch(type){
			case TRENDING:
				return l_trending;
			case DISCOVER:					
				return l_discover;
		}
		//by default
		return l_hot;
	}
	
	public void setLoadFirstEntriesFinished(LoadFirstEntriesFinishedListener finished){loadFinished = finished;}
	
	public HttpClient getHttpClient(){return httpclient;}
	private void updateNewEntries(EntryType type) throws ClientProtocolException, IOException{
		HttpGet httpget;
		switch(type){
			case TRENDING:
				httpget = new HttpGet(_sImg + "trending&id=" 
									+ l_trending.get(l_trending.size() - 1).getID());
				break;
			case DISCOVER:
				httpget = new HttpGet(_sImg + "discover&id=" 
										+ l_discover.get(l_discover.size() - 1).getID());
				break;
			default://HOT	
				httpget = new HttpGet(_sImg + "hot&id="
										+ l_hot.get(l_hot.size() - 1).getID());
				break;
		}
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if(entity != null){
			InputStream in = entity.getContent();
			StringBuilder str = new StringBuilder();
			int i;
			while(( i = in.read()) != -1){
				str.append((char)i);			
			}			
			try {
				
				JSONObject json = new JSONObject(str.toString());	
				JSONArray arr = json.getJSONArray("ids");
				JSONObject json_items = json.getJSONObject("items");
				
				StringBuilder strb = new StringBuilder();
				
				for(int j = 0; j < json_items.length(); j++)					
					strb.append(json_items.getString("entry-" + arr.getInt(j)));
				
				if(type == EntryType.DISCOVER)
					l_discover.addAll(produceEntriesFromString(strb.toString(), type));
				else if(type == EntryType.TRENDING)
					l_trending.addAll(produceEntriesFromString(strb.toString(), type));
				else
					l_hot.addAll(produceEntriesFromString(strb.toString(), type));				
				
			} catch (JSONException e) {				
				e.printStackTrace();
			}
			
		}
	}
	//This function must be called when touching for the next entry
	//Because it is used for update new entries
	public GagEntry Next(EntryType type){		
		if( (l_hot.size() != 0) && (l_hot.size() - point_hot) <= _iUpdate)
			try {
				updateNewEntries(type);
			} catch (Exception e) {}
		
		if((l_discover.size() != 0 && ((l_discover.size() - point_discover) <= _iUpdate)))
			try {
				updateNewEntries(type);
			} catch (Exception e) {}
		
		if((l_trending.size() != 0 && ((l_trending.size() - point_trending) <= _iUpdate)))
			try {
				updateNewEntries(type);
			} catch (Exception e) {}
		
		if(type == EntryType.TRENDING)			
			return l_trending.get(point_trending++);
		
		if(type == EntryType.DISCOVER)			
			return l_discover.get(point_discover++);
		
		//for Default(hot)
		return l_hot.get(point_hot++);
	}
	public int getPointHot(){return point_hot;}
	public int getPointDiscover(){return point_discover;}
	public int getPointTrending(){return point_trending;}
	
	private List<GagEntry> produceEntriesFromString(String str, EntryType type){
		Pattern par_gagid = Pattern.compile("gagid=\"[0-9]*\"", Pattern.CASE_INSENSITIVE);
		Pattern par_datatext = Pattern.compile("data-text=\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
		Pattern par_dataurl = Pattern.compile("data-url=\"[^\"]*\"", Pattern.CASE_INSENSITIVE);;
		
		Matcher match_id = par_gagid.matcher(str);
		Matcher match_text = par_datatext.matcher(str);
		Matcher match_dataurl = par_dataurl.matcher(str);
		List<GagEntry> list_entry = new ArrayList<GagEntry>();
		while(match_id.find() && match_text.find() && match_dataurl.find()){
			
			int id = Integer.parseInt(match_id.group().replaceFirst("(?i)gagid=\"", "")
					.replaceFirst("\"", ""));
			
			String name = match_text.group().replaceFirst("(?i)data-text=\"", "")
					.replaceFirst("\"", "");				
			name = Utilities.ConvertHTMLEntities(name);
			String url = match_dataurl.group().replaceFirst("(?i)data-url=\"", "")
					.replaceFirst("\"", "");
			
			//Add get link
			String strFind;
			if(str.substring(0, 2).compareTo("<l") == 0) //if json data
				strFind = "<a href=\"/gag/" + Integer.toString(id) + "\"  target=\"_blank\" ><img src=\"";
			else
				strFind = "<a href=\"/gag/" + Integer.toString(id) + "\"  target=\"_blank\" >\n\t\t<img src=\"";
			int f1 = str.indexOf(strFind);
			f1 += strFind.length();
			int f2 = str.indexOf("\"", f1);			
			String link = str.substring(f1, f2); 
		
			list_entry.add(new GagEntry(httpclient, id, name, url, link, type));
		}
		return list_entry;
	}
	private List<GagEntry> getFirstEntries(EntryType type) throws ClientProtocolException, IOException{
		HttpGet httpget;		
		switch (type){				
			case DISCOVER:
				httpget = new HttpGet(_sMainPage + "discover/");
				break;
			case TRENDING:
				httpget = new HttpGet(_sMainPage + "trending/");
				break;
			default:
				httpget = new HttpGet(_sMainPage + "hot/");
				break;
		}
		
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		if(entity != null){
			InputStream in = entity.getContent();
			StringBuilder str = new StringBuilder();
			int i;
			while(( i = in.read()) != -1){
				str.append((char)i);
			}
			String str_m = str.toString();			
			return produceEntriesFromString(str_m, type);
		}
		return null;
	}
	
	public NineGAG(Context context){
		_context = context;
		//Create Thread-Safe HTTPClient
		HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpProtocolParams.setUseExpectContinue(params, true);
		
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));		
		ClientConnectionManager conMng = new ThreadSafeClientConnManager(params, schemeRegistry);
		
		httpclient = new DefaultHttpClient(conMng, params);
	}

	public void StartDownloadFirstPage(EntryType type){
		progressDialog = ProgressDialog.show(_context, "", _context.getString(R.string.Loading));		
		LoadFirstEntriesTask lfeT_hot = new LoadFirstEntriesTask();		
		lfeT_hot.execute(type);
	}
	
	private enum DataToFile{
		ID,
		NAME,
		URL,
		IMGLINK
	}
	private String getJSONString(EntryType type){
		JSONArray array = new JSONArray();
		List<GagEntry> list = null;
		switch(type){
		case HOT:
			list = l_hot;
			break;
		case TRENDING:
			list = l_trending;
			break;
		}
		for(GagEntry entry : list){
			JSONObject js_item = new JSONObject();
			try {
				js_item.put(DataToFile.ID.toString(), entry.getID());
				js_item.put(DataToFile.NAME.toString(), entry.getEntryName());
				js_item.put(DataToFile.URL.toString(), entry.getEntryUrl());
				js_item.put(DataToFile.IMGLINK.toString(), entry.getLink());
				array.put(js_item.toString());
			} catch (JSONException e) {				
			}				
		}
		return array.toString();
	}
	public void SaveDataToStorage(){		
		try {
			FileOutputStream fos = _context.openFileOutput(_sSavedFileName, Context.MODE_PRIVATE);
			JSONObject json = new JSONObject();
			try {
				json.put(EntryType.HOT.toString(), getJSONString(EntryType.HOT));
				json.put(EntryType.TRENDING.toString(), getJSONString(EntryType.TRENDING));
			} catch (JSONException e) {
			}
			fos.write(json.toString().getBytes());
			fos.close();
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		}
	}
	
	
	public boolean LoadDataFromFile(){
		try{										
			FileInputStream fis = _context.openFileInput(_sSavedFileName);			
			StringBuilder sf = new StringBuilder();
			int c;
			while((c = fis.read()) != -1)
				sf.append((char)c);			
			fis.close();
			//Process String
			String sResult = sf.toString();
			try {
				JSONObject json = new JSONObject(sResult);
				//HOT
				JSONArray arr = new JSONArray(json.getString(EntryType.HOT.toString()).toString());
				for(int i = 0; i < arr.length() - 1; i++){
					JSONObject js_item = new JSONObject(arr.getString(i));
					l_hot.add(new GagEntry(httpclient,
							js_item.getInt(DataToFile.ID.toString()),
							js_item.getString(DataToFile.NAME.toString()),
							js_item.getString(DataToFile.URL.toString()),
							js_item.getString(DataToFile.IMGLINK.toString()),
							EntryType.HOT
							));
				}
				//TRENDING
				arr = new JSONArray(json.getString(EntryType.TRENDING.toString()).toString());
				for(int i = 0; i < arr.length() - 1; i++){
					JSONObject js_item = new JSONObject(arr.getString(i));
					l_trending.add(new GagEntry(httpclient,
							js_item.getInt(DataToFile.ID.toString()),
							js_item.getString(DataToFile.NAME.toString()),
							js_item.getString(DataToFile.URL.toString()),
							js_item.getString(DataToFile.IMGLINK.toString()),
							EntryType.TRENDING
							));
				}
			} catch (JSONException e) {				
			}
		}catch(FileNotFoundException e){
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	
	private class LoadFirstEntriesTask extends AsyncTask<EntryType, Void, List<GagEntry>>{

		@Override
		protected List<GagEntry> doInBackground(EntryType... params) {
			try {
				return getFirstEntries(params[0]);
			} catch (ClientProtocolException e) {
			} catch (IOException e) {}
			return null;
		}

		@Override
		protected void onPostExecute(List<GagEntry> result) {			
			EntryType type = result.get(0).getEntryType();
			if(type == EntryType.DISCOVER)
				l_discover.addAll(result);
			else if (type == EntryType.TRENDING)
				l_trending.addAll(result);
			else
				l_hot.addAll(result);
			if(loadFinished != null)
				loadFinished.OnLoadFirstEntriesFinished();
			
			//Close Progress Dialog
			progressDialog.dismiss();
		}	
		
		
	}
	
	public interface LoadFirstEntriesFinishedListener{
		public void OnLoadFirstEntriesFinished();
	}
}
