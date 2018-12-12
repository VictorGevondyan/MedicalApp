package com.implementhit.OptimizeHIT.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.SwitchCompat;
import android.widget.SeekBar;

/**
 * Created by acerkinght on 2/8/17.
 */

public class ColorUtil {
    public static ColorStateList colorStateListForColor(int color) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_enabled},
                new int[] {-android.R.attr.state_checked},
                new int[] { android.R.attr.state_pressed}
        };

        int[] colors = new int[] {
                color,
                color,
                color,
                color
        };

        return new ColorStateList(states, colors);
    }

    public static Drawable getTintedDrawable(@NonNull Context context, @DrawableRes int inputDrawable, @ColorInt int color) {
        return getTintedDrawable(context.getResources().getDrawable(inputDrawable), color);
    }

    public static Drawable getTintedDrawable(@NonNull Drawable inputDrawable, @ColorInt int color) {
        Drawable wrapDrawable = DrawableCompat.wrap(inputDrawable);
        DrawableCompat.setTint(wrapDrawable, color);
        DrawableCompat.setTintMode(wrapDrawable, PorterDuff.Mode.SRC_IN);
        return wrapDrawable;
    }

    public static void changeSwitchColor(@NonNull SwitchCompat switchCompat, int color) {
        int[][] states = new int[][] {
                new int[] { -android.R.attr.state_checked},
                new int[] { android.R.attr.state_checked}
        };

        int[] trackColors = new int[] {
                getShiftedColor(color, 2.0f),
                color
        };

        int[] thumbColors = new int[] {
                getShiftedColor(color, 2.0f),
                color
        };

        ColorStateList trackColorStateList = new ColorStateList(states, trackColors);
        ColorStateList thumbColorStateList = new ColorStateList(states, thumbColors);
        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getThumbDrawable()), thumbColorStateList);
        DrawableCompat.setTintList(DrawableCompat.wrap(switchCompat.getTrackDrawable()), trackColorStateList);
    }

    public static void changeSeekBarColor(@NonNull SeekBar seekBar, int color) {
        int[][] states = new int[][] {
                new int[] { android.R.attr.state_checked},
                new int[] { android.R.attr.state_activated},
                new int[] { android.R.attr.state_active},
                new int[] { android.R.attr.state_enabled},
                new int[] { android.R.attr.state_selected},
                new int[] { android.R.attr.state_single}
        };

        int[] colors = new int[] {
                color,
                color,
                color,
                color,
                color,
                color
        };

        ColorStateList colorStateList = new ColorStateList(states, colors);
        DrawableCompat.setTintList(DrawableCompat.wrap(seekBar.getProgressDrawable()), colorStateList);
    }

    public static void changeRatingBarColor(@NonNull AppCompatRatingBar ratingBar, int color) {
        int lightColor = getShiftedColor(color, 2.0f);
        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();
        stars.getDrawable(0).setColorFilter(lightColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(1).setColorFilter(lightColor, PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    }

    public static int getShiftedColor(int color, float coefficient) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= coefficient; // value component
        return Color.HSVToColor(hsv);
    }
}
