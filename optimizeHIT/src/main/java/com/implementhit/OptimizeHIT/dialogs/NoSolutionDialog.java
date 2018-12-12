package com.implementhit.OptimizeHIT.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;

public class NoSolutionDialog extends ProgressDialog implements android.view.View.OnClickListener {
	NoSolutionDialogHandler handler;
	Context context;
	String token;
	
	public NoSolutionDialog(Context context, String token, NoSolutionDialogHandler handler) {
		super(context);
		
		this.context = context;
		this.handler = handler;
		this.token = token;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.dialog_no_voice_solution);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    	setCancelable(true);
		setCanceledOnTouchOutside(false);

    	Button cancel = (Button) findViewById(R.id.cancel);
    	cancel.setOnClickListener(this);
    	Button tryAgain = (Button) findViewById(R.id.try_again);
    	tryAgain.setOnClickListener(this);
    	Button browseSolutions = (Button) findViewById(R.id.browse_for_solutions);
    	browseSolutions.setOnClickListener(this);
    	
    	TextView title = (TextView) findViewById(R.id.title);
    	title.setText(context.getString(R.string.no_result_found).replace("TOKEN", token));
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
			dismiss();
			handler.onDismiss();
		} else if (view.getId() == R.id.try_again) {
			dismiss();
			handler.tryAgain();
		} else if (view.getId() == R.id.browse_for_solutions) {
			dismiss();
			handler.browse();
		}
	}
	
	public interface NoSolutionDialogHandler {
		void tryAgain();
		void browse();
		void onDismiss();
	}
}