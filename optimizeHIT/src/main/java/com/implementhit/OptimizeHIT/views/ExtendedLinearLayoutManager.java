package com.implementhit.OptimizeHIT.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by anhaytananun on 11.06.16.
 */
public class ExtendedLinearLayoutManager extends LinearLayoutManager {
    private VerticalScrollListener verticalScrollListener;
    private int y;

    public ExtendedLinearLayoutManager(Context context) {
        super(context);
    }

    public ExtendedLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public ExtendedLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int y = super.scrollVerticallyBy(dy, recycler, state);

        verticalScrollListener.onScrolledBy(dy);

        return y;
    }

    public void setVerticalScrollListener(VerticalScrollListener verticalScrollListener) {
        this.verticalScrollListener = verticalScrollListener;
    }

    public interface VerticalScrollListener {
        boolean onScrolledBy(int dy);
    }
}
