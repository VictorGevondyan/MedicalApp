package com.implementhit.OptimizeHIT.layouts;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class SlidingRelativeLayout extends RelativeLayout {
	public SlidingRelativeLayout(Context context) {
		super(context);
	}

	public SlidingRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SlidingRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public float getXFraction() {
		Point size = new Point();
	    final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
	    wm.getDefaultDisplay().getSize(size);
	    int width = size.x;
	    return (width == 0) ? 0 : getX() / (float) width;
	}

	public void setXFraction(float xFraction) {
		Point size = new Point();
	    final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
	    wm.getDefaultDisplay().getSize(size);
	    int width = size.x;
	    setX((width > 0) ? (xFraction * width) : 0);
	}
}
