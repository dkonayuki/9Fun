package net.jstudio.gagCore;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jstudio.gagfun.PublicResource;
import net.jstudio.gagfun.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

public class NineGAG {
	//Some constants
	private static final String _sMainPage = "http://9gag.com/";
	private static final String _sImg = "http://9gag.com/new/json?list=";
	private static final String _sLogin = "https://9gag.com/login";
	private static final String _sCheckSafeModeIsOff = "safe-mode-toggle off";
	private static final int _iUpdate = 5;
	private static final String _sSavedFileName = "saved";
	private static final String _sPHPSESS_Cookie = "PHPSESSID";
	private static final String _sSafeModeLink = "http://9gag.com/pref/safe-browse?enable=";
	private static final String _sLogout = "http://9gag.com/logout";
	public static final String love_count_dot = "bull";
	
	private List<GagEntry> 	l_hot 		= new ArrayList<GagEntry>(),
							l_discover 	= new ArrayList<GagEntry>(),
							l_trending 	= new ArrayList<GagEntry>(),
							l_vote 		= new ArrayList<GagEntry>();
							
	private int point_hot = 0, point_discover = 0, point_trending = 0, point_vote = 0;
	private DefaultHttpClient httpclient;
	private LoadFirstEntriesFinishedListener loadFinished;
	private ProcessLoginFinishedListener lsPLFinished;
	private ProgressDialog progressDialog, progDlgLogin;
	private Context _context;
	private boolean m_isLogged = false, m_bSafeMode = true;
	private String m_sPHPSESSID;
	
	public List<GagEntry> getListHot(){return l_hot;}
	public List<GagEntry> getListdiscover(){return l_discover;}
	public List<GagEntry> getListTrending(){return l_trending;}
	public List<GagEntry> getListVote(){return l_vote;}
	
	public boolean Logged(){return m_isLogged;}
	public boolean getSafeMode(){return m_bSafeMode;}
	public String getPHPSESSID(){return m_sPHPSESSID;}
	public void setPHPSESSID(String str){m_sPHPSESSID = str;}
	public void setLogged(boolean logged){m_isLogged = logged;}
	public void setSafemode(boolean mode){m_bSafeMode = mode;}
	
	public void postSafeMode(boolean mode){
		try {
			if(m_isLogged && m_bSafeMode != mode){
				String value;
				if(mode)
					value = "1";
				else 
					value = "0";
				HttpGet get = getHttpGet(_sSafeModeLink + value);
				httpclient.execute(get);
				m_bSafeMode = mode;
				PublicResource.setSafeMode(_context, mode);
			}
		} catch (ClientProtocolException e) {
		} catch (IOException e) {
		}
	}
	
	public void Logout(){
		try{
			if(m_isLogged){
				HttpGet get = getHttpGet(_sLogout);
				httpclient.execute(get);
				httpclient.getCookieStore().clear();
				m_isLogged = false;
				m_bSafeMode = true;
				PublicResource.setSafeMode(_context, true);
			}
		}catch(Exception e){}
	}
	public List<GagEntry> getList(EntryType type){
		switch(type){
			case TRENDING:
				return l_trending;
			case DISCOVER:					
				return l_discover;
			case VOTE:
				return l_vote;
		}
		//by default
		return l_hot;
	}
	
	public void setLoadFirstEntriesFinished(LoadFirstEntriesFinishedListener finished){loadFinished = finished;}
	public void Reset(EntryType type){
		switch(type){
			case HOT:
				point_hot = 0;
				l_hot.clear();				
				break;
			case TRENDING:
				point_trending = 0;
				l_trending.clear();				
				break;
			case VOTE:
				point_vote = 0;
				l_vote.clear();
				break;
		}
	}
	
	public HttpClient getHttpClient(){return httpclient;}
	private void updateNewEntries(EntryType type) throws ClientProtocolException, IOException{
		HttpGet httpget;
		switch(type){
			case TRENDING:
				httpget = getHttpGet(_sImg + "trending&id=" 
									+ l_trending.get(l_trending.size() - 1).getID());
				break;
			case DISCOVER:
				httpget = getHttpGet(_sImg + "discover&id=" 
										+ l_discover.get(l_discover.size() - 1).getID());
				break;
			case VOTE:
				httpget = getHttpGet(_sImg + "vote&id=" 
						+ l_vote.get(l_vote.size() - 1).getID());
				break;
			default://HOT	
				httpget = getHttpGet(_sImg + "hot&id="
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
				
				if(type == EntryType.DISCOVER){
					l_discover.addAll(produceEntriesFromString(strb.toString(), type));
				}
				else if(type == EntryType.TRENDING){
					l_trending.addAll(produceEntriesFromString(strb.toString(), type));
				}
				else if(type == EntryType.VOTE){
					l_vote.addAll(produceEntriesFromString(strb.toString(), type));
				}else{
					l_hot.addAll(produceEntriesFromString(strb.toString(), type));
				}
				
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
		
		if((l_vote.size() != 0 && ((l_vote.size() - point_vote) <= _iUpdate)))
			try {
				updateNewEntries(type);
			} catch (Exception e) {}
		
		if(type == EntryType.TRENDING)			
			return l_trending.get(point_trending++);
		
		if(type == EntryType.DISCOVER)			
			return l_discover.get(point_discover++);
		
		if(type == EntryType.VOTE)			
			return l_vote.get(point_vote++);
		
		//for Default(hot)
		return l_hot.get(point_hot++);
	}
	public int getPointHot(){return point_hot;}
	public int getPointDiscover(){return point_discover;}
	public int getPointTrending(){return point_trending;}
	public int getPointVote(){return point_vote;}
	
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
		
			//Add get love_count
			strFind = "id=\"love_count_" + Integer.toString(id) + "\"";
			f1 = str.indexOf(strFind);
			f1 += strFind.length();
			f1 = str.indexOf(">", f1);
			f2 = str.indexOf("<", f1);
			String loveCount = str.substring(f1 + 1, f2);
			if(loveCount.contains(love_count_dot))
				loveCount = "0";
			list_entry.add(new GagEntry(httpclient, id, name, url, link, loveCount, type));
		}
		return list_entry;
	}
	private List<GagEntry> getFirstEntries(EntryType type) throws ClientProtocolException, IOException{
		HttpGet httpget;		
		switch (type){				
			case DISCOVER:
				httpget = getHttpGet(_sMainPage + "discover/");
				break;
			case TRENDING:
				httpget = getHttpGet(_sMainPage + "trending/");
				break;
			case VOTE:
				httpget = getHttpGet(_sMainPage + "vote/");
				break;
			default:
				httpget = getHttpGet(_sMainPage + "hot/");
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
	
	private enum DataEntry{
		ID,
		NAME,
		URL,
		IMGLINK,
		LOVECOUNT
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
			case VOTE:
				list = l_vote;
				break;
		}
		for(GagEntry entry : list){
			JSONObject js_item = new JSONObject();
			try {
				js_item.put(DataEntry.ID.toString(), entry.getID());
				js_item.put(DataEntry.NAME.toString(), entry.getEntryName());
				js_item.put(DataEntry.URL.toString(), entry.getEntryUrl());
				js_item.put(DataEntry.IMGLINK.toString(), entry.getLink());
				js_item.put(DataEntry.LOVECOUNT.toString(), entry.getLoveCount());
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
				json.put(EntryType.VOTE.toString(), getJSONString(EntryType.VOTE));
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
							js_item.getInt(DataEntry.ID.toString()),
							js_item.getString(DataEntry.NAME.toString()),
							js_item.getString(DataEntry.URL.toString()),
							js_item.getString(DataEntry.IMGLINK.toString()),
							js_item.getString(DataEntry.LOVECOUNT.toString()),
							EntryType.HOT
							));
				}
				//TRENDING
				arr = new JSONArray(json.getString(EntryType.TRENDING.toString()).toString());
				for(int i = 0; i < arr.length() - 1; i++){
					JSONObject js_item = new JSONObject(arr.getString(i));
					l_trending.add(new GagEntry(httpclient,
							js_item.getInt(DataEntry.ID.toString()),
							js_item.getString(DataEntry.NAME.toString()),
							js_item.getString(DataEntry.URL.toString()),
							js_item.getString(DataEntry.IMGLINK.toString()),
							js_item.getString(DataEntry.LOVECOUNT.toString()),
							EntryType.TRENDING
							));
				}
				//VOTE
				arr = new JSONArray(json.getString(EntryType.VOTE.toString()).toString());
				for(int i = 0; i < arr.length() - 1; i++){
					JSONObject js_item = new JSONObject(arr.getString(i));
					l_vote.add(new GagEntry(httpclient,
							js_item.getInt(DataEntry.ID.toString()),
							js_item.getString(DataEntry.NAME.toString()),
							js_item.getString(DataEntry.URL.toString()),
							js_item.getString(DataEntry.IMGLINK.toString()),
							js_item.getString(DataEntry.LOVECOUNT.toString()),
							EntryType.VOTE
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
	
	public HttpGet getHttpGet(String sLink){
		HttpGet get = new HttpGet(sLink);
		if(m_isLogged){
			//Set cookie
			int f1 = m_sPHPSESSID.indexOf("=");
	        int f2 = m_sPHPSESSID.length();
			Cookie newCookie = new BasicClientCookie(_sPHPSESS_Cookie, m_sPHPSESSID.substring(f1 + 1, f2));
			CookieStore store = new BasicCookieStore();
		    store.addCookie(newCookie);
		    httpclient.setCookieStore(store);
		    
			get.setHeader("Cookie", m_sPHPSESSID);
		}
		return get;
	}
	
	public void Login(String username, String password, ProcessLoginFinishedListener ls){
		progDlgLogin = ProgressDialog.show(_context, "", _context.getString(R.string.Logging));
		lsPLFinished = ls;
		ProcessLoginTask log = new ProcessLoginTask();
		log.execute(username, password);
	}
	private class ProcessLoginTask extends AsyncTask<String, Void, LoginReturnValue>{

		@Override
		protected LoginReturnValue doInBackground(String... params) {
			LoginReturnValue re = new LoginReturnValue();
			try {
				//Get csrftoken
				final String token = "crsftoken";
				HttpGet get = new HttpGet(_sLogin);
				String sLoginPage = Utilities.ReadInputHTTP(httpclient.execute(get));
				int f1 = sLoginPage.indexOf(token);
				f1 = sLoginPage.indexOf("value=\"", f1);
				f1 += 7;
				int f2 = sLoginPage.indexOf("\"", f1);
				String sToken = sLoginPage.substring(f1, f2);
				
				//Login
				 URL url = new URL(_sLogin);
				 HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
				 urlc.setRequestMethod("POST");
				 urlc.setDoOutput(true);
				 urlc.setDoInput(true);
				 urlc.setUseCaches(false);
				 urlc.setAllowUserInteraction(false);
				 
				 int i = 0;
				 while(!httpclient.getCookieStore().getCookies().get(i)
						 .getName().contains(_sPHPSESS_Cookie))
					 i++;
				 Cookie prvCookie = httpclient.getCookieStore().getCookies().get(i);
				 urlc.setRequestProperty("Cookie", prvCookie.getName() + "=" + prvCookie.getValue());
				 urlc.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
				 String output = "csrftoken="+ URLEncoder.encode(sToken, HTTP.UTF_8)
		                +"&username="+ URLEncoder.encode(params[0], HTTP.UTF_8)
		                +"&password="+ URLEncoder.encode(params[1], HTTP.UTF_8);
				 DataOutputStream dataout = new DataOutputStream(urlc.getOutputStream());
				 dataout.writeBytes(output);
				 
				if(urlc.getResponseCode() == 302){
					re.Success = true;
					String sCookie = urlc.getHeaderField(8);//PHPSESSID in 8th header
					f1 = sCookie.indexOf("=");
			        f2 = sCookie.indexOf(";", f1);
					re.PHPSESSID = _sPHPSESS_Cookie + "=" + sCookie.substring(f1 + 1, f2);
					//Set cookie
					Cookie newCookie = new BasicClientCookie(_sPHPSESS_Cookie, sCookie.substring(f1 + 1, f2));
					CookieStore store = new BasicCookieStore();
				    store.addCookie(newCookie);
				    httpclient.setCookieStore(store);
					//SafeMode is on or off
					get = new HttpGet(_sMainPage);
					get.setHeader("Cookie", re.PHPSESSID);
					String sMainPage = Utilities.ReadInputHTTP(httpclient.execute(get));
					if(sMainPage.contains(_sCheckSafeModeIsOff))
						re.SafeMode = false;
				}
				else //200
					re.Success = false;
			} catch (Exception e) {
				System.out.println("d");
			}
			
			return re;
		}

		@Override
		protected void onPostExecute(LoginReturnValue result) {
			m_isLogged = result.Success;
			m_bSafeMode = result.SafeMode;
			PublicResource.setSafeMode(_context, m_bSafeMode);
			m_sPHPSESSID = result.PHPSESSID;
			progDlgLogin.dismiss();
			if(lsPLFinished != null)
				lsPLFinished.OnProcessLoginFinished(m_isLogged, m_bSafeMode);
		}
	}
	public interface ProcessLoginFinishedListener{
		public void OnProcessLoginFinished(boolean Success, boolean SafeMode);
	}
	private class LoginReturnValue{
		public LoginReturnValue(){}
		public boolean Success = false;
		public boolean SafeMode = true;
		public String PHPSESSID;
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
			if(result.size() != 0){
				EntryType type = result.get(0).getEntryType();
				if(type == EntryType.DISCOVER)
					l_discover.addAll(result);
				else if (type == EntryType.TRENDING)
					l_trending.addAll(result);
				else if (type == EntryType.VOTE)
					l_vote.addAll(result);
				else
					l_hot.addAll(result);
				if(loadFinished != null)
					loadFinished.OnLoadFirstEntriesFinished();
				
				//Close Progress Dialog
				progressDialog.dismiss();
			}else{
				PublicResource.WrongExit = true;
				PublicResource.ResetAllVariable(_context);
				_context.deleteFile(_sSavedFileName);
				AlertDialog.Builder builder = new AlertDialog.Builder(_context);
	        	builder.setMessage(R.string.DidNotExitRightWay)
	        			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {						
							public void onClick(DialogInterface dialog, int which) {
								((Activity)_context).finish();		
							}
						});
	        	AlertDialog alert = builder.create();
	        	alert.show();
			}
		}	
		
		
	}
	
	public interface LoadFirstEntriesFinishedListener{
		public void OnLoadFirstEntriesFinished();
	}
}
