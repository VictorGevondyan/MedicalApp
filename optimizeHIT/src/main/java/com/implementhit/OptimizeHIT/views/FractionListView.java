package com.implementhit.OptimizeHIT.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.implementhit.OptimizeHIT.util.DeviceUtil;

public class FractionListView extends ListView {
	private float prevY;
	private float touchSlop = 2;
	private int totalItemCount;
	private boolean isScrollable = true;
	private boolean isDisabled = false;

	public FractionListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public float getXFraction() {
		Point size = new Point();
	    final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
	    wm.getDefaultDisplay().getSize(size);
	    int width = size.x;
	    return (width == 0) ? 0 : getX() / (float) width;
	}

//	public void setXFraction(float xFraction) {
//		Point size = new Point();
//	    final WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
//	    wm.getDefaultDisplay().getSize(size);
//	    int width = size.x;
//	    setX((width > 0) ? (xFraction * width) : 0);
//	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
    public boolean onTouchEvent(MotionEvent ev) {
		if (isDisabled){
			return false;
		}
		if (!isScrollable) {
			return super.onTouchEvent(ev);
		}

        switch (ev.getActionMasked()) {
        	case MotionEvent.ACTION_MOVE:
				try {
					int firstVisibleItem = getFirstVisiblePosition();
					int lastVisibleItemCount = getLastVisiblePosition();
					boolean onTop = firstVisibleItem == 0 && this.getChildAt(0) != null && this.getChildAt(0).getTop() == 0;
					boolean onBottom = lastVisibleItemCount + 1 == totalItemCount && this.getChildAt(lastVisibleItemCount - firstVisibleItem).getBottom() == this.getHeight();

					if (!(prevY - ev.getY() > touchSlop && onBottom)
							&& !(ev.getY() - prevY > touchSlop && onTop)) {
						getParent().requestDisallowInterceptTouchEvent(true);
					}
				} catch (Exception e) {}

                break;

            case MotionEvent.ACTION_DOWN:
                prevY = ev.getY();
                break;

        }

        return super.onTouchEvent(ev);
    }

	public void setIsScrollable(boolean isScrollable) {
		this.isScrollable = isScrollable;
	}

	public void setTotalItemCount(int totalItemCount) {
		this.totalItemCount = totalItemCount;
	}

	public static void setListViewHeightBasedOnChildren( ListView listView ) {

	    ListAdapter listAdapter = listView.getAdapter();

	    if (listAdapter == null) {
			return;
		}

	    int desiredWidth = DeviceUtil.getScreenSize(listView.getContext()).x;
	    int totalHeight = 0;
	    View view = null;
	    for (int i = 0; i < listAdapter.getCount(); i++) {

	        view = listAdapter.getView(i, null, listView);

	        if (i == 0) {
	            view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, LayoutParams.WRAP_CONTENT));
	        }

	        view.measure(
					MeasureSpec.makeMeasureSpec(desiredWidth, MeasureSpec.EXACTLY),
					MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, MeasureSpec.UNSPECIFIED));

	        totalHeight += view.getMeasuredHeight();
	    }

	    ViewGroup.LayoutParams params = listView.getLayoutParams();
	    params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	    listView.setLayoutParams(params);
	    listView.requestLayout();
	}
}
