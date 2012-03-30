package net.jstudio.gagfun;

import android.graphics.Rect;

public class TransformRect{
	private float m_MaxScale = 10.f, m_MinScale = 1.f;	
	private int ImgWidth, ImgHeight;	
	private int screenWidth, screenHeight;
	private Rect r_Src, r_ActiveDst, r_ActiveSrc;
	
	public TransformRect(int ImgWidth, int ImgHeight, int screenWidth, int screenHeight){		
		this.ImgWidth = ImgWidth;
		this.ImgHeight = ImgHeight;
		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		r_Src = new Rect();
		r_ActiveDst = new Rect();
		r_ActiveSrc = new Rect();
		Init_Rect();
	}
	
	private void setRect(Rect r, int left, int top, int width, int height){
		r.left = left;
		r.top = top;
		r.right = left + width;
		r.bottom = top + height;
	}
	private void Init_Rect(){
		//
		float scale = (float)screenWidth/ImgWidth;
		int newWidth = ImgWidth, newHeight = (int)(screenHeight/scale);
		setRect(r_Src, 0, 0, newWidth, newHeight);	
	}
	
	public void setMaxScale(float max){
		m_MaxScale = max;
	}
	public void setMinScale(float min){
		m_MinScale = min;
	}
	
	public void Translate(int disX, int disY){
		float scale = screenWidth/r_Src.width();//Scale between screen and corresponding rectangle
		r_Src.offset((int)(disX/scale), (int)(disY/scale));
		//Fix X coordinate
		if(r_Src.right >= ImgWidth)
			r_Src.offset(ImgWidth - r_Src.right, 0);
		if(r_Src.left <= 0)
			r_Src.offset(-r_Src.left, 0);
		//Fix Y coordinate
		if(r_Src.bottom >= ImgHeight)
			r_Src.offset(0, ImgHeight - r_Src.bottom);
		if(r_Src.top <= 0)
			r_Src.offset(0, -r_Src.top);
		TranslateToHalfofScreen();
	}
	
	private void TranslateToHalfofScreen(){
		if(r_Src.height() >= ImgHeight)
			r_Src.offset(0, -(int)((r_Src.height() - ImgHeight)/2));
	}
	public boolean canGoNext(){
		return (r_Src.right >= ImgWidth);
	}
	
	public boolean canGoPrevious(){
		return (r_Src.left <= 0);
	}
	public float getScaled(){
		return (float)(ImgWidth)/r_Src.width();
	}
	
	public void Reset(){
		Init_Rect();
	}
	
	public void Scale(int focalPX, int focalPY, float factor){
		int newWidth = (int)(r_Src.width()/factor);
		int MaxWidth = (int)(ImgWidth/m_MinScale), MinWidth = (int)(ImgWidth/m_MaxScale);
		if(newWidth >= MinWidth && newWidth <= MaxWidth){
			float screenScale = (float)screenWidth/screenHeight;
			int newHeight = (int)(newWidth/screenScale);
			float percentWidth = focalPX/(float)screenWidth, percentHeight = focalPY/(float)screenHeight;
			int newLeft 	= r_Src.left + (int)((r_Src.width() - newWidth)*percentWidth),
					newTop 	= r_Src.top + (int)((r_Src.height() - newHeight)*percentHeight);
			//Fix X coordinate
			if(newLeft + newWidth >= ImgWidth)
				newLeft = ImgWidth - newWidth;
			if(newLeft <= 0)
				newLeft = 0;
			//Fix Y coordinate
			if(newTop + newHeight >= ImgHeight)
				newTop = ImgHeight - newHeight;
			if(newTop <= 0)
				newTop = 0;
			/////////////////////////////////
			setRect(r_Src, newLeft, newTop, newWidth, newHeight);
			TranslateToHalfofScreen();
		}
	}

	public Rect getSrcRect(){
		if(r_Src.height() >= ImgHeight){
			int 	left 	= r_Src.left,
					top		= 0,
					width 	= r_Src.width(),
					height 	= ImgHeight;
			setRect(r_ActiveSrc, left, top, width, height);
		}else
			setRect(r_ActiveSrc, r_Src.left, r_Src.top,
							r_Src.width(), r_Src.height());
		return r_ActiveSrc;
	}
	
	public Rect getDstRect(){
		if(r_Src.height() >= ImgHeight){	
			int		 width = r_Src.width(),
					height = ImgHeight;
			float scale = (float)width/screenWidth;
			int 	newLeft 	= 0,
					newTop 		= (int)((screenHeight - height/scale)/2),
					newWidth 	= (int)(width/scale),
					newHeight	= (int)(height/scale);
			setRect(r_ActiveDst, newLeft, newTop, newWidth, newHeight);
		}else
			setRect(r_ActiveDst, 0, 0, screenWidth, screenHeight);
		return r_ActiveDst;
	}
}