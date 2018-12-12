package com.implementhit.OptimizeHIT.views;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.ImageView;

import com.implementhit.OptimizeHIT.util.DeviceUtil;

/**
 * Created on 12/7/16 __ Schumakher .
 */

public class PoweredByImageView extends ImageView {
    private int height;
    private int width;

    public PoweredByImageView(Context context) {
        super(context);
        init();
    }

    public PoweredByImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PoweredByImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PoweredByImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(metrics);

        height = metrics.heightPixels - (int)DeviceUtil.getPxForDp(getContext(), 30);
        width = (int)DeviceUtil.getPxForDp(getContext(), 110);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                width,
                height);
    }
}