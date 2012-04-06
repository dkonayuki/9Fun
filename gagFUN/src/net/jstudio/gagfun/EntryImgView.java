package net.jstudio.gagfun;


import net.jstudio.gagCore.GagEntry;
import android.content.Context;
import android.os.SystemClock;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Canvas;
import android.graphics.Color;

public class EntryImgView extends View {	
	private static final double MaxAngleForFling = 0.707106781;//sin(pi/4) 
	private ScaleGestureDetector _scaleDetector;
	private GestureDetector _gestureDetector;
	private TransformRect r_img;
	private RibbonView m_rbV;
	private long lStartImageLoading = 0;	
	private long lStartImageLoading2 = 0;
	private GagEntry _gagEntry;
	boolean notification;
	
	public EntryImgView(Context context) {
		super(context);
	}
	
	public EntryImgView(Context context, GagEntry gagEntry, RibbonView rbV){
		this(context);
		m_rbV = rbV;
		notification=true;
		
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
			
		if(_gagEntry.isNSFW()){
			FixImageSize(getWidth(), getHeight());    
			canvas.drawBitmap(PublicResource.NSFWBitmap(), r_img.getSrcRect(),r_img.getDstRect(), null);
		}else if(!_gagEntry.isDownloaded()){			
			canvas.drawColor(Color.TRANSPARENT);
			long now = SystemClock.uptimeMillis();
			if(lStartImageLoading == 0)
				lStartImageLoading = now;
			int relTime = (int)(now - lStartImageLoading) % PublicResource.LoadingMovie().duration();
			PublicResource.LoadingMovie().setTime(relTime);			
			PublicResource.LoadingMovie().draw(canvas, (this.getWidth() - PublicResource.LoadingMovie().width())/2, (this.getHeight() - PublicResource.LoadingMovie().height())/2);		
			invalidate();	
		}else if(r_img != null){		
			//canvas.drawBitmap(_gagEntry.getBitmap(), null, r_img.getRect(), null);
			canvas.drawBitmap(_gagEntry.getBitmap(), r_img.getSrcRect(),r_img.getDstRect(), null);
			if (_gagEntry.getBitmap().getHeight()>this.getHeight()&&notification){
				
			
					long now2 = SystemClock.uptimeMillis();
					if(lStartImageLoading2 == 0)
						lStartImageLoading2 = now2;
					int relTime = (int)(now2 - lStartImageLoading2) % PublicResource.LoadingDown().duration();
					PublicResource.LoadingDown().setTime(relTime);	
					PublicResource.LoadingDown().draw(canvas, (this.getWidth() - PublicResource.LoadingDown().width())/2, (this.getHeight() - PublicResource.LoadingDown().height()));		
					invalidate();
			}
		}
	}
	
	
	private void FixImageSize(int w, int h){
		if(_gagEntry.isNSFW())
			r_img = new TransformRect(PublicResource.NSFWBitmap().getWidth(),
									PublicResource.NSFWBitmap().getHeight(),
									w, h);
		else
			r_img = new TransformRect(_gagEntry.getBitmap().getWidth(), 
								_gagEntry.getBitmap().getHeight(),
								w, h);
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
				if(r_img.getScaled() != 1.f){
					r_img.Reset();
					//r_img.Scale((int)e.getX(), (int)e.getY(), 0.5f);
				}else
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
			if(!PublicResource.getPrefTouchMode(EntryImgView.this.getContext())){
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
			}
			return true;
		}

		public void onLongPress(MotionEvent e) {			
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			notification=false;
			if(_gagEntry.isDownloaded()){
				r_img.Translate((int)distanceX, (int)distanceY);
				invalidate();
			}
			return true;
		}
		public void onShowPress(MotionEvent e) {}
		public boolean onSingleTapUp(MotionEvent e) {	
			if (m_rbV.isDisplayedMenu()) m_rbV.hideMenu();
			else m_rbV.displayMenu();
			return true;
		}
		
	}
}
