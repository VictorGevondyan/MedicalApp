package com.implementhit.OptimizeHIT.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.util.FontsHelper;

public class YesNoDialog  extends DialogFragment implements android.view.View.OnClickListener {

	public static final String ACTION_NO = "no";
	public static final String ACTION_YES = "yes";
    public static final String ACTION_MORE_INFO = "moreInfo";
	
	private YesNoDialogListener handler;
	
	private final String TITLE = "title";
	private final String MESSAGE = "message";
	private final String TITLE_STRING = "titleString";
	private final String MESSAGE_STRING = "messageString";
	
	private View view;
	private int title;
	private int message;
	private int cancel = -1;
	
	private String titleString;
	private String messageString;
	
	public void setupDialog(int title, int message) {
		this.title = title;
		this.message = message;
		this.titleString = null;
		this.messageString = null;
	}
	
	public void setupDialog(int title, int message, int cancel) {
		setupDialog(title, message);
		this.cancel = cancel;
	}
	
	public void setupDialog(String title, String message) {
		this.titleString = title;
		this.messageString = message;
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(MESSAGE, message);
		outState.putInt(TITLE, title);
		outState.putInt(ACTION_NO, cancel);
		outState.putString(MESSAGE_STRING, messageString);
		outState.putString(TITLE_STRING, titleString);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.dialog_question_yes_no, container, false);
		
		if (savedInstanceState != null) {
			title = savedInstanceState.getInt(TITLE);
			message = savedInstanceState.getInt(MESSAGE);
			cancel = savedInstanceState.getInt(ACTION_NO);
			messageString = savedInstanceState.getString(MESSAGE_STRING);
			titleString = savedInstanceState.getString(TITLE_STRING);
		}

		TextView iconTextView = (TextView) view.findViewById(R.id.icon_question);
		iconTextView.setTypeface(FontsHelper.sharedHelper(getActivity()).fontello());
    	
    	Button yes = (Button) view.findViewById(R.id.yes);
    	yes.setOnClickListener(this);
    	Button no = (Button) view.findViewById(R.id.no);
    	no.setOnClickListener(this);
    	
    	if (cancel > 0) {
    		no.setText(cancel);
    	}
    	
    	TextView titleView = (TextView) view.findViewById(R.id.title);
    	TextView messageView = (TextView) view.findViewById(R.id.message);
    	
    	if (titleString != null) {

			// if the dialog is a notification about list item in FindTheCode or ExploreICDActivity being billable,
			// we must change dialog buttons text
			if( titleString.equals( getString(R.string.title_billable_code) ) ){

				iconTextView.setText( R.string.icon_dollar_circled );
				iconTextView.setTextColor( getResources().getColor( R.color.text_black_grey ) );

				yes.setText( R.string.more_info );
				no.setText( R.string.ok );

			}

    		titleView.setText(titleString);

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
		return dialog;
	}

	@Override
	public void onClick(View view) {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if (view.getId() == R.id.no) {
			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			dismiss();
			
			if (handler != null) {
				handler.onDialogAction(ACTION_NO);
			}
		} else if (view.getId() == R.id.yes) {

			getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
			dismiss();
			
			if (handler != null) {

                if( titleString != null && titleString.equals( getString( R.string.title_billable_code ) )  ){
                    handler.onDialogAction(ACTION_MORE_INFO);
                } else {
                    handler.onDialogAction(ACTION_YES);
                }

			}

		}

	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		if (activity instanceof YesNoDialogListener) {
			this.handler = (YesNoDialogListener) activity;
		}
	}
	
	public interface YesNoDialogListener {
		void onDialogAction(String actionCode);
	}
	
	public void setHandler(YesNoDialogListener handler) {
		this.handler = handler;
	}

}