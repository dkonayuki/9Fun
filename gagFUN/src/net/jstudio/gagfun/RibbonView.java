package net.jstudio.gagfun;

import java.util.LinkedList;
import java.util.Queue;

import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.GagEntry;
import net.jstudio.gagCore.NineGAG;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class RibbonView extends ViewAnimator {	
	private static final int MaxFirstImage = 5;
	private static final int MaxLoadAhead = (int)(MaxFirstImage/2);
	private Animation 	anim_InFromLeft,
						anim_InFromRight,
						anim_InFromTop,
						anim_InFromBot,
						anim_OutToLeft,
						anim_OutToRight,
						anim_OutToTop,
						anim_OutToBot;
	
	private NineGAG _nineGag;
	private Queue<GagEntry> queue_Download;
	private EntryType m_type;
	boolean menu_on=false;
	TextView m_Title;
	FrameLayout layout;
	LinearLayout menuTop,menuBot;
	
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
		LayoutParams params =
        		new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		layout=new FrameLayout(this.getContext());
		layout.addView(img);
		addView(layout,params);
	}
	public boolean isDisplayedMenu(){
		return menu_on;
	}
	public void createMenu(){
		//Menu Top
		menuTop = new LinearLayout(this.getContext());
		menuTop.setBackgroundResource(R.drawable.title_bar);
	    menuTop.setGravity(Gravity.RIGHT);
		menuTop.setOrientation(LinearLayout.HORIZONTAL);
		 //Reload Button
        ImageView btt_Refresh= new ImageView(this.getContext()); 
        btt_Refresh.setImageResource(R.drawable.ic_reload);
        LinearLayout temp= new LinearLayout(this.getContext());
        temp.addView(btt_Refresh,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        menuTop.addView(temp,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
       
        //Title
		m_Title = new TextView(this.getContext());
        m_Title.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        m_Title.setTextColor(Color.WHITE);
        m_Title.setTextSize(20);
        menuTop.addView(m_Title,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT)); 
        
        //Menu Bottom
        menuBot = new LinearLayout(this.getContext());
        menuBot.setOrientation(LinearLayout.HORIZONTAL);
        menuBot.setBackgroundResource(R.drawable.bar_bottom);
        //Like Button
        LinearLayout temp3= new LinearLayout(this.getContext());
        ImageView btt_Like = new ImageView(this.getContext());
        btt_Like.setImageResource(R.drawable.ic_like);
        TextView like_number = new TextView(this.getContext());
        like_number.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        like_number.setText("2");
        like_number.setTextSize(20);
        temp3.addView(btt_Like);
        temp3.addView(like_number,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        menuBot.addView(temp3);
        //Comment Button
        ImageView btt_Comment = new ImageView(this.getContext());
        btt_Comment.setImageResource(R.drawable.ic_cmt);
        LinearLayout temp2= new LinearLayout(this.getContext());
        temp2.addView(btt_Comment,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        temp2.setOrientation(LinearLayout.HORIZONTAL);
        temp2.setGravity(Gravity.RIGHT);
        menuBot.addView(temp2,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	}
	
	
	
	public void displayMenu(String title){
		menu_on=true;
		title=title.replace("&#039;","'");
		m_Title.setText(title);
		((ViewGroup) this.getCurrentView()).addView(menuTop,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.TOP));
		((ViewGroup) this.getCurrentView()).addView(menuBot,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.BOTTOM));
		menuTop.startAnimation(anim_InFromTop);
		menuBot.startAnimation(anim_InFromBot);
	}
	public void hideMenu(){
		menu_on=false;
		menuTop.startAnimation(anim_OutToTop);
		menuBot.startAnimation(anim_OutToBot);
		((ViewGroup) this.getCurrentView()).removeView(menuTop);
		((ViewGroup) this.getCurrentView()).removeView(menuBot);
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
		createMenu();
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
		anim_InFromTop= AnimationUtils.loadAnimation(ct, R.anim.infromtop);
		anim_InFromBot= AnimationUtils.loadAnimation(ct, R.anim.infrombot);
		anim_OutToLeft = AnimationUtils.loadAnimation(ct, R.anim.outtoleft);
		anim_OutToRight = AnimationUtils.loadAnimation(ct, R.anim.outtoright);
		anim_OutToBot = AnimationUtils.loadAnimation(ct, R.anim.outtobot);
		anim_OutToTop= AnimationUtils.loadAnimation(ct, R.anim.outtotop);
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
			if (isDisplayedMenu()) hideMenu();
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
			if (isDisplayedMenu()) hideMenu();
			showPrevious();
		}
	}

}