package net.jstudio.gagfun;

import java.io.IOException;
import java.io.InputStream;


import net.jstudio.gagCore.GagEntry;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;

public class EntryImgView extends View {
	private static final String sLoadingFileName = "loading.gif";
	private static final double MaxAngleForFling = 0.707106781;//sin(pi/4) 
	private ScaleGestureDetector _scaleDetector;
	private GestureDetector _gestureDetector;
	private TransformRect r_img;
	private RibbonView m_rbV;
	private Movie mv_Loading;
	private long lStartImageLoading = 0;	
	private GagEntry _gagEntry;
	

	
	public EntryImgView(Context context) {
		super(context);
	
	}
	
	public EntryImgView(Context context, GagEntry gagEntry, RibbonView rbV){
		this(context);
		//m_bitmap = bmp;
		//Load Loading image
		try {
			mv_Loading = Movie.decodeStream(context.getAssets().open(sLoadingFileName));
		} catch (IOException e) {
		}
		m_rbV = rbV;
		
		//GagEntry
		this._gagEntry = gagEntry;	
		_gagEntry.addDownloadFinished(new GagEntry.DownloadFinishedListener() {
			
			public void OnDownloadFinished() {
                FixImageSize(getWidth(), getHeight());                     
                invalidate();				
			}
		});
		//
		_scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		_gestureDetector = new GestureDetector(context, new GestureListener());
		_gestureDetector.setOnDoubleTapListener(new DoubleTapListener());		
	}

	public GagEntry getGagEntry(){return _gagEntry;}
		
	@Override
	
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(!_gagEntry.isDownloaded()){
			canvas.drawColor(Color.TRANSPARENT);
			long now = SystemClock.uptimeMillis();
			if(lStartImageLoading == 0)
				lStartImageLoading = now;
			int relTime = (int)(now - lStartImageLoading) % mv_Loading.duration();
			mv_Loading.setTime(relTime);			
			mv_Loading.draw(canvas, (this.getWidth() - mv_Loading.width())/2, (this.getHeight() - mv_Loading.height())/2);		
			invalidate();	
		}else if(r_img != null)
			{
		
			canvas.drawBitmap(_gagEntry.getBitmap(), null, r_img.getRect(), null);
			
			}
	}
	
	
	private void FixImageSize(int w, int h){
		float scaleW = (float)_gagEntry.getBitmap().getWidth()/w; //No handle for scaleW < 1;
		
		int left, top, width, height;		
		width = w;
		height = (int)(_gagEntry.getBitmap().getHeight()/scaleW);
		//Locate in center		
		//Translate to 0(height) if left < 0
		left = 0;
		top = (height > h) ? 0 : (int)((h - height)/2); 
		
		r_img = new TransformRect(left, top, width, height, h);
		r_img.setMaxScale(3.f);
		r_img.setMinScale(1.f);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		if(_gagEntry.isDownloaded()){
			FixImageSize(w, h);
		}
		super.onSizeChanged(w, h, oldw, oldh);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		_scaleDetector.onTouchEvent(ev);
		_gestureDetector.onTouchEvent(ev);
		return true;		
	}	

	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if(_gagEntry.isDownloaded()){
				float _scale = detector.getScaleFactor();
				r_img.Scale((int)detector.getFocusX(), (int)detector.getFocusY()
						, _scale);
				//r_img.FixScale();
				invalidate();
			}
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {			
			super.onScaleEnd(detector);			
		}
		
	}
	
	private class DoubleTapListener implements GestureDetector.OnDoubleTapListener{

		public boolean onDoubleTap(MotionEvent e) {
			if(_gagEntry.isDownloaded()){
				if(r_img.getScaled() != 1.f)
					r_img.Reset();				
				else
					r_img.Scale((int)e.getX(), (int)e.getY(), 2.f);
				invalidate();
			}
			return true;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		public boolean onSingleTapConfirmed(MotionEvent e) {
			return false;
		}
		
	}
	
	private class GestureListener implements GestureDetector.OnGestureListener{

		public boolean onDown(MotionEvent e) {
			return false;
		}

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if(_gagEntry.isDownloaded()){
				//Check if whether the fling's angle is bellow MaxAngleForFling
				boolean bAngle = (Math.abs(velocityY)/Math.sqrt(velocityX*velocityX + velocityY*velocityY) <= MaxAngleForFling);
				if(bAngle){
					if(velocityX < 0 && r_img.canGoNext()){
						m_rbV.goNext();				
					}else if (velocityX >= 0 && r_img.canGoPrevious()){
						m_rbV.goPrevious();				
					}			
				}
			}else{
				if(velocityX < 0)
					m_rbV.goNext();
				else
					m_rbV.goPrevious();
			}
			return true;
		}

		public void onLongPress(MotionEvent e) {			
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if(_gagEntry.isDownloaded()){
				r_img.Translate(-(int)distanceX, -(int)distanceY);
				invalidate();
			}
			return true;
		}
		public void onShowPress(MotionEvent e) {}
		public boolean onSingleTapUp(MotionEvent e) {			
			return false;
		}
		
	}
}
