package net.jstudio.gagfun;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ViewAnimator;

public class RibbonView extends ViewAnimator {
	//private final static int MAX_VIEW_CHILD = 5;
	private Animation 	anim_InFromLeft,
						anim_InFromRight,
						anim_OutToLeft,
						anim_OutToRight;
	
	public RibbonView(Context ct) {
		super(ct);
		loadAnimation();
		/*
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));		
		EntryImgView entry1 = new EntryImgView(ct, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test1), this);
		EntryImgView entry2 = new EntryImgView(ct, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test2), this);
		EntryImgView entry3 = new EntryImgView(ct, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test3), this);
		EntryImgView entry4 = new EntryImgView(ct, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test4), this);
		EntryImgView entry5 = new EntryImgView(ct, BitmapFactory.decodeResource(this.getResources(), R.drawable.img_test5), this);
		addView(entry1);
		addView(entry2);
		addView(entry3);
		addView(entry4);
		addView(entry5);
		*/		
	}
	
	private void loadAnimation(){
		Context ct = this.getContext();
		anim_InFromLeft = AnimationUtils.loadAnimation(ct, R.anim.infromleft);
		anim_InFromRight = AnimationUtils.loadAnimation(ct, R.anim.infromright);
		anim_OutToLeft = AnimationUtils.loadAnimation(ct, R.anim.outtoleft);
		anim_OutToRight = AnimationUtils.loadAnimation(ct, R.anim.outtoright);
	}
	public void goNext(){
		if(getDisplayedChild() != getChildCount() - 1){
			setInAnimation(anim_InFromRight);
			setOutAnimation(anim_OutToLeft);
			showNext();
		}
	}
	
	public void goPrevious(){
		if(getDisplayedChild() != 0){
			setInAnimation(anim_InFromLeft);
			setOutAnimation(anim_OutToRight);
			showPrevious();
		}
	}

}
