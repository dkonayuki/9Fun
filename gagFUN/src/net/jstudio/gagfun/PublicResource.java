package net.jstudio.gagfun;

import java.io.IOException;

import net.jstudio.gagCore.EntryType;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Movie;

public class PublicResource {
	private static final String sLoadingFileName = "loading.gif";
	private static final String sLoadingDown = "Down.gif";
	private static Movie m_LoadingMovie = null;
	private static Movie m_Down = null;
	
	public static Movie LoadingMovie(){return m_LoadingMovie;}
	public static Movie LoadingDown(){return m_Down;}
	public static void LoadResource(Context ct){
		//Load "loading movie"
		try {
			m_LoadingMovie = Movie.decodeStream(ct.getAssets().open(sLoadingFileName));
			m_Down = Movie.decodeStream(ct.getAssets().open(sLoadingDown));
		} catch (IOException e) {}
	}
	
	private static final String sPrefName = "PrefName";
	private static final String sCurrent_HOT = "CurrentHOT";
	private static final String sCurrent_TRENDING = "CurrentTRENDING";
	private static final String sCurrent_Page = "CurrentPage";	
	public static EntryType getPrefCurrentPage(Context ct){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		int i = pref.getInt(sCurrent_Page, 0);
		switch (i){			
			case 1:
				return EntryType.TRENDING;
		}
		return EntryType.HOT;
	}
	public static void setPrefCurrentPage(Context ct, EntryType type){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		switch(type){
			case HOT:
				editor.putInt(sCurrent_Page, 0);				
				break;
			case TRENDING:
				editor.putInt(sCurrent_Page, 1);
				break;
		}
		editor.commit();
	}
	public static int getPrefCurrentView(Context ct, EntryType type){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		switch(type){
			case HOT:
				return pref.getInt(sCurrent_HOT, -1);
			case TRENDING:
				return pref.getInt(sCurrent_TRENDING, -1);
		}
		return -1;
	}
	
	public static void setPrefCurrentView(Context ct, EntryType type, int value){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		switch(type){
			case HOT:
				editor.putInt(sCurrent_HOT, value);
				break;
			case TRENDING:
				editor.putInt(sCurrent_TRENDING, value);
				break;
		}		
		editor.commit();
	}
}
