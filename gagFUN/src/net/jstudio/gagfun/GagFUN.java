package net.jstudio.gagfun;

import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.NineGAG;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class GagFUN extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV_hot,
						rbV_trending;
	private NineGAG _nineGag;
	private boolean bFirstTrending = true;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check if Internet is available on the mobile
        if(!CheckIfInternetIsAvailable()){
        	AlertDialog.Builder builder = new AlertDialog.Builder(this);
        	builder.setMessage(R.string.InternetIsNotAvailable)
        			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {						
						public void onClick(DialogInterface dialog, int which) {
							GagFUN.this.finish();							
						}
					});
        	AlertDialog alert = builder.create();
        	alert.show();
        	return;
        }        
        PublicResource.LoadResource(this);
      _nineGag = new NineGAG(this);
        rbV_hot = new RibbonView(this, EntryType.HOT, _nineGag); 
       
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	   setContentView(rbV_hot);
	   
	 
    }
    
    private boolean CheckIfInternetIsAvailable(){
    	ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	if(cm != null){
    		NetworkInfo[] netInfo = cm.getAllNetworkInfo();
    		for(NetworkInfo ni : netInfo){
    			if(((ni.getTypeName().equalsIgnoreCase("WIFI"))
    					|| (ni.getTypeName().equalsIgnoreCase("MOBILE")))
    					&& ni.isConnected() && ni.isAvailable())
    				return true;
    		}
    	}
    	return false;
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.mnu_main, menu);		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(!item.isChecked()){
			item.setChecked(true);
			switch(item.getItemId()){
				case R.id.mnu_hot:{
					setContentView(rbV_hot);
					return true;
				}
				case R.id.mnu_trending:{
					if(bFirstTrending){
						rbV_trending = new RibbonView(this, EntryType.TRENDING, _nineGag);
						bFirstTrending = false;
					}
					setContentView(rbV_trending);
					return true;
				}
			}			
		}		
		return super.onOptionsItemSelected(item);
	}
    
    
}