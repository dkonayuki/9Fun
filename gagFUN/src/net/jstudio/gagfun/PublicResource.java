package net.jstudio.gagfun;

import java.io.IOException;

import android.content.Context;
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
}
