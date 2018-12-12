package com.implementhit.OptimizeHIT.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import java.util.Calendar;

/**
 * This class is intended to solve bug of  CustomEditText cursor visibility after soft keyboard is closed by user
 * ( for example on back button press )
 */

public class CustomEditText extends EditText {

    Context context;

    OnHideKeyboardListener hideKeyboardListener;

    public CustomEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setOnTouchListener(CustomEditTextTouchListener);
    }
    
    public CustomEditText(Context context) {
		super(context);
		this.context = context;
	}

	public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {

    	if( event.getKeyCode() == KeyEvent.KEYCODE_BACK  ){

    		// User has pressed Back key. So hide the keyboard
            InputMethodManager inputMethodManager = (InputMethodManager)
                context.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(this.getWindowToken(), 0);

            // Remove cursor from CustomEditText
            this.setFocusable(false);
            this.setFocusableInTouchMode(false);

            // At that moment ( 17.03.2016 ) listener exists only for login screen
            if( hideKeyboardListener != null ) {
                hideKeyboardListener.onHideKeyboard();
            }
            return true;
    	}
    	return super.dispatchKeyEventPreIme(event);

    }

    @Override
    public void onEditorAction(int actionCode) {
        super.onEditorAction(actionCode);

        if( actionCode == EditorInfo.IME_ACTION_DONE ){
            this.setFocusable(false);
            this.setFocusableInTouchMode(false);
        }

    }

    OnTouchListener CustomEditTextTouchListener = new OnTouchListener() {

        private static final int MAX_CLICK_DURATION = 200;
        private long clickStartTime;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            // onClickListener not working the way we want, so we have to do such a strange things,
            // in order to detect click event. :)
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    clickStartTime = Calendar.getInstance().getTimeInMillis();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    long clickDuration = Calendar.getInstance().getTimeInMillis() - clickStartTime;
                    if(clickDuration < MAX_CLICK_DURATION) {
                        //click event has occurred
                        view.setFocusable(true);
                        view.setFocusableInTouchMode(true);
                        view.requestFocus();
                    }
                }
            }

            return false;
        }
    };

    /**
     *  Listener to make all the CustomEditTexts in activity not focusable.
     *  Indicates back button press when one of CustomEditTexts of activity has focus ( and keyboard is open)
     */

    public interface OnHideKeyboardListener  {
        void onHideKeyboard();
    }

    public void setOnHideKeyboardListener( OnHideKeyboardListener hideKeyboardListener){
        this.hideKeyboardListener = hideKeyboardListener;
    }

}
