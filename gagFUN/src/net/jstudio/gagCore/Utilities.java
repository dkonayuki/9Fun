package net.jstudio.gagCore;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
}
