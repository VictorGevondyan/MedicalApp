package com.implementhit.OptimizeHIT.util;

import com.implementhit.OptimizeHIT.activity.SuperActivity;

import android.content.Context;
import android.view.MotionEvent;
import android.webkit.WebView;

public class CustomWebView extends WebView{

	public CustomWebView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return false;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();
		
		performClick();
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean performClick() {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return false;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();
		
		return super.performClick();
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		return false;
	}
	

}
