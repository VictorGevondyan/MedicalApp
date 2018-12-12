package com.implementhit.OptimizeHIT.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;
import android.view.View;

import static me.zhanghai.android.materialprogressbar.R.styleable.View;

/**
 * Created by victor on 7/26/16.
 */
public class DimensionUtil {
    public static float dpToPx(float dp, Resources resources) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static void changeBackgroundPreservingPadding(View view, Context context, int drawable, int color) {
        int bottom = view.getPaddingBottom();
        int top = view.getPaddingTop();
        int right = view.getPaddingRight();
        int left = view.getPaddingLeft();
        view.setBackground(ColorUtil.getTintedDrawable(context, drawable, color));
        view.setPadding(left, top, right, bottom);
    }
}
