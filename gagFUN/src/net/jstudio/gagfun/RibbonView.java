package net.jstudio.gagfun;

import java.util.LinkedList;
import java.util.Queue;

import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.GagEntry;
import net.jstudio.gagCore.NineGAG;
import android.content.Context;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class RibbonView extends ViewAnimator {	
	private static final int MaxFirstImage = 5;
	private static final int MaxLoadAhead = (int)(MaxFirstImage/2);
	private Animation 	anim_InFromLeft,
						anim_InFromRight,
						anim_OutToLeft,
						anim_OutToRight;	
	private NineGAG _nineGag;
	private Queue<GagEntry> queue_Download;
	private EntryType m_type;
	private void addNewView(GagEntry entry){
		entry.addDownloadFinished(new GagEntry.DownloadFinishedListener() {			
			public void OnDownloadFinished() {
				GagEntry g = queue_Download.poll();
				if(g != null)
					g.StartDownloadBitmap();
			}
		});
		addEntryToDownloadQueue(entry);
		
		EntryImgView img = new EntryImgView(this.getContext(), entry, this);
		
		FrameLayout layout=new FrameLayout(this.getContext());
		TextView title=new TextView(this.getContext());
		String text= entry.getEntryName();
		title.setText(text);
		/*if (text.length()>10){
			mAnimation = new TranslateAnimation(0,-200,0.0f,0.0f);
			mAnimation.setDuration(10000);
			mAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis()+2000);
			mAnimation.setRepeatCount(Animation.INFINITE);
			mAnimation.setRepeatMode(Animation.REVERSE);
			title.setAnimation(mAnimation);
		}*/
		LayoutParams params =
	        		new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		layout.addView(img);
		
		addView(layout,params);
		layout.addView(title);
				
	}
	
	private void addEntryToDownloadQueue(GagEntry entry){		
		queue_Download.add(entry);
		if(queue_Download.size() == 1){
			queue_Download.poll();
			entry.StartDownloadBitmap();
		}
	}

	public RibbonView(Context ct, EntryType type, NineGAG nineGag) {
		super(ct);	
		m_type = type;	
		setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
		loadAnimation();
		//Queue for Download Image
		queue_Download = new LinkedList<GagEntry>();
	
		//Set event download finish for 9Gag
		_nineGag = nineGag;
		_nineGag.setLoadFirstEntriesFinished(new NineGAG.LoadFirstEntriesFinishedListener() {
			
			public void OnLoadFirstEntriesFinished() {
				for(int i = 0; i < MaxFirstImage; i++){
					addNewView(_nineGag.getList(m_type).get(i));				
				}
			}
		});		
		_nineGag.StartDownloadFirstPage(m_type);
	}	

	private void loadAnimation(){
		Context ct = this.getContext();
		anim_InFromLeft = AnimationUtils.loadAnimation(ct, R.anim.infromleft);
		anim_InFromRight = AnimationUtils.loadAnimation(ct, R.anim.infromright);
		anim_OutToLeft = AnimationUtils.loadAnimation(ct, R.anim.outtoleft);
		anim_OutToRight = AnimationUtils.loadAnimation(ct, R.anim.outtoright);
	}
	
	public void goNext(){		
		int iChildCount = getChildCount(), iDisplayedChild = getDisplayedChild(); 
		
		//Add newView if necessary
		if(iDisplayedChild + MaxLoadAhead + 1 == iChildCount){			
			_nineGag.Next(m_type);//Next as rule
			addNewView(_nineGag.getList(m_type).get(iChildCount));
		}else{
			addEntryToDownloadQueue(
					_nineGag.getList(m_type).get(iDisplayedChild + MaxLoadAhead + 1));
		}		
		//Dispose previous Image
		int prevImg = iDisplayedChild - MaxLoadAhead;
		if(prevImg >= 0)
			_nineGag.getList(m_type).get(prevImg).DisposeImage();			
		
		//Animation
		if(iDisplayedChild != iChildCount - 1){
			setInAnimation(anim_InFromRight);
			setOutAnimation(anim_OutToLeft);
			showNext();
		}		
	}
	
	public void goPrevious(){
		int		iDisplayedChild = getDisplayedChild(),
				prevImg 		= iDisplayedChild - MaxLoadAhead - 1,
				aheadImg 		= iDisplayedChild + MaxLoadAhead; 
		if( prevImg >= 0)
			addEntryToDownloadQueue(_nineGag.getList(m_type).get(prevImg));
		if(aheadImg >= MaxFirstImage)
			_nineGag.getList(m_type).get(aheadImg).DisposeImage();
		
		//Animation
		if(iDisplayedChild != 0){
			setInAnimation(anim_InFromLeft);
			setOutAnimation(anim_OutToRight);
			showPrevious();
		}
	}

}