package com.implementhit.OptimizeHIT.util;

import android.view.View;
import android.view.View.OnClickListener;

public class ClearButtonClickListener implements OnClickListener{
	CustomEditText customEditText;
	
	public ClearButtonClickListener( CustomEditText CustomEditTextToClear ) {
		this.customEditText = CustomEditTextToClear;
	}

	@Override
	public void onClick(View v) {
		customEditText.setText("");
	}
}