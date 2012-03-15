package net.knvz.android_fortest;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;

public class EntryImgView extends View {
	private Bitmap m_bitmap;
	private ScaleGestureDetector _scaleDetector;
	private TransformRect r_img;
	private float prevX, prevY;
	private int activePointerId = -1;
	
	public EntryImgView(Context context) {
		super(context);
	}
	
	public EntryImgView(Context context, Bitmap bmp){
		this(context);
		m_bitmap = bmp;
		_scaleDetector = new ScaleGestureDetector(context, new ScaleListener());
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
		switch(ev.getAction() & MotionEvent.ACTION_MASK){
		case MotionEvent.ACTION_DOWN:{
			prevX = ev.getX();prevY = ev.getY();
			activePointerId = ev.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE:{
			int pIndex = ev.findPointerIndex(activePointerId);
			float x = ev.getX(pIndex), y = ev.getY(pIndex);
			if(!_scaleDetector.isInProgress()){
				r_img.Translate((int)(x - prevX), (int)(y - prevY));
				this.invalidate();
			}
			prevX = x;prevY = y;
			break;
		}
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			activePointerId = -1;
			break;
		case MotionEvent.ACTION_POINTER_UP:{
			int pIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	        int pId = ev.getPointerId(pIndex);
	        if(pId == activePointerId){
	        	int pNewIndex = (pIndex == 0) ? 1 : 0;
	        	prevX = ev.getX(pNewIndex);
	        	prevY = ev.getY(pNewIndex);
	        	activePointerId = ev.getPointerId(pNewIndex);
	        }
			break;
		}
		}
		return true;
	}
	
	
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float _scale = detector.getScaleFactor();
			r_img.Scale((int)detector.getFocusX(), (int)detector.getFocusY()
					, _scale);
			invalidate();
			return true;
		}

		@Override
		public void onScaleEnd(ScaleGestureDetector detector) {			
			super.onScaleEnd(detector);
			r_img.FixScale();
			invalidate();			
		}
		
	}
}
