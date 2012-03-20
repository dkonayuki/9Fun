package net.jstudio.gagfun;


import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class GagFUNActivity extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rbV = new RibbonView(this);    
  
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(rbV);
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
    		Toast.makeText(this, "You clicked on Item 1", Toast.LENGTH_LONG).show();
    		return true;
    	case 1:
    		Toast.makeText(this, "You clicked on Item 2", Toast.LENGTH_LONG).show();
    		return true;
    	}
    	return false;
    }
    
}