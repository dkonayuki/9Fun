package net.jstudio.gagfun;

import android.graphics.Rect;

public class TransformRect{
	private float m_MaxScale = 10.f, m_MinScale = 1.f;
	private int left, top,
				width, height;
	private int oLeft, oTop,
			oWidth, oHeight;//o = original	
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
	
	public boolean canGoNext(){
		return (width + left - oWidth <= 0);
	}
	
	public boolean canGoPrevious(){
		return (left >= 0);
	}
	public float getScaled(){
		return (float)(width)/oWidth;
	}
	
	public boolean isOriginal(){
		return (left == oLeft) && (top == oTop)
				&& (width == oWidth) && (height == oHeight);
	}
	public void Reset(){
		left = oLeft;top = oTop;
		width = oWidth;height = oHeight;
	}
	
	public void Scale(int focalPX, int focalPY, float factor){
		/*
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
		*/
		float percentWidth = ((float)(focalPX - left)/width), percentHeight = ((float)(focalPY - top)/height);
		//Find appropriate scale
		width = (int)(width*factor);
		height = (int)(height*factor);
		if(oWidth*m_MinScale >= width*factor){
			width = (int)(oWidth*m_MinScale);
			height = (int)(oHeight*m_MinScale);
		}
		if(oWidth*m_MaxScale <= width*factor){
			width = (int)(oWidth*m_MaxScale);
			height = (int)(oHeight*m_MaxScale);
		}
		//
		if(height <= screenHeight){			
			//left = (oWidth - width)/2;
			//Fit on the left and right;
			if(width + left <= oWidth)
				left = oWidth - width;
			else if(left >= 0)
				left = 0;
			else
				left = (int) (focalPX - width*percentWidth);
			//Fit on top and bottom
			
			top = (screenHeight - height)/2; 
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