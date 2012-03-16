package net.jstudio.gagfun;

import java.io.IOException;


import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;

public class EntryImgView extends View {
	private static final String sLoadingFileName = "loading.gif";
	private Bitmap m_bitmap;
	private ScaleGestureDetector _scaleDetector;
	private GestureDetector _gestureDetector;
	private TransformRect r_img;
	private RibbonView m_rbV;
	private Movie mv_Loading;	
	private boolean isLoading;
	private long lStartImageLoading = 0;
	private DownloadImageTask dlTask;
	
	public EntryImgView(Context context) {
		super(context);
	}
	
	public EntryImgView(Context context, String url, RibbonView rbV){
		this(context);
		//m_bitmap = bmp;
		//Load Loading image
		try {
			mv_Loading = Movie.decodeStream(context.getAssets().open(sLoadingFileName));
		} catch (IOException e) {
		}
		m_rbV = rbV;
		isLoading = true;
		//Start Download Image
		dlTask = new DownloadImageTask();
		dlTask.execute(url);
		//
		_scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		_gestureDetector = new GestureDetector(context, new GestureListener());
		_gestureDetector.setOnDoubleTapListener(new DoubleTapListener());		
	}

	
	  
	private Bitmap downloadBitmap(String url) throws IOException {
		HttpUriRequest request = new HttpGet(url.toString());
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = httpClient.execute(request);

		StatusLine statusLine = response.getStatusLine();
		int statusCode = statusLine.getStatusCode();
		if (statusCode == 200) {
			HttpEntity entity = response.getEntity();
			byte[] bytes = EntityUtils.toByteArray(entity);

			Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
					bytes.length);
			return bitmap;
		} else {
			throw new IOException("Download failed, HTTP response code "
					+ statusCode + " - " + statusLine.getReasonPhrase());
		}
	}
		
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(isLoading){
			canvas.drawColor(Color.TRANSPARENT);
			long now = SystemClock.uptimeMillis();
			if(lStartImageLoading == 0)
				lStartImageLoading = now;
			int relTime = (int)(now - lStartImageLoading) % mv_Loading.duration();
			mv_Loading.setTime(relTime);			
			mv_Loading.draw(canvas, (this.getWidth() - mv_Loading.width())/2, (this.getHeight() - mv_Loading.height())/2);
			invalidate();	
		}else if(r_img != null)
			canvas.drawBitmap(m_bitmap, null, r_img.getRect(), null);
	}
	
	private void FixImageSize(int w, int h){
		float scaleW = (float)m_bitmap.getWidth()/w; //No handle for scaleW < 1;
		
		int left, top, width, height;		
		width = w;
		height = (int)(m_bitmap.getHeight()/scaleW);
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
		if(!isLoading){
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
	
	private class DownloadImageTask extends AsyncTask<String, Void, Void>{

		@Override
		protected Void doInBackground(String... params) {
			//Delete later
			try {
				m_bitmap = downloadBitmap(params[0]);
			} catch (IOException e) {				
			}
			//
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {			
			isLoading = false;
			int w = getWidth(), h = getHeight();
			FixImageSize(w, h);			
			invalidate();
		}
		
		
	}
	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			if(!isLoading){
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
			if(!isLoading){
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
			if(!isLoading){
				if(velocityX < 0 && r_img.canGoNext()){
					m_rbV.goNext();				
				}else if (velocityX >= 0 && r_img.canGoPrevious()){
					m_rbV.goPrevious();				
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
			if(!isLoading){
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
