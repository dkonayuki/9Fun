package net.jstudio.gagCore;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;


public class NineGAG {
	//Some constants
	private final String _sMainPage = "http://9gag.com/";
	private final String _sImg = "http://9gag.com/new/json?list=";
	private final int _iUpdate = 10;
		
	
	private List<GagEntry> 	l_hot 		= new ArrayList<GagEntry>(),
							l_trending 	= new ArrayList<GagEntry>(),
							l_vote 		= new ArrayList<GagEntry>();
	private int point_hot = 0, point_trending = 0;
	private HttpClient httpclient;
	
	public List<GagEntry> getListHot(){return l_hot;}
	public List<GagEntry> getListTrending(){return l_trending;}
	
	private void updateNewEntries(EntryType type) throws ClientProtocolException, IOException{
		HttpGet httpget;
		switch(type){
			case TRENDING:
				httpget = new HttpGet(_sImg + "hot&id=" 
										+ l_trending.get(l_trending.size() - 1).getID());
			break;
			default://HOT	
				httpget = new HttpGet(_sImg + "trending&id="
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
			System.out.println(str);
			try {
				
				JSONObject json = new JSONObject(str.toString());	
				JSONArray arr = json.getJSONArray("ids");
				JSONObject json_items = json.getJSONObject("items");
				
				StringBuilder strb = new StringBuilder();
				
				for(int j = 0; j < json_items.length(); j++)					
					strb.append(json_items.getString("entry-" + arr.getInt(j)));

				produceEntriesFromString(strb.toString(), type);
				
			} catch (ParseException e) {				
				e.printStackTrace();
			}
			
		}
	}
	//This function must be called when touching for the next entry
	//Because it is used for update new entries
	public GagEntry Next(EntryType type){
		if((l_hot.size() - point_hot) <= _iUpdate || (l_trending.size() - point_trending) <= _iUpdate )
			try {
				updateNewEntries(type);
			} catch (Exception e) {}
		
		if(type == EntryType.TRENDING)			
			return l_trending.get(point_trending++);
		
		//for Default(hot)
		return l_hot.get(point_hot++);
	}
	
	private void produceEntriesFromString(String str, EntryType type){
		Pattern par_gagid = Pattern.compile("gagid=\"[0-9]*\"", Pattern.CASE_INSENSITIVE);
		Pattern par_datatext = Pattern.compile("data-text=\"[^\"]*\"", Pattern.CASE_INSENSITIVE);
		Pattern par_dataurl = Pattern.compile("data-url=\"[^\"]*\"", Pattern.CASE_INSENSITIVE);;
		
		Matcher match_id = par_gagid.matcher(str);
		Matcher match_text = par_datatext.matcher(str);
		Matcher match_dataurl = par_dataurl.matcher(str);
		while(match_id.find() && match_text.find() && match_dataurl.find()){
			
			int id = Integer.parseInt(match_id.group().replaceFirst("(?i)gagid=\"", "")
					.replaceFirst("\"", ""));
			
			String name = match_text.group().replaceFirst("(?i)data-text=\"", "")
					.replaceFirst("\"", "");				
			
			String url = match_dataurl.group().replaceFirst("(?i)data-url=\"", "")
					.replaceFirst("\"", "");
			
			if(type == EntryType.HOT)
				l_hot.add(new GagEntry(id, name, url, EntryType.HOT));
			else
				l_trending.add(new GagEntry(id, name, url, EntryType.TRENDING));
		}
	}
	private void getFirstEntries(EntryType type) throws ClientProtocolException, IOException{
		HttpGet httpget;		
		switch (type){				
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
			produceEntriesFromString(str_m, type);
		}
	}
	
	public NineGAG(){
		httpclient = new DefaultHttpClient();
		try{
			getFirstEntries(EntryType.HOT);
			getFirstEntries(EntryType.TRENDING);
		}catch(Exception e){
			System.out.println("FUCK");
		}
		
	}

}
