package net.jstudio.gagfun;

import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.NineGAG;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

public class GagFUN extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV_hot = null,
						rbV_trending = null;
	private NineGAG _nineGag;	
	
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
      
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
	     
		PublicResource.LoadResource(this);
        _nineGag = new NineGAG(this);        
        if(!_nineGag.LoadDataFromFile()){//IF fail to load data, reset all variable to 0
        	PublicResource.setPrefCurrentView(this, EntryType.HOT, 0);
        	PublicResource.setPrefCurrentView(this, EntryType.TRENDING, 0);
        }
        switch(PublicResource.getPrefCurrentPage(this)){
        	case HOT:        		
        		rbV_hot = new RibbonView(this, EntryType.HOT, _nineGag);        		
                setContentView(rbV_hot);                
        		break;
        	case TRENDING:        		
        		rbV_trending = new RibbonView(this, EntryType.TRENDING, _nineGag);        		
        		setContentView(rbV_trending);
        		break;
        		
        }    
	 
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
					setContentView(rbV_hot);
					
				}return true;
				case R.id.mnu_trending:
					if(!item.isChecked()){
						item.setChecked(true);
					if(rbV_trending == null)
						rbV_trending = new RibbonView(this, EntryType.TRENDING, _nineGag);
						PublicResource.setPrefCurrentPage(this, EntryType.TRENDING);
					setContentView(rbV_trending);
					
				}return true;
				case R.id.mnu_setting:{
					startActivity(new Intent("net.jstudio.Preference"));
				}return true;
				case R.id.mnu_login:{
					return true;
				}
			}			
				
		return super.onOptionsItemSelected(item);
	}
    
	@Override
	protected void onDestroy() {		
		super.onDestroy();
		_nineGag.SaveDataToStorage();
		if(rbV_hot != null)
			PublicResource.setPrefCurrentView(this, EntryType.HOT, rbV_hot.getDisplayedChild());
		if(rbV_trending != null)
			PublicResource.setPrefCurrentView(this, EntryType.TRENDING, rbV_trending.getDisplayedChild());
	}
    
}