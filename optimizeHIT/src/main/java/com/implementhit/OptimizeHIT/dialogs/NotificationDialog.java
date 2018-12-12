package com.implementhit.OptimizeHIT.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;

public class NotificationDialog  extends DialogFragment implements android.view.View.OnClickListener {

	private final String TITLE = "title";
	private final String MESSAGE = "message";
	private final String TITLE_STRING = "titleString";
	private final String MESSAGE_STRING = "messageString";
	private final String IS_ERROR = "isError";
	private final String FORCE_DEFAULT_COLOR = "forceDefaultColor";

	private View view;
	private int title;
	private int message;
	private boolean isError;
	private boolean forceDefaultColor;

	private String titleString;
	private String messageString;

	public void setupDialog(int title, int message) {
		this.title = title;
		this.message = message;
		this.titleString = null;
		this.messageString = null;
	}
	
	public void setupDialog(String title, String message) {
		this.titleString = title;
		this.messageString = message;
	}

	public void setIsError(boolean isError) {
		this.isError = isError;
	}

	public void setForceDefaultColor(boolean forceDefaultColor) {
		this.forceDefaultColor = forceDefaultColor;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(MESSAGE, message);
		outState.putInt(TITLE, title);
		outState.putString(MESSAGE_STRING, messageString);
		outState.putString(TITLE_STRING, titleString);
		outState.putBoolean(IS_ERROR, isError);
		outState.putBoolean(FORCE_DEFAULT_COLOR, forceDefaultColor);

		super.onSaveInstanceState(outState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.dialog_notification, container, false);
		
		if (savedInstanceState != null) {
			title = savedInstanceState.getInt(TITLE);
			message = savedInstanceState.getInt(MESSAGE);
			messageString = savedInstanceState.getString(MESSAGE_STRING);
			titleString = savedInstanceState.getString(TITLE_STRING);
			isError = savedInstanceState.getBoolean(IS_ERROR);
			forceDefaultColor = savedInstanceState.getBoolean(FORCE_DEFAULT_COLOR);
		}

		Button okButton = (Button) view.findViewById(R.id.ok);
		okButton.setOnClickListener(this);

		if (forceDefaultColor) {
			okButton.setBackground(ColorUtil.getTintedDrawable(okButton.getContext(), R.drawable.rounded_rect_orange, getResources().getColor(R.color.orange)));
		} else {
			okButton.setBackground(ColorUtil.getTintedDrawable(okButton.getContext(), R.drawable.rounded_rect_orange, User.sharedUser(getActivity()).primaryColor()));
		}

		TextView titleView = (TextView) view.findViewById(R.id.title);
    	TextView messageView = (TextView) view.findViewById(R.id.message);
		TextView iconTextView = (TextView) view.findViewById(R.id.icon);

		iconTextView.setTypeface(FontsHelper.sharedHelper(getActivity()).fontello());

		if (isError) {
			iconTextView.setTextColor(getResources().getColor(R.color.red));
			iconTextView.setText(R.string.icon_dismiss_bold);
		} else {
			iconTextView.setTextColor(getResources().getColor(R.color.text_icon_blue));
			iconTextView.setText(R.string.icon_info);
		}

        if (titleString != null) {
    		titleView.setText(titleString);
    		
    		if (titleString.isEmpty()) {
    			titleView.setVisibility(View.GONE);
    		}
    	} else {
    		titleView.setText(title);
    	}
    	
    	if (messageString != null) {
    		messageView.setText(messageString);
    	} else {
    		messageView.setText(message);
    	}

		return view;

    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);

//		if (isError) {
//			dialog.getWindow().getAttributes().windowAnimations = R.style.Dialog_ErrorAnimation;
//		} else {
//			dialog.getWindow().getAttributes().windowAnimations = R.style.Dialog_NotificationAnimation;
//		}

		return dialog;
	}

	@Override
	public void onClick(View view) {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if ( view.getId() == R.id.ok ) {
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

			((SuperActivity) getActivity()).error = null;
			((SuperActivity) getActivity()).message = null;

			SuperActivity.IS_LIBRARY_OFFLINE_DIALOG_SHOWN = false;
			dismiss();
		}
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		((SuperActivity) getActivity()).error = null;
		((SuperActivity) getActivity()).message = null;

		super.onCancel(dialog);
	}

}





























