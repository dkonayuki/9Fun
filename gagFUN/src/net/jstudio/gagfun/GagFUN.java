package net.jstudio.gagfun;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class GagFUN extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV;
	private final String[] pages = {"Hot Page", "Trending Page"};
	int type=0;
	
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
        Bundle extras=getIntent().getExtras();
        if (extras!=null){
        	type=extras.getInt("page");
        }
        rbV = new RibbonView(this,type);    
  
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(rbV);
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
    
    private void CreateMenu(Menu menu){
    	MenuItem mnu1 = menu.add(0, 0, 0, "Choose Page");
    	{
    		mnu1.setAlphabeticShortcut('P');
    		mnu1.setIcon(R.drawable.ic_menu_moreoverflow);
    	}
    	MenuItem mnu2 = menu.add(0, 1, 1, "Share");
    	{
    		mnu2.setAlphabeticShortcut('S');
    		mnu2.setIcon(R.drawable.ic_menu_allfriends);
    	}
    	
    }
    public boolean onCreateOptionsMenu(Menu menu){
    	super.onCreateOptionsMenu(menu);
    	CreateMenu(menu);
    	return true;
    }
    public boolean onOptionsItemSelected(MenuItem item){
    	return MenuChoice(item);
    }
    
    private boolean MenuChoice(MenuItem item){
    	switch(item.getItemId()){
    	case 0:
    		showDialog(0);
    		return true;
    	case 1:
    		Toast.makeText(this, "You clicked on Item 2", Toast.LENGTH_LONG).show();
    		return true;
    	}
    	return false;
    }
    
    
    protected Dialog onCreateDialog(int id){
    	switch(id){
    	case 0:
    		return new AlertDialog.Builder(this)
    		.setItems(pages, new DialogInterface.OnClickListener() {
    		    public void onClick(DialogInterface dialog, int item) {
    		        type=item;
    		        Intent intent = getIntent();
    		        Bundle extras = new Bundle();
    		        extras.putInt("page", type);
    		        intent.putExtras(extras);
    		        finish();
    		        startActivity(intent);
    		    }
    		})
			.create();
    	}
    	return null;
    }
    
}