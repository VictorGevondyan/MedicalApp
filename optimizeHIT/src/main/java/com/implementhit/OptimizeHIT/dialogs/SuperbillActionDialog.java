package com.implementhit.OptimizeHIT.dialogs;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;

public class SuperbillActionDialog extends ProgressDialog implements android.view.View.OnClickListener {

	public static final String ACTION_CANCEL = "actionCancel";
	public static final String ACTION_REMOVE = "actionRemove";
	public static final String ACTION_EXPLORE = "actionExplore";
	
	private boolean canRemove = true;
	private String actionTitle = null;
	
	Context context;
	SuperbillActionListener handler;
	String subtitle;
	TextView subtitleTextView;

	public SuperbillActionDialog(Context context, SuperbillActionListener handler) {
		super(context);
		
		this.context = context;
		this.handler = handler;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.dialog_superbill_action);

		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

    	setCancelable(true);
		setCanceledOnTouchOutside(false);

    	Button cancel = (Button) findViewById(R.id.cancel);
    	cancel.setOnClickListener(this);
    	Button explore = (Button) findViewById(R.id.more_info);
    	explore.setOnClickListener(this);

		subtitleTextView = (TextView) findViewById(R.id.subtitle);
		subtitleTextView.setText(subtitle);

    	if (canRemove) {
        	Button removeSuperbill = (Button) findViewById(R.id.remove_superbill);
        	removeSuperbill.setOnClickListener(this);
    	} else {
    		findViewById(R.id.remove_superbill).setVisibility(View.GONE);
    		findViewById(R.id.remove_superbill_divider).setVisibility(View.GONE);
    	}

    	if (actionTitle != null && !actionTitle.isEmpty()) {
	    	Button additionalAction = (Button) findViewById(R.id.superbill_action);
	    	additionalAction.setText(actionTitle);
	    	additionalAction.setOnClickListener(this);
    	} else {
    		findViewById(R.id.superbill_action).setVisibility(View.GONE);
    		//findViewById(R.id.superbill_action_divider).setVisibility(View.GONE);
			findViewById(R.id.remove_superbill_divider).setVisibility(View.GONE);
    	}
    }
	
	public void setAdditionalAction(String actionTitle) {
		this.actionTitle = actionTitle;
	}
	
	public String getAdditionalAction() {
		return actionTitle;
	}
	
	public void canRemove(boolean canRemove) {
		this.canRemove = canRemove;
	}
	
	public boolean getCanRemove() {
		return canRemove;
	}

	public void setSubtitle( String subtitle ){
		this.subtitle = subtitle;
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
			handler.onSuperbillDialogAction(ACTION_CANCEL);
		} else if (view.getId() == R.id.more_info) {
			handler.onSuperbillDialogAction(ACTION_EXPLORE);
		}
 		else if (view.getId() == R.id.remove_superbill) {
			handler.onSuperbillDialogAction(ACTION_REMOVE);

			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.REMOVE_FROM_SUPERBILL.CATEGORY,
					GAnalitycsEventNames.REMOVE_FROM_SUPERBILL.ACTION,
					GAnalitycsEventNames.REMOVE_FROM_SUPERBILL.LABEL);

		}
 		else if (view.getId() == R.id.superbill_action) {
			handler.onSuperbillDialogAction(actionTitle);
		}
		
		dismiss();
	}
	
	public interface SuperbillActionListener {
		void onSuperbillDialogAction(String action);
	}

}