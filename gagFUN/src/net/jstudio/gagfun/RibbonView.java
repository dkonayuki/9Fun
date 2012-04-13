package net.jstudio.gagfun;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.jstudio.gagCore.EntryType;
import net.jstudio.gagCore.GagEntry;
import net.jstudio.gagCore.NineGAG;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewAnimator;

public class RibbonView extends ViewAnimator {	
	private static final int MaxFirstImage = 3;
	private static final int MaxLoadAhead = (int)(MaxFirstImage/2);
	public Animation 	anim_InFromLeft,
						anim_InFromRight,
						anim_InFromTop,
						anim_InFromBot,
						anim_OutToLeft,
						anim_OutToRight,
						anim_OutToTop,
						anim_OutToBot,
						anim_FadeIn,
						anim_FadeOut;
	
	private NineGAG _nineGag;
	private Queue<GagEntry> queue_Download;
	private EntryType m_type;
	private boolean menu_on = false;
	
	private TextView m_Title, m_LikeNumber;
	private ToggleButton btt_Like;
	private FrameLayout layout;
	private LinearLayout menuTop, menuBot;
	private CommentDialog FBCmtDialog = null;
	
	public boolean NeedToRefresh = false;
	
	
	private GagEntry getCurrentEntry(){
		if(_nineGag.getList(m_type).size() > 0)
			return _nineGag.getList(m_type).get(getDisplayedChild());
		return null;
	}
	
	private void addNewView(GagEntry entry, boolean addToQueue){
		entry.addDownloadFinished(new GagEntry.DownloadFinishedListener() {			
			public void OnDownloadFinished() {
				GagEntry g = queue_Download.poll();
				if(g != null)
					g.StartDownloadBitmap();
			}
		});
		if(addToQueue)
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
	   // menuTop.setGravity(Gravity.RIGHT);
		menuTop.setOrientation(LinearLayout.HORIZONTAL);
		 //Reload Button
        ImageButton btt_Refresh= new ImageButton(this.getContext());
        btt_Refresh.setBackgroundColor(Color.TRANSPARENT);
        btt_Refresh.setImageResource(R.drawable.button_refresh);
        btt_Refresh.setOnClickListener(new OnClickListener(){

			public void onClick(View v) {				
				RibbonView.this.Reset();
			}        	
        });
        LinearLayout temp= new LinearLayout(this.getContext());
        temp.addView(btt_Refresh,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        menuTop.addView(temp,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));        
       
        //Title
        LinearLayout temp4 = new LinearLayout(this.getContext());
		m_Title = new TextView(this.getContext());
        m_Title.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        m_Title.setTextColor(0xff222222);
        m_Title.setTextSize(20);
        m_Title.setBackgroundResource(R.drawable.black_button_big2);
        temp4.addView(m_Title,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL));
        menuTop.addView(temp4,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT)); 
        
        //Menu Bottom
        menuBot = new LinearLayout(this.getContext());
        menuBot.setOrientation(LinearLayout.HORIZONTAL);
        //Like Button
        LinearLayout temp3= new LinearLayout(this.getContext());
        btt_Like = new ToggleButton(this.getContext());
        btt_Like.setText("");
        btt_Like.setTextOff("");
        btt_Like.setTextOn("");
        btt_Like.setBackgroundResource(R.drawable.button_like_bg);
        btt_Like.setOnClickListener(new OnClickListener(){

        	
			public void onClick(View v) {
				CompoundButton btt = (CompoundButton)v;
				if(_nineGag.Logged()){
					final GagEntry entry = getCurrentEntry();
					GagEntry.LikeDisLikeCallback ldk = new GagEntry.LikeDisLikeCallback() {
						public void OnLikeDisLike() {
							
							entry.getEntryInfoRealTime(new GagEntry.GetCallback() {
								public void OnGetCallBackInt(int loves) {
								}
	
								public void OnGetCallBackInfo(int loves, boolean isLiked) {
									m_LikeNumber.setText(String.valueOf(loves));
									btt_Like.setChecked(isLiked);
								}
							});
								
						}
					};
					
					if(btt.isChecked())
						entry.Like(ldk);
					else
						entry.UnLike(ldk);
				}else{
					Toast.makeText(getContext(), R.string.CannotUseLikeButton, Toast.LENGTH_SHORT).show();
					btt.setChecked(false);
				}
				
			}
			
        });
    
        //btt_Like.setBackgroundColor(Color.TRANSPARENT);
        m_LikeNumber = new TextView(this.getContext());
        m_LikeNumber.setGravity(Gravity.CENTER_HORIZONTAL|Gravity.CENTER_VERTICAL);
        m_LikeNumber.setText("--");
        m_LikeNumber.setTextColor(0xff888888);
        m_LikeNumber.setTextSize(20);
        temp3.addView(btt_Like);
        temp3.addView(m_LikeNumber,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        menuBot.addView(temp3,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        //Share Button
        ImageButton btt_Share = new ImageButton(this.getContext());
        btt_Share.setImageResource(R.drawable.button_share);
        btt_Share.setBackgroundColor(Color.TRANSPARENT);
        btt_Share.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				Intent share = new Intent(Intent.ACTION_SEND);
				share.setType("text/plain");
				GagEntry entry = getCurrentEntry();
				share.putExtra(Intent.EXTRA_SUBJECT, entry.getEntryName());
				share.putExtra(Intent.EXTRA_TEXT, entry.getEntryUrl());
				getContext().startActivity(Intent.createChooser(share, getContext().getString(R.string.ShareWith)));
			}        	
        });
        //Comment Button
        ImageButton btt_Comment = new ImageButton(this.getContext());
        btt_Comment.setImageResource(R.drawable.button_comment);
        btt_Comment.setBackgroundColor(Color.TRANSPARENT);
        btt_Comment.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				FBCmtDialog = new CommentDialog(getContext(), 
						getCurrentEntry().getFBCommentLink());
		    	int[] pos = {0, 0};
		    	v.getLocationOnScreen(pos);
		    	FBCmtDialog.setPosition(pos[0] + v.getWidth()/2, pos[1] + v.getHeight()/2);
		    	FBCmtDialog.show();
				
			}        	
        });
        LinearLayout temp2= new LinearLayout(this.getContext());
        temp2.addView(btt_Share,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        temp2.addView(btt_Comment,new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
       
        temp2.setOrientation(LinearLayout.HORIZONTAL);
        
        temp2.setGravity(Gravity.RIGHT);
        menuBot.addView(temp2,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
	}
	
	
	public void displayMenu(){
		GagEntry entry = getCurrentEntry();
		menu_on=true;		
		m_Title.setText(entry.getEntryName());
		m_LikeNumber.setText(entry.getLoveCount());
		btt_Like.setChecked(false);
		
		entry.getEntryInfoRealTime(new GagEntry.GetCallback() {
			public void OnGetCallBackInt(int loves) {
				//m_LikeNumber.setText(String.valueOf(loves));
			}

			public void OnGetCallBackInfo(int loves, boolean isLiked) {
				m_LikeNumber.setText(String.valueOf(loves));
				btt_Like.setChecked(isLiked);
			}
		});			
		((ViewGroup) this.getCurrentView()).addView(menuTop,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT,Gravity.TOP));
		((ViewGroup) this.getCurrentView()).addView(menuBot,new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT,Gravity.BOTTOM));
		menuTop.startAnimation(anim_InFromTop);
		menuBot.startAnimation(anim_InFromBot);
	}
	public void hideMenu(){
		menu_on=false;				
		ViewGroup currentMenu = ((ViewGroup) this.getCurrentView());
		if(currentMenu != null){
			if(currentMenu.indexOfChild(menuTop) != -1){
				menuTop.startAnimation(anim_OutToTop);
				((ViewGroup) this.getCurrentView()).removeView(menuTop);
			}
			if(currentMenu.indexOfChild(menuBot) != -1){
				menuBot.startAnimation(anim_OutToBot);
				((ViewGroup) this.getCurrentView()).removeView(menuBot);
			}
		}
	}
	
	private void addEntryToDownloadQueue(GagEntry entry){	
		if(!entry.isNSFW()){
			queue_Download.add(entry);
			if(queue_Download.size() == 1){
				queue_Download.poll();
				entry.StartDownloadBitmap();
			}
		}
	}

	public void Reset(){
		_nineGag.Reset(m_type);
		PublicResource.setPrefCurrentView(getContext(), m_type, 0);
		//Remove menu if exist
		hideMenu();
		removeAllViews();
		setUpLoadFirstEntry();
		NeedToRefresh = false;
	}
	
	private void setUpLoadFirstEntry(){
		_nineGag.setLoadFirstEntriesFinished(new NineGAG.LoadFirstEntriesFinishedListener() {
			
			public void OnLoadFirstEntriesFinished() {
				for(int i = 0; i < MaxFirstImage; i++){
					addNewView(_nineGag.getList(m_type).get(i), true);					
				}		
			}
		});		
		_nineGag.StartDownloadFirstPage(m_type);
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
		List<GagEntry> l_entry = nineGag.getList(m_type);
		if(l_entry.size() == 0){		
			setUpLoadFirstEntry();
		}else{		
			int iCurrentView = PublicResource.getPrefCurrentView(ct, m_type);
			//Simulate Next
			for(int i = 1; i <= iCurrentView; i++){//Notice i = 1
				nineGag.Next(m_type);
			}
			//Load View Without adding to queue download
			for(int i = 0; i <= iCurrentView - MaxLoadAhead - 1; i++){
				addNewView(l_entry.get(i), false);				
			}
			//Load View
			if(iCurrentView - MaxLoadAhead >= 0){
				for(int i = iCurrentView - MaxLoadAhead; i <= iCurrentView + MaxLoadAhead; i++)
					addNewView(l_entry.get(i), true);
			}else{
				for(int i = 0; i < MaxFirstImage; i++)
					addNewView(l_entry.get(i), true);
			}
			setDisplayedChild(iCurrentView);
		}
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
		anim_FadeIn = AnimationUtils.loadAnimation(ct, R.anim.fadein);
		anim_FadeOut = AnimationUtils.loadAnimation(ct, R.anim.fadeout);
	}
	
	public void goNext(){		
		int iChildCount = getChildCount(), iDisplayedChild = getDisplayedChild(); 
		
		//Add newView if necessary
		if(iDisplayedChild + MaxLoadAhead + 1 == iChildCount){			
			_nineGag.Next(m_type);//Next as rule
			addNewView(_nineGag.getList(m_type).get(iChildCount), true);
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

	public void DisposeAllDialog(){
		if(FBCmtDialog != null && FBCmtDialog.isShowing())
			FBCmtDialog.dismiss();
		
		//Dispose Downloading Image
		GagEntry currentEntry = getCurrentEntry();
		if(currentEntry != null) getCurrentEntry().DisposeImage();
	}
	
}
