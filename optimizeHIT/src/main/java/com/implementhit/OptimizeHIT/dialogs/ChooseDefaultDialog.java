package com.implementhit.OptimizeHIT.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;

public class ChooseDefaultDialog extends AlertDialog implements android.view.View.OnClickListener {
	ChooseDefaultsHandler handler;
	Context context;
	
	public ChooseDefaultDialog(Context context, ChooseDefaultsHandler handler) {
		super(context);
		
		this.context = context;
		this.handler = handler;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    	super.onCreate(savedInstanceState);

    	setContentView(R.layout.dialog_choose_default);

    	setCancelable(true);
		setCanceledOnTouchOutside(false);

    	Button dashboard = (Button) findViewById(R.id.dashboard);
    	dashboard.setOnClickListener(this);
    	Button library = (Button) findViewById(R.id.library);
		library.setOnClickListener(this);
		Button optiquery = (Button) findViewById(R.id.opti_query);
		optiquery.setOnClickListener(this);

		Button cancel = (Button) findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

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

		if (view.getId() == R.id.dashboard) {
			handler.selectedDefault(0);
		} else if (view.getId() == R.id.library) {
			handler.selectedDefault(1);
		} else if (view.getId() == R.id.opti_query) {
			handler.selectedDefault(2);
		}
		
		handler.dismiss();
		dismiss();
	}
	
	public interface ChooseDefaultsHandler {
		void selectedDefault(int defaultScreen);
		void dismiss();
	}
}