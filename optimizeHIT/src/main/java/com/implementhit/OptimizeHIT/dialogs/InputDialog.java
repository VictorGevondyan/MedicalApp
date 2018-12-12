package com.implementhit.OptimizeHIT.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.util.CustomEditText;

public class InputDialog extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "descruption";
	public static final String OK_BUTTON = "ok_button";
	public static final String INVALID_INPUT = "invalidInput";
	public static final String INVALID_MIN = "invalidMin";
	public static final String ERROR_DESCRIPTION = "errorDescription";
	public static final String ERROR_DEFAULT = "errorDefault";
	public static final String ERROR_AS_HINT = "errorAsHint";
	
	private final String ERROR_IS_SHOWN = "errorIsShown";
	
	private View view;
	CustomEditText inputEditText;

	private Activity activity;
	private Toast toast;
	private InputDialogHandler handler;
	private String invalidInput;
	private String errorDescription;
	private String errorDefault;
	private boolean errorAsHint;
	private boolean errorIsShown;
	private int invalidMin = 5;
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.dialog_feedback, container, false);

    	Button cancel = (Button) view.findViewById(R.id.cancel);
    	cancel.setOnClickListener(this);
    	Button send = (Button) view.findViewById(R.id.send);
    	send.setOnClickListener(this);
		inputEditText = (CustomEditText) view.findViewById(R.id.feedback);
		inputEditText.requestFocus();
		inputEditText.setOnEditorActionListener(this);
		ScrollView scrollView  = (ScrollView) view.findViewById(R.id.scrollbar);
		scrollView.setVerticalScrollBarEnabled(false);
		
		if (savedInstanceState != null) {
			errorIsShown = savedInstanceState.getBoolean(ERROR_IS_SHOWN, false);
		}
		
		if (getArguments() != null) {
			String okButton = getArguments().getString(OK_BUTTON);
			String title = getArguments().getString(TITLE);
			String description = getArguments().getString(DESCRIPTION);
			invalidInput = getArguments().getString(INVALID_INPUT);
			invalidMin = getArguments().getInt(INVALID_MIN, 5);
			errorDescription = getArguments().getString(ERROR_DESCRIPTION);
			errorDefault = getArguments().getString(ERROR_DEFAULT);
			errorAsHint = getArguments().getBoolean(ERROR_AS_HINT);
			
			if (okButton != null) {
				send.setText(okButton);
			}
			
			TextView titleTextView = (TextView) view.findViewById(R.id.title);

			if (title != null && !title.isEmpty()) {
				titleTextView.setText(title);
			} else {
				titleTextView.setVisibility(View.GONE);
			}
			
			TextView descriptioTextView = (TextView) view.findViewById(R.id.text);

			if (description != null && !description.isEmpty()) {
				descriptioTextView.setText(description);
			} else {
				descriptioTextView.setVisibility(View.GONE);
			}
			
			if (errorIsShown) {
				showError();
			}
		}

		return view;
    }
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(ERROR_IS_SHOWN, errorIsShown);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}
	
	@Override
	public void onPause() {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);
        
		super.onPause();
	}
	
	@Override
	public void onResume() {
		inputEditText.requestFocus();
		super.onResume();
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(false);
		return dialog;
	}
	
	private void send() {

		if (handler != null) {
			String feedback = inputEditText.getText().toString().trim();
			
			if (feedback.length() < invalidMin) {
				showError();
				
				return;
			}
			
			handler.onInputSubmitted(feedback);
		}
		
		dismiss();

	}
	
	public void showError() {
		if (errorDescription != null) {
			TextView textView = (TextView) view.findViewById(R.id.error);
			textView.setText(errorDescription);
			textView.setVisibility(View.VISIBLE);
		} else if (invalidInput != null) {
			displayToast(invalidInput);
		}
		
		if (errorDefault != null) {
			if (errorAsHint) {
				inputEditText.setHint(errorDefault);
			} else {
				inputEditText.setText(errorDefault);
			}
		}
		
		errorIsShown = true;
	}
	
	/**
	 * OnClickListener Methods
	 */

	@Override
	public void onClick(View view) {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if (view.getId() == R.id.cancel) {
			closeDialog();
		} else {
			send();
		}
	}

	public interface InputDialogHandler {
		void onInputSubmitted(String input);
		void onInputCanceled();
	}

	public void displayToast(String message) {
	    if(toast != null) {
	        toast.cancel();
	    }
	    
	    toast = Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT);
	    toast.show();
	}	
	
	public void setHandler(InputDialogHandler handler) {
		this.handler = handler;
	}
	
	private void closeDialog() {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0);

		handler.onInputCanceled();
		dismiss();
	}

	/**
	 * OnEditorActionListener Methods
	 */

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			send();
			return true;
		}

		return false;
	}

}