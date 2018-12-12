package com.implementhit.OptimizeHIT.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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

/**
 * Created by victor on 2/13/16.
 */
public class AlreadyAddedDialog extends DialogFragment implements android.view.View.OnClickListener {
    private final String TITLE = "title";
    private final String MESSAGE = "message";
    private final String TITLE_STRING = "titleString";
    private final String MESSAGE_STRING = "messageString";

    private View view;
    private int title;
    private int message;

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

    public void setButtonText( String buttonText ){
        Button dialogButton = (Button) view.findViewById(R.id.ok);
        dialogButton.setText(buttonText);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(MESSAGE, message);
        outState.putInt(TITLE, title);
        outState.putString(MESSAGE_STRING, messageString);
        outState.putString(TITLE_STRING, titleString);

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
        }

        Button ok = (Button) view.findViewById(R.id.ok);
        ok.setText(getString(R.string.cancel));
        ok.setOnClickListener(this);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView messageView = (TextView) view.findViewById(R.id.message);
        TextView iconTextView = (TextView) view.findViewById(R.id.icon);
        iconTextView.setTypeface(FontsHelper.sharedHelper(getActivity()).fontello());

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

        if (view.getId() == R.id.ok) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dismiss();
            ((SuperActivity) getActivity()).error = "";
        }
    }

}
