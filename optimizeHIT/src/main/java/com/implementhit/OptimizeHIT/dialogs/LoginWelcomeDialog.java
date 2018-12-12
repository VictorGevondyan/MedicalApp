package com.implementhit.OptimizeHIT.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;

import mbanje.kurt.fabbutton.FabButton;
import mbanje.kurt.fabbutton.FabUtil;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class LoginWelcomeDialog extends Dialog {
    // Forces Default color for ONLY ONE TIME
    private boolean forceDefaultColor;

    public LoginWelcomeDialog(Context context) {
        super(context);

        setCancelable(false);
        setCanceledOnTouchOutside(false);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable ( new ColorDrawable(Color.TRANSPARENT) );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login_welcome);
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

    public void  setTitle(int title) {
        TextView titleTextView = (TextView) findViewById(R.id.loading_dialog_text);
        titleTextView.setText(title);
    }

    public void setTitle(String title) {
        TextView titleTextView = (TextView) findViewById(R.id.loading_dialog_text);
        titleTextView.setText(title);
    }

    public void processWelcome(String firstName, String lastName) {
        FabButton loadingIndeterminate = (FabButton) findViewById(R.id.indeterminate);
        FabButton loadingDeterminate = (FabButton) findViewById(R.id.determinate);

        loadingIndeterminate.setVisibility(View.GONE);
        loadingDeterminate.setVisibility(View.VISIBLE);

        String stringBuilder = getContext().getResources().getString(R.string.welcome) +
                " " +
                firstName +
                " " +
                lastName;

        TextView titleTextView = (TextView) findViewById(R.id.loading_dialog_text);
        titleTextView.setText(stringBuilder);
    }

    public void startIndeterminate() {
        FabButton button = (FabButton) findViewById(R.id.indeterminate);
        button.showProgress(true);
    }

    public void setForceDefaultColor(boolean forceDefaultColor) {
        this.forceDefaultColor = forceDefaultColor;

        setLoadingSpinnerColor();
    }

    private void setLoadingSpinnerColor() {
        FabButton loadingIndeterminate = (FabButton) findViewById(R.id.indeterminate);
        FabButton loadingDeterminate = (FabButton) findViewById(R.id.determinate);

        int primaryColor = User.sharedUser(getContext()).primaryColor();
        int orangeColor = getContext().getResources().getColor(R.color.orange);

        if (forceDefaultColor) {
            loadingIndeterminate.setProgressColor(orangeColor);
            loadingDeterminate.setProgressColor(orangeColor);
        } else {
            loadingIndeterminate.setProgressColor(primaryColor);
            loadingDeterminate.setProgressColor(primaryColor);
        }
    }
}