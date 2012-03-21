package net.jstudio.gagfun;

import java.util.LinkedList;
import java.util.Queue;

import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.GagEntry;
import net.jstudio.gagCore.NineGAG;
import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class RibbonView extends ViewAnimator {	
	private static int MaxFirstImage = 5;
	private static final int MaxLoadAhead = 2;
	private Animation 	anim_InFromLeft,
						anim_InFromRight,
						anim_OutToLeft,
						anim_OutToRight;	
	private NineGAG _nineGag;
	private Queue<GagEntry> queue_Download;
	private int iCurrentLoadAhead = 0;
	Animation mAnimation;

	private void addNewView(GagEntry entry){
		entry.addDownloadFinished(new GagEntry.DownloadFinishedListener() {			
			public void OnDownloadFinished() {
				GagEntry g = queue_Download.poll();
				if(g != null)
					g.StartDownloadBitmap();
			}
		});
		queue_Download.add(entry);
		if(queue_Download.size() == 1){
			queue_Download.poll();
			entry.StartDownloadBitmap();
		}
		EntryImgView img = new EntryImgView(this.getContext(), entry, this);

		LinearLayout layout=new LinearLayout(this.getContext());
		LinearLayout layout_title=new LinearLayout(this.getContext());
		LinearLayout layout_main=new LinearLayout(this.getContext());
		TextView title=new TextView(this.getContext());
		String text= entry.getEntryName();
		title.setText(text);

		if (text.length()>40){
			mAnimation = new TranslateAnimation(0,-200,0.0f,0.0f);
			mAnimation.setDuration(10000);
			mAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+2000);
			mAnimation.setRepeatCount(Animation.INFINITE);
			mAnimation.setRepeatMode(Animation.REVERSE);
			title.setAnimation(mAnimation);
		}
		LayoutParams params =
	        		new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout_title.addView(title);
		layout_title.setBackgroundResource(android.R.drawable.title_bar);
		layout_main.addView(img);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(layout_title);
		layout.addView(layout_main);
		addView(layout,params);
	
		
		
		iCurrentLoadAhead++;
	}
	
	public RibbonView(Context ct) {
		super(ct);		
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		loadAnimation();
		//Queue for Download Image
		queue_Download = new LinkedList<GagEntry>();
		//
		
		//Initiate for 9Gag
		_nineGag = new NineGAG(ct);
		_nineGag.setLoadFirstEntriesFinished(new NineGAG.LoadFirstEntriesFinishedListener() {
			
			public void OnLoadFirstEntriesFinished() {
				for(int i = 0; i < MaxFirstImage; i++){
					addNewView(_nineGag.getListHot().get(i));
				}
			}
		});		
	}	

	private void loadAnimation(){
		Context ct = this.getContext();
		anim_InFromLeft = AnimationUtils.loadAnimation(ct, R.anim.infromleft);
		anim_InFromRight = AnimationUtils.loadAnimation(ct, R.anim.infromright);
		anim_OutToLeft = AnimationUtils.loadAnimation(ct, R.anim.outtoleft);
		anim_OutToRight = AnimationUtils.loadAnimation(ct, R.anim.outtoright);
	}
	
	public void goNext(){		
		_nineGag.Next(EntryType.HOT);
		if(getChildCount() - getDisplayedChild()<= MaxLoadAhead)
				addNewView(_nineGag.getListHot().get(iCurrentLoadAhead + 1));
		
		//Animation
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
