package com.implementhit.OptimizeHIT.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class LoadingDialog extends Dialog {
    // Forces Default color for ONLY ONE TIME
    private boolean forceDefaultColor;

    public LoadingDialog(Context context) {
        super(context);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void  setTitle(int title) {
        TextView titleTextView = (TextView) findViewById(R.id.loading_dialog_text);
        titleTextView.setText(title);
    }

    public void setTitle(String title) {
        TextView titleTextView = (TextView) findViewById(R.id.loading_dialog_text);
        titleTextView.setText(title);
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.dialog_loading);
	}

    @Override
    protected void onStart() {
        super.onStart();

        setLoadingSpinnerColor();
    }

    @Override
    protected void onStop() {
        super.onStop();

        forceDefaultColor = false;
    }

    private void setLoadingSpinnerColor() {
        MaterialProgressBar materialProgressBar = (MaterialProgressBar) findViewById(R.id.loading_spinner);

        if (forceDefaultColor) {
            materialProgressBar.setProgressTintList(ColorUtil.colorStateListForColor(getContext().getResources().getColor(R.color.orange)));
        } else {
            materialProgressBar.setProgressTintList(ColorUtil.colorStateListForColor(User.sharedUser(getContext()).primaryColor()));
        }
    }

    public void setForceDefaultColor(boolean forceDefaultColor) {
        this.forceDefaultColor = forceDefaultColor;

        setLoadingSpinnerColor();
    }
}