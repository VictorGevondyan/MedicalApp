package com.implementhit.OptimizeHIT.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;

public class FeedbackMenuDialog extends ProgressDialog implements android.view.View.OnClickListener {
	FeedbackMenuDialogHandler handler;
	Context context;
	String token;
	
	public FeedbackMenuDialog(Context context, FeedbackMenuDialogHandler handler) {
		super(context);
		
		this.context = context;
		this.handler = handler;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.dialog_feedback_menu);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    	setCancelable(true);
		setCanceledOnTouchOutside(false);

    	Button cancel = (Button) findViewById(R.id.cancel);
    	cancel.setOnClickListener(this);
    	Button wrongCategory = (Button) findViewById(R.id.solution_wrong_category);
    	wrongCategory.setOnClickListener(this);
    	Button inaccurate = (Button) findViewById(R.id.inaccurate);
    	inaccurate.setOnClickListener(this);
    	Button langPres = (Button) findViewById(R.id.lang_pres);
    	langPres.setOnClickListener(this);
    	Button other = (Button) findViewById(R.id.other);
    	other.setOnClickListener(this);
    }
	
	/*
	 * OnClickListener Methods
	 */

	@Override
	public void onClick(View view) {
		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}
		
		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if (view.getId() == R.id.cancel) {
			handler.feedbackMenuDismiss();
			dismiss();
		} else if (view.getId() == R.id.inaccurate) {
			handler.sendInstantFeedback(context.getResources().getString(R.string.solution_inaccurate));
			dismiss();
		} else if (view.getId() == R.id.solution_wrong_category) {
			handler.sendInstantFeedback(context.getResources().getString(R.string.solution_wrong_category));
			dismiss();
		} else if (view.getId() == R.id.lang_pres) {
			handler.sendInstantFeedback(context.getResources().getString(R.string.lang_pres_issue));
			dismiss();
		} else if (view.getId() == R.id.other) {
			handler.sendFeedback();
			dismiss();
		}
	}
	
	public interface FeedbackMenuDialogHandler {
		void sendInstantFeedback(String feedback);
		void sendFeedback();
		void feedbackMenuDismiss();
	}
}