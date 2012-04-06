package net.jstudio.gagfun;

import java.io.IOException;

import net.jstudio.gagCore.EntryType;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.preference.PreferenceManager;

public class PublicResource {
	private static final String sLoadingFileName = "loading.gif";
	private static final String sLoadingDown = "Down.gif";
	private static final String sNSFW = "nsfw.jpg";
	private static Movie m_LoadingMovie = null;
	private static Movie m_Down = null;
	private static Bitmap m_NSFW = null;
	
	
	public static Bitmap NSFWBitmap(){return m_NSFW;}
	public static Movie LoadingMovie(){return m_LoadingMovie;}
	public static Movie LoadingDown(){return m_Down;}
	public static boolean WrongExit = false;
	
	public enum Activity{
		Login
	}
	
	public static void LoadResource(Context ct){
		//Load "loading movie"
		try {
			m_LoadingMovie = Movie.decodeStream(ct.getAssets().open(sLoadingFileName));
			m_Down = Movie.decodeStream(ct.getAssets().open(sLoadingDown));
		} catch (IOException e) {}
		//Load NSFW
		AssetManager amng = ct.getAssets();
		try {
			m_NSFW = BitmapFactory.decodeStream(amng.open(sNSFW));
		} catch (IOException e) {
		}
	}
	
	private static final String sPrefName = "PrefName";
	private static final String sCurrent_HOT = "CurrentHOT";
	private static final String sCurrent_TRENDING = "CurrentTRENDING";
	private static final String sCurrent_VOTE = "CurrentVOTE";
	private static final String sCurrent_Page = "CurrentPage";
	private static final String sPHPSESSID = "PHPSESSID";
	private static final String sLogged = "Logged";
	private static final String sTouchMode = "touchMode";
	public static final String sSafeMode = "safeMode";
	
	public static EntryType getPrefCurrentPage(Context ct){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		int i = pref.getInt(sCurrent_Page, 0);
		switch (i){			
			case 1:
				return EntryType.TRENDING;
			case 2:
				return EntryType.VOTE;
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
			case VOTE:
				editor.putInt(sCurrent_Page, 2);
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
			case VOTE:
				return pref.getInt(sCurrent_VOTE, -1);
		}
		return -1;
	}
	
	public static String getPHPSESSID(Context ct){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		return pref.getString(sPHPSESSID, "");
	}
	
	public static boolean getLogged(Context ct){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		return pref.getBoolean(sLogged, false);
	}
	
	public static void setPHPSESSID(Context ct, String str){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putString(sPHPSESSID, str);
		editor.commit();
	}
	
	public static void setLogged(Context ct, boolean value){
		SharedPreferences pref = ct.getSharedPreferences(sPrefName, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(sLogged, value);
		editor.commit();
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
			case VOTE:
				editor.putInt(sCurrent_VOTE, value);
				break;
		}		
		editor.commit();
	}

	public static boolean getPrefTouchMode(Context ct){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ct);
		return pref.getBoolean(sTouchMode, false);
	}
	
	public static boolean getSafeMode(Context ct){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ct);
		return pref.getBoolean(sSafeMode, true);
	}
	
	public static void setSafeMode(Context ct, boolean value){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(ct);
		SharedPreferences.Editor editor = pref.edit();
		editor.putBoolean(sSafeMode, value);
		editor.commit();
	}
	
	public static void ResetAllVariable(Context ct){
		setLogged(ct, false);
		setSafeMode(ct, true);
		setPrefCurrentPage(ct, EntryType.HOT);
		setPrefCurrentView(ct, EntryType.HOT, 0);
		setPrefCurrentView(ct, EntryType.DISCOVER, 0);
		setPrefCurrentView(ct, EntryType.TRENDING, 0);
		setPrefCurrentView(ct, EntryType.VOTE, 0);
		setPHPSESSID(ct, "");
	}
}
