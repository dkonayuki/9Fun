package net.knvz.android_fortest;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;


public class Android_fortestActivity extends Activity {
		
	private EntryImgView entryImg;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        entryImg = new EntryImgView(this, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test));
        setContentView(entryImg);
        entryImg.requestFocus();

    }    
   
}