package com.implementhit.OptimizeHIT.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;

public class CancelableTextWatcher implements TextWatcher{

    private Button cancelView;
    private CustomEditText customEditText;
    
    public CancelableTextWatcher(Button cancelView, CustomEditText CustomEditText) {
        this.cancelView = cancelView;
        this.customEditText = CustomEditText;
    }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
	}

	@Override
	public void afterTextChanged(Editable string) {
        String text = string.toString();
        
        if(text.isEmpty()) {
        	cancelView.setVisibility(View.GONE);
        } else if (customEditText.hasFocus()) {
        	cancelView.setVisibility(View.VISIBLE);
        }
	}
}
