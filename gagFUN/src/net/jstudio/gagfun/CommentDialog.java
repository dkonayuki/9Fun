package net.jstudio.gagfun;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;


public class CommentDialog extends Dialog {
	static final float Dialog_Width_DP_Portrait = 270f;
	static final float Dialog_Height_DP_Portrait = 396f;
	static final float Dialog_Width_DP_Landscape = 420f;
	static final float Dialog_Height_DP_Landscape = 246f;
	static final int WebView_Transparent = 210;
	
	private String m_URL;
	private int m_px = 0, m_py = 0;
	private WebView m_cmtView;
	
	public CommentDialog(Context context, String url) {
		super(context);		
		m_URL = url;
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		//Set properties
		setCanceledOnTouchOutside(true);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(0));
		WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
		
		//Get orientation and suitable Width, Height of Dialog
		float density = getContext().getResources().getDisplayMetrics().density;
		int orientation = wm.getDefaultDisplay().getRotation();
		int DWidth = 0, DHeight = 0;
		
		if(orientation == Surface.ROTATION_0){//Portrait
			setContentView(R.layout.cmt_dialog_portrait);
			DWidth = (int)(Dialog_Width_DP_Portrait*density + 0.5f);
			DHeight = (int)(Dialog_Height_DP_Portrait*density + 0.5f);
		}else{//Landscape
			setContentView(R.layout.cmt_dialog_landscape);
			DWidth = (int)(Dialog_Width_DP_Landscape*density + 0.5f);
			DHeight = (int)(Dialog_Height_DP_Landscape*density + 0.5f);
		}
		
		//Set position
		if(m_px != 0 && m_py != 0){						
			DisplayMetrics dm = new DisplayMetrics();
			wm.getDefaultDisplay().getMetrics(dm);
			int bottomright_X = dm.widthPixels/2 + DWidth/2;
			int bottomright_Y = dm.heightPixels/2 + DHeight/2;
			
			WindowManager.LayoutParams wmlp = getWindow().getAttributes();
			wmlp.x = m_px - bottomright_X; 
			wmlp.y = m_py - bottomright_Y;			
		}		
		//WebView
		setUpWebView();
		m_cmtView.loadUrl(m_URL);
	}
	
	private void setUpWebView(){
		m_cmtView = (WebView)this.findViewById(R.id.cmt_view);
		m_cmtView.getSettings().setJavaScriptEnabled(true);
		m_cmtView.setVerticalScrollBarEnabled(true);
		m_cmtView.setHorizontalScrollBarEnabled(false);
		m_cmtView.setBackgroundColor(Color.argb(WebView_Transparent, 255, 255, 255));
		m_cmtView.setWebViewClient(new WebViewClient(){

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}			
		});
	}
	public void setPosition(int x, int y){m_px = x; m_py = y;}
	
	
}
