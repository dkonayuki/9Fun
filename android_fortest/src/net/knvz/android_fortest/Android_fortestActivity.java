package net.knvz.android_fortest;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;


public class Android_fortestActivity extends Activity{
	
	private RibbonView rbV;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rbV = new RibbonView(this);        
        setContentView(rbV);
        //setContentView(new EntryImgView(this, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test1), null));
        /*
        ImageView iv = new ImageView(this);
        try {
			iv.setImageBitmap(downloadBitmap("http://1.bp.blogspot.com/-yIf0KriZdUg/TkAD6DXU15I/AAAAAAAAIZ4/mlcb3Yi0TDc/s1600/Desktop+wallpaper+Free+2.jpg"));
		} catch (IOException e) {	
			Log.d("debug", e.toString());			
		}
        setContentView(iv);
        */
   }
 
}