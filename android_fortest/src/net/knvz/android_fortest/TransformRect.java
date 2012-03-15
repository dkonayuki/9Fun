package net.knvz.android_fortest;

import android.graphics.Rect;

public class TransformRect{
	private float m_MaxScale = 10.f, m_MinScale = 1.f;
	private int left, top,
				width, height;
	private int oLeft, oTop,
			oWidth, oHeight;//o = original	
	private static final int error_range = 10;//pixel
	private int screenHeight;
	
	public TransformRect(int left, int top, int width, int height, int screenHeight){
		this.left = oLeft = left;
		this.top  = oTop = top;
		this.width = oWidth = width;
		this.height = oHeight = height;
		this.screenHeight = screenHeight;
	}
	
	public void Translate(int disX, int disY){
		//this.left += disX;this.top += disY;
		int newLeft = left + disX, newTop = top + disY;
		if(newLeft <= 0 && newLeft >= (oWidth - width))
			this.left = newLeft;
		if(height > screenHeight && newTop <= 0 && newTop >= (screenHeight - height))
			this.top = newTop;
	}
	
	public void Scale(int focalPX, int focalPY, float factor){
		float percentWidth = ((float)(focalPX - left)/width), percentHeight = ((float)(focalPY - top)/height);		
		width = (int)Math.max(oWidth*m_MinScale, Math.min(oWidth*m_MaxScale, width*factor));
		height = (int)Math.max(oHeight*m_MinScale, Math.min(oHeight*m_MaxScale, height*factor));
		if(Math.abs(width - oWidth) <= error_range 
				&& Math.abs(height - oHeight) <= error_range){
			left = oLeft;
			top = oTop;
		}else{
			left = (int) (focalPX - width*percentWidth);
			top = (int) (focalPY - height*percentHeight);

		}
	}
	
	public void FixScale(){
		if(height < screenHeight){
			left = (oWidth - width)/2;
			top = (screenHeight - height)/2;
		}
	}
	
	public void setMaxScale(float max){
		m_MaxScale = max;
	}
	
	public void setMinScale(float min){
		m_MinScale = min;
	}
	public Rect getRect(){
		return new Rect(left, top, left + width - 1, top + height - 1);
	}
}
