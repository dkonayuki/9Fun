package net.jstudio.gagCore;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;

public  class Utilities {
	private static final Map<String, String> mEntities = new HashMap<String, String>();
	private static boolean bMapLoaded = false;
	
	public static String ConvertHTMLEntities(String str){
		if(!bMapLoaded){
			bMapLoaded = true;
			mEntities.put("&#034;", "\"");
			mEntities.put("&#038;", "&");
			mEntities.put("&#039;", "'");
			mEntities.put("&#060;", "<");
			mEntities.put("&#062;", ">");	
			mEntities.put("&quot;","\"");
		}		
		@SuppressWarnings("rawtypes")
		Iterator i = mEntities.entrySet().iterator();
		while(i.hasNext()){
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> entry = (Map.Entry<String, String>)i.next();
			str = str.replace(entry.getKey(), entry.getValue());
		}
		return str;		
	}
	
	public static String ReadInputHTTP(HttpResponse response){
		String str_m = null;
		try {
			HttpEntity entity = response.getEntity();
			InputStream in = entity.getContent();
			StringBuilder str = new StringBuilder();
			int i;
			while((i = in.read()) != -1)
				str.append((char)i);
			str_m = str.toString();
			
		} catch (IllegalStateException e) {
		} catch (IOException e) {
		}
		return str_m;
	}
}
