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
import android.widget.Toast;

public class GagFUN extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV_hot = null,
						rbV_trending = null,
						rbV_vote = null;
	
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
        	PublicResource.setPrefCurrentView(this, EntryType.VOTE, 0);
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
        	case VOTE:        		
        		rbV_vote = new RibbonView(this, EntryType.VOTE, _nineGag);        		
        		setView(rbV_vote);
        		break;
        }    
        //Load previous Logged information
        _nineGag.setPHPSESSID(PublicResource.getPHPSESSID(this));
        _nineGag.setExpires(PublicResource.getExpires(this));
        _nineGag.setLogged(PublicResource.getLogged(this));
        _nineGag.setSafemode(PublicResource.getSafeMode(this));
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
				if (rbV.indexOfChild(rbV.getCurrentView())==0) 
					Toast.makeText(getBaseContext(), R.string.no_img, Toast.LENGTH_SHORT).show();
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
    	if(rbV.NeedToRefresh)
    		rbV.Reset();
    }
    
    public RibbonView getCurrentRibbon(){
    	if(layout.indexOfChild(rbV_hot) != -1)
    		return rbV_hot;
    	if(layout.indexOfChild(rbV_trending) != -1)
    		return rbV_trending;
    	if(layout.indexOfChild(rbV_vote) != -1)
    		return rbV_vote;
    	return null;
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
	public boolean onPrepareOptionsMenu(Menu menu) {
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
			case VOTE:
				item = (MenuItem)menu.findItem(R.id.mnu_vote);
				item.setChecked(true);
				break;
		}
		
		//Change Login Icon
		item = (MenuItem)menu.findItem(R.id.mnu_login);
		if(_nineGag.Logged()){
			item.setIcon(R.drawable.ic_logout9gag);
			item.setTitle(R.string.mnuLogout);
		}else{
			item.setIcon(R.drawable.ic_login9gag);
			item.setTitle(R.string.mnuLogin);
		}
		//Set Vote Menu
		//item = (MenuItem)menu.findItem(R.id.mnu_vote);
		//item.setEnabled(_nineGag.Logged());
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
					}
					return true;
				case R.id.mnu_trending:
					if(!item.isChecked()){
						item.setChecked(true);
						if(rbV_trending == null)
							rbV_trending = new RibbonView(this, EntryType.TRENDING, _nineGag);
						PublicResource.setPrefCurrentPage(this, EntryType.TRENDING);
						setView(rbV_trending);
					}
					return true;
				case R.id.mnu_vote:
					if(_nineGag.Logged()){
						if(!item.isChecked()){
							item.setChecked(true);
							if(rbV_vote == null)
								rbV_vote = new RibbonView(this, EntryType.VOTE, _nineGag);
							PublicResource.setPrefCurrentPage(this, EntryType.VOTE);
							setView(rbV_vote);
						}
					}else{
						Toast t = Toast.makeText(GagFUN.this, R.string.LoginToUseVotePage, Toast.LENGTH_SHORT);
						t.show();
					}
				return true;
				case R.id.mnu_setting:{
					startActivity(new Intent("net.jstudio.Preference"));
				}return true;
				case R.id.mnu_login:{
					if(_nineGag.Logged()){
						_nineGag.Logout();
						PublicResource.setLogged(GagFUN.this, false);
						Toast t = Toast.makeText(GagFUN.this, R.string.Logout, Toast.LENGTH_SHORT);
						t.show();
						if(getCurrentRibbon() == rbV_vote){
							PublicResource.setPrefCurrentPage(this, EntryType.HOT);
							if(rbV_hot == null)
								rbV_hot = new RibbonView(this, EntryType.HOT, _nineGag);	
							setView(rbV_hot);
							Toast.makeText(GagFUN.this, R.string.ReturnToHotPage, Toast.LENGTH_SHORT).show();
						}
					}else
						startActivityForResult(new Intent("net.jstudio.Login"), PublicResource.Activity.Login.ordinal());
		        	return true;
				}
			}			
				
		return super.onOptionsItemSelected(item);
	}
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == PublicResource.Activity.Login.ordinal()) {
			if (resultCode == RESULT_OK) {
				//Toast.makeText(getBaseContext(), data.getStringExtra("username"), Toast.LENGTH_SHORT).show();
				_nineGag.Login(data.getStringExtra(LoginActivity.Result.Username.toString()),
								data.getStringExtra(LoginActivity.Result.Password.toString()), 
								new NineGAG.ProcessLoginFinishedListener() {
					
					public void OnProcessLoginFinished(boolean Success, boolean SafeMode) {
						if(Success){
							setAllRibbonNeedToRefresh();
							GagFUN.this.getCurrentRibbon().Reset();
							Toast t = Toast.makeText(GagFUN.this, R.string.Logged, Toast.LENGTH_SHORT);
							t.show();
							PublicResource.setLogged(GagFUN.this, true);
						}else{
							AlertDialog.Builder builder = new AlertDialog.Builder(GagFUN.this);
							builder.setMessage(R.string.UsernamePasswordIncorrect)
									.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
										
										public void onClick(DialogInterface dialog, int which) {
											GagFUN.this.startActivityForResult(new Intent("net.jstudio.Login"), 
													PublicResource.Activity.Login.ordinal());
										}
									});
							AlertDialog alert = builder.create();
							alert.show();
						}
					}
				});
			}
		}
	}

	@Override
	protected void onDestroy() {		
		super.onDestroy();
		if(!PublicResource.WrongExit){
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
			if(rbV_vote != null){
				PublicResource.setPrefCurrentView(this, EntryType.VOTE, rbV_vote.getDisplayedChild());
				rbV_vote.DisposeAllDialog();
			}
			//Save Logged information
			PublicResource.setPHPSESSID(this, _nineGag.getPHPSESSID());
			PublicResource.setExpires(this, _nineGag.getExpires());
			PublicResource.setLogged(this, _nineGag.Logged());
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
		
		//Fix android bugs
		Handler hl = new Handler();
		hl.postDelayed(new Runnable(){

			public void run() {
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
			}
			
		}, 500);
		
		setUpOnScreenButtonMode();
		setUpSafemode();
		
		super.onResume();
	}
    
	private void setUpSafemode() {
		if(_nineGag.Logged()){
			boolean mode = PublicResource.getSafeMode(this);
			if(mode != _nineGag.getSafeMode()){
				_nineGag.postSafeMode(mode);
				setAllRibbonNeedToRefresh();
				getCurrentRibbon().Reset();
			}
		}
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
	
	private void setAllRibbonNeedToRefresh(){
		if(rbV_hot != null)
			rbV_hot.NeedToRefresh = true;
		if(rbV_trending != null)
			rbV_trending.NeedToRefresh = true;
		if(rbV_vote != null)
			rbV_vote.NeedToRefresh = true;
	}
}