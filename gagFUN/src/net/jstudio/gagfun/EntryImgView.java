package net.jstudio.gagfun;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class EntryImgView extends View {
	private Bitmap m_bitmap;
	private ScaleGestureDetector _scaleDetector;
	private GestureDetector _gestureDetector;
	private TransformRect r_img;
	private RibbonView m_rbV;
	
	public EntryImgView(Context context) {
		super(context);
	}
	
	public EntryImgView(Context context, Bitmap bmp, RibbonView rbV){
		this(context);
		m_bitmap = bmp;
		m_rbV = rbV;
		_scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		_gestureDetector = new GestureDetector(context, new GestureListener());
		_gestureDetector.setOnDoubleTapListener(new DoubleTapListener());
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawBitmap(m_bitmap, null, r_img.getRect(), null);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {		
		float scaleW = m_bitmap.getWidth()/w; //No handle for scaleW < 1;
		
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
			float _scale = detector.getScaleFactor();
			r_img.Scale((int)detector.getFocusX(), (int)detector.getFocusY()
					, _scale);
			//r_img.FixScale();
			invalidate();
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {			
			super.onScaleEnd(detector);
			//r_img.FixScale();
			//invalidate();			
		}
		
	}
	
	private class DoubleTapListener implements GestureDetector.OnDoubleTapListener{

		public boolean onDoubleTap(MotionEvent e) {
			
			if(r_img.getScaled() != 1.f)
				r_img.Reset();				
			else
				r_img.Scale((int)e.getX(), (int)e.getY(), 2.f);
			invalidate();
			
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
			if(velocityX < 0 && r_img.canGoNext()){
				m_rbV.goNext();				
			}else if (velocityX >= 0 && r_img.canGoPrevious()){
				m_rbV.goPrevious();				
			}			
			return true;
		}

		public void onLongPress(MotionEvent e) {			
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			r_img.Translate(-(int)distanceX, -(int)distanceY);
			invalidate();
			return true;
		}
		public void onShowPress(MotionEvent e) {}
		public boolean onSingleTapUp(MotionEvent e) {			
			return false;
		}
		
	}
}
