package net.jstudio.gagfun;


import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.NineGAG;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class GagFUN extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV_hot = null,
						rbV_trending = null;
	private NineGAG _nineGag;	
	private FrameLayout layout;
	private ImageButton btt_next,btt_previous;

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

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	    setContentView(R.layout.main);
	    layout = (FrameLayout)findViewById(R.id.frame); 
	    layout.setBackgroundColor(Color.BLACK);
	    onCreateOnScreenButton();
		PublicResource.LoadResource(this);
        _nineGag = new NineGAG(this);        
        if(!_nineGag.LoadDataFromFile()){//IF fail to load data, reset all variable to 0
        	PublicResource.setPrefCurrentView(this, EntryType.HOT, 0);
        	PublicResource.setPrefCurrentView(this, EntryType.TRENDING, 0);
        }
        switch(PublicResource.getPrefCurrentPage(this)){
        	case HOT:        		
        		rbV_hot = new RibbonView(this, EntryType.HOT, _nineGag);        		
                setView(rbV_hot);                
        		break;
        	case TRENDING:        		
        		rbV_trending = new RibbonView(this, EntryType.TRENDING, _nineGag);        		
        		setView(rbV_trending);
        		break;
        		
        }    
	 
    }
    public void onCreateOnScreenButton(){
    	//button next
    	btt_next = (ImageButton)findViewById(R.id.btt_next);
		btt_next.setBackgroundColor(Color.TRANSPARENT);
		btt_next.setAlpha(60);
		
		//button previous
		btt_previous = (ImageButton)findViewById(R.id.btt_pre);
		btt_previous.setBackgroundColor(Color.TRANSPARENT);
		btt_previous.setAlpha(60);
    }
    
    
    private void setOnClickButton(final RibbonView rbV ){
    	btt_next.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				rbV.goNext();
			}
		});
		btt_next.startAnimation(rbV.anim_FadeIn);
		btt_previous.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				rbV.goPrevious();
			}
			
		});
		btt_previous.startAnimation(rbV.anim_FadeIn);
    }
    
    private void setView(RibbonView rbV){
    	//if(layout.indexOfChild(rbV)!=-1) layout.removeView(rbV);
    	if(layout.getChildCount()==3) layout.removeViewAt(0);
    	layout.addView(rbV);
        btt_next.bringToFront();
        btt_previous.bringToFront();
        setOnClickButton(rbV);
    	
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
		//Check menu
		MenuItem item;
		switch(PublicResource.getPrefCurrentPage(this)){
			case HOT:
				item = (MenuItem)menu.findItem(R.id.mnu_hot);
				item.setChecked(true);
				break;
			case TRENDING:
				item = (MenuItem)menu.findItem(R.id.mnu_trending);
				item.setChecked(true);
				break;
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
			switch(item.getItemId()){
				case R.id.mnu_hot:
					if(!item.isChecked()){
						item.setChecked(true);
							if(rbV_hot == null)
						rbV_hot = new RibbonView(this, EntryType.HOT, _nineGag);					
					PublicResource.setPrefCurrentPage(this, EntryType.HOT);
					setView(rbV_hot);
					
				}return true;
				case R.id.mnu_trending:
					if(!item.isChecked()){
						item.setChecked(true);
					if(rbV_trending == null)
						rbV_trending = new RibbonView(this, EntryType.TRENDING, _nineGag);
						PublicResource.setPrefCurrentPage(this, EntryType.TRENDING);
					setView(rbV_trending);
					
				}return true;
				case R.id.mnu_setting:{
					startActivity(new Intent("net.jstudio.Preference"));
				}return true;
				case R.id.mnu_login:{
					AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        	builder.setMessage(R.string.NextVersion)
		        			.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {						
								public void onClick(DialogInterface dialog, int which) {						
								}
							});
		        	AlertDialog alert = builder.create();
		        	alert.show();
		        	return true;
				}
			}			
				
		return super.onOptionsItemSelected(item);
	}
    

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		if(_nineGag != null)
			_nineGag.SaveDataToStorage();
		if(rbV_hot != null){
			PublicResource.setPrefCurrentView(this, EntryType.HOT, rbV_hot.getDisplayedChild());
			rbV_hot.DisposeAllDialog();
		}
		if(rbV_trending != null){
			PublicResource.setPrefCurrentView(this, EntryType.TRENDING, rbV_trending.getDisplayedChild());
			rbV_trending.DisposeAllDialog();
		}
	}


	@Override
	protected void onResume() {
		//Check internet is available
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
        }
		super.onResume();
		//Fix android bugs
		setUpOnScreenButtonMode();
		Handler hl = new Handler();
		hl.postDelayed(new Runnable(){

			public void run() {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			
		}, 500);
		
		
	}
    
	private void setUpOnScreenButtonMode(){
		if(btt_previous != null && btt_next != null){
			if(PublicResource.getPrefTouchMode(this)){
				btt_previous.setVisibility(View.VISIBLE);
				btt_next.setVisibility(View.VISIBLE);	
				btt_next.bringToFront();
				btt_previous.bringToFront();
			}else{
				btt_next.setVisibility(View.INVISIBLE);
				btt_previous.setVisibility(View.INVISIBLE);
			}
		}
	}
}