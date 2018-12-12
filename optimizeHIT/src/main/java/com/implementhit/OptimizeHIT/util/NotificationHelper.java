package com.implementhit.OptimizeHIT.util;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.LoginActivity;
import com.implementhit.OptimizeHIT.activity.SolutionActivity;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.dialogs.AlreadyAddedDialog;
import com.implementhit.OptimizeHIT.dialogs.NotificationDialog;
import com.implementhit.OptimizeHIT.fragments.OptiQueryFragment;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.api.TalkersConstants;

public class NotificationHelper {

	public static void showNotification(String error, final SuperActivity activity) {
		showNotification(error, activity, false);
	}

	public static void showNotification(String error, final SuperActivity activity, boolean forceDefaultColor) {
		if (activity == null) {
			return;
		}

		Fragment fragment = activity.getFragmentManager().findFragmentByTag("notificationDialog");

		if (fragment instanceof NotificationDialog) {
			NotificationDialog dialog = (NotificationDialog) fragment;
			dialog.dismiss();
		}

		activity.error = error;

		NotificationDialog dialog = new NotificationDialog();
		dialog.setForceDefaultColor(forceDefaultColor);

		if (error.equals(TalkersConstants.HASH_INVALID)) {
			activity.error = null;
			User.sharedUser(activity).logoutUser();
			activity.setUserLoggedOut(true);
			
			if (activity instanceof LoginActivity) {
				dialog.setupDialog(R.string.session_expired, R.string.your_session_has_expired);
			} else {
				showLoginPage(activity, true, false);
				return;
			}
		} else if (error.equals(TalkersConstants.SOLUTION_INVALID)) {
			dialog.setupDialog(R.string.no_solution, R.string.no_solution_found);
			dialog.setIsError(true);
		} else if (error.equals(TalkersConstants.JUST_FAILURE)) {
			dialog.setupDialog(R.string.title_just_fail, R.string.please_try_again);
			dialog.setIsError(true);
		} else if (error.equals(TalkersConstants.LOGIN_FAILED)) {
			dialog.setupDialog(R.string.login_failed, R.string.invalid_credentials);
			dialog.setIsError(true);
		} else if (error.equals(SolutionActivity.SPEECH_ERROR)) {
			dialog.setupDialog(R.string.speech_error, R.string.speech_failure);
			dialog.setIsError(true);
		} else if (error.equals(SolutionActivity.THANKS_ERROR_LOL)) {
			dialog.setupDialog(R.string.feedback_sent, R.string.thanks_for_feedback);
		} else if (error.equals(TalkersConstants.ALL_REQUIRED)) {
			dialog.setupDialog(R.string.login_failed, R.string.all_fields_required);
			dialog.setIsError(true);
		} else if (error.equals(TalkersConstants.CHANGE_PASSWORD_FAILURE)) {
			dialog.setupDialog(R.string.error, R.string.all_fields_required);
			dialog.setIsError(true);
		} else {
			showNotification(activity.getString(R.string.error), error, true, activity);
			return;
		}

		try {
			dialog.show(activity.getFragmentManager(), "notificationDialog");
		} catch(Exception e) {

		}
	}
	
	public static void showNotification(String title, String message, boolean isError, final SuperActivity activity) {
		showNotification(title, message, isError, activity, false);
	}

	public static void showNotification(String title, String message, boolean isError, SuperActivity activity, boolean forceDefaultColor) {
		if (activity == null) {
			return;
		}

		Fragment fragment = activity.getFragmentManager().findFragmentByTag("notificationDialog");

		if (fragment instanceof NotificationDialog) {
			NotificationDialog dialog = (NotificationDialog) fragment;
			dialog.dismiss();
		}

		activity.error = title;
		activity.message = message;

		NotificationDialog dialog = new NotificationDialog();
		dialog.setForceDefaultColor(forceDefaultColor);

		// TODO: REFACTOR THIS, NOT LINER LOGIC
		if (title.equals(TalkersConstants.CHANGE_PASSWORD_FAILURE)) {
			dialog.setupDialog(activity.getString(R.string.request_failed), message);
			dialog.setIsError(true);
		} else if (title.equals(TalkersConstants.CHANGE_PASSWORD_SUCCESS)) {
			dialog.setupDialog(activity.getString(R.string.request_succeed), message);
		} else {
			dialog.setupDialog(title, message);
			dialog.setIsError(isError);
//_______here_was_qyandr_once______jst_in_cse__
		}

		if (dialog.isAdded()) {
			dialog.dismiss();
		}
		
		try {
			dialog.show(activity.getFragmentManager(), "notificationDialog");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void showAlreadyAddedNotification(String error, String message, final SuperActivity activity) {
		if (activity == null) {
			return;
		}

		Fragment fragment = activity.getFragmentManager().findFragmentByTag("AlreadyAddedFragment");

		if (fragment instanceof AlreadyAddedDialog) {
			AlreadyAddedDialog dialog = (AlreadyAddedDialog) fragment;
			dialog.dismiss();
		}

		activity.error = error;
		activity.message = message;

		AlreadyAddedDialog dialog = new AlreadyAddedDialog();
		dialog.setupDialog(error, message);

		if (dialog.isAdded()) {
			dialog.dismiss();
		}

		try {
			dialog.show(activity.getFragmentManager(), "AlreadyAddedFragment");
		} catch(Exception e) {

		}
	}

	public static void showLoginPage(SuperActivity activity, boolean doesSessionExpire, boolean fromLogOut) {
		Intent intent = new Intent(activity, LoginActivity.class);
		intent.putExtra(SuperActivity.IS_STARTED_FOR_HASH_EXPIRE, doesSessionExpire);
		intent.putExtra(SuperActivity.RETURN_BACK, true);
		intent.putExtra(SuperActivity.FROM_LOG_OUT, fromLogOut);
		activity.overridePendingTransition(R.anim.hold, R.anim.slide_up_in);
		activity.startActivity(intent);
	}

}