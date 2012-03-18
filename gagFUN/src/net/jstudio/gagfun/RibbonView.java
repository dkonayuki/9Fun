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
		addView(img);
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
