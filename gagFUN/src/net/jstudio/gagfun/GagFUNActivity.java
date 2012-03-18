package net.jstudio.gagfun;

import android.app.Activity;
import android.os.Bundle;

public class GagFUNActivity extends Activity {
    /** Called when the activity is first created. */
	private RibbonView rbV;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rbV = new RibbonView(this);        
        setContentView(rbV);
    }
}