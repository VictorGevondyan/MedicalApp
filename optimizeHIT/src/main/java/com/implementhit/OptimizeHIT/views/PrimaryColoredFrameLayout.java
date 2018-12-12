package com.implementhit.OptimizeHIT.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.implementhit.OptimizeHIT.models.User;

/**
 * Created by acerkinght on 2/8/17.
 */

public class PrimaryColoredFrameLayout extends FrameLayout {
    public PrimaryColoredFrameLayout(Context context) {
        super(context);
        init();
    }

    public PrimaryColoredFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PrimaryColoredFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PrimaryColoredFrameLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setBackgroundColor(User.sharedUser(getContext()).primaryColor());
    }
}