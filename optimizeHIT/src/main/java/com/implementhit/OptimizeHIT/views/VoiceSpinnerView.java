package com.implementhit.OptimizeHIT.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.View;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.util.DimensionUtil;

/**
 * Created by acerkinght on 8/4/16.
 */
public class VoiceSpinnerView extends View {
    private Paint arcPaint;
    private RectF ovalRect;

    private int arcStart;
    private int arcSweep;

    public VoiceSpinnerView(Context context) {
        super(context);
        init();
    }

    public VoiceSpinnerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VoiceSpinnerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        arcPaint = new Paint();
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(DimensionUtil.dpToPx(6, getResources()));
        arcPaint.setColor(getResources().getColor(R.color.background_white));
        arcPaint.setAlpha(80);

        ovalRect = new RectF();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        ovalRect.set(
                arcPaint.getStrokeWidth() / 2, arcPaint.getStrokeWidth() / 2,
                getWidth() - arcPaint.getStrokeWidth() / 2, getHeight() - arcPaint.getStrokeWidth() / 2);
        canvas.drawArc(ovalRect, arcStart, arcSweep, false, arcPaint);
    }

    public void setArcSweep(int arcSweep) {
        if (arcSweep < 2) {
            arcSweep = 2;
        }

        this.arcSweep = arcSweep;
    }

    public void setArcStart(int arcStart) {
        this.arcStart = arcStart;
    }
}
