package net.knvz.android_fortest;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.widget.ViewFlipper;


public class Android_fortestActivity extends Activity{
	ViewAnimator va;
	GestureDetector gd;
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