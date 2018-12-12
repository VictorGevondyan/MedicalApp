package com.implementhit.OptimizeHIT.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.fragments.SolutionFragment;
import com.implementhit.OptimizeHIT.fragments.SolutionFragment.SolutionFragmentListener;
import com.implementhit.OptimizeHIT.gcm.GcmBroadcastReceiver;
import com.implementhit.OptimizeHIT.util.HelperConstants;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.api.APITalker;

import org.json.JSONArray;

import java.util.ArrayList;

public class SolutionActivity extends SuperActivity implements SolutionFragmentListener {
	public static final String EXTRA_SPEECH = "speechExtra";
	public static final String EXTRA_HTML = "htmlExtra";
	public static final String EXTRA_SOLUTIONS = "solutionsExtra";
	public static final String EXTRA_POSITION = "positionExtra";
	public static final String EXTRA_ACCESS_METHOD = "accessMethodExtra";
	public static final String SAVE_REMOVED_FAVORITES = "saveRemovedFavorites";
	public static final String SAVE_ADDED_FAVORITES = "saveAddedFavorites";
	public static final String IS_VOICE_HISTORY = "isVoiceHistory";

	public static final int REQUEST_CODE = 9915;
	public static final int CHECKMARK_FINISH = 9917;

	public static final String SPEECH_ERROR = "speechError";
	public static final String THANKS_ERROR_LOL = "thanksErrorLol";

	private ArrayList<Solution> removedFavorites = new ArrayList<Solution>();
	private ArrayList<Solution> addedFavorites = new ArrayList<Solution>();

	private SolutionFragment fragment = new SolutionFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_solution);

		if (savedInstanceState != null) {
			fragment = (SolutionFragment) getSupportFragmentManager().getFragment(savedInstanceState, "fragment");
		} else {
			fragment = new SolutionFragment();

			getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.fragment, fragment)
				.commit();
		}

		if (getIntent().getExtras().containsKey(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID)) {
			int notificationId = getIntent().getExtras().getInt(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID);

			Notification notification = DBTalker.sharedDB(this).getNotification(notificationId);

			if (!notification.isRead()) {
				int unreadNotificationsCount = User.sharedUser(this).unreadNotifications();
				unreadNotificationsCount--;
				User.sharedUser(this).setUnreadNotifications(unreadNotificationsCount);
			}

			JSONArray jsonArray = new JSONArray();
			String notificationIdString = String.valueOf(notificationId);
			jsonArray.put(notificationIdString);
			String jsonFormattedString = jsonArray.toString();

			APITalker.sharedTalker().markNotificationRead(User.sharedUser(this).hash(), jsonFormattedString, null);

			ArrayList<String> notificationIds = new ArrayList<String>();
			notificationIds.add(notificationIdString);

			DBTalker.sharedDB(this).markNotificationsReadInDb(notificationIds);
		}

		fragment.setFragmentListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);

		fragment.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void onConnectionUpdate(boolean isConnected) {
		super.onConnectionUpdate(isConnected);

		fragment.onConnectionUpdate(isConnected);
	}

	@Override
	protected void refreshStateAfterLogin(boolean isInitialLogin) {
		fragment.reloadContent();
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}

		savedLastClickTime = System.currentTimeMillis();

		fragment.onBackPressed();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SolutionActivity.REQUEST_CODE) {
			if (resultCode == HelperConstants.RESULT_HASH_EXPIRED) {
				setResult(HelperConstants.RESULT_HASH_EXPIRED);
				finish();
			}
		}
	}

	@Override
	public void finish() {
		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(SAVE_REMOVED_FAVORITES, removedFavorites);
		intent.putParcelableArrayListExtra(SAVE_ADDED_FAVORITES, addedFavorites);
		setResult(RESULT_OK, intent);

		super.finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		getSupportFragmentManager().putFragment(outState, "fragment", fragment);

		super.onSaveInstanceState(outState);
	}

	/**
	 * SolutionFragmentListener Methods
	 */

	@Override
	public void checkMarkClicked(ArrayList<Solution> addedFavorites, ArrayList<Solution> removedFavorites) {
		this.removedFavorites = removedFavorites;
		this.addedFavorites = addedFavorites;

		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(SAVE_REMOVED_FAVORITES, removedFavorites);
		intent.putParcelableArrayListExtra(SAVE_ADDED_FAVORITES, addedFavorites);
		setResult(CHECKMARK_FINISH, intent);
		super.finish();

		// Make activity transition animated, only if entered from main application
		if (!(getIntent().getAction() != null && getIntent().getAction()
				.equals(GcmBroadcastReceiver.NOTIFICATION_ACTION))
				&& !(getIntent().getDataString() != null && !getIntent().getDataString()
				.isEmpty())) {
			overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
		}
	}

	@Override
	public void backClicked(ArrayList<Solution> addedFavorites, ArrayList<Solution> removedFavorites) {
		this.removedFavorites = removedFavorites;
		this.addedFavorites = addedFavorites;

		Intent intent = new Intent();
		intent.putParcelableArrayListExtra(SAVE_REMOVED_FAVORITES, removedFavorites);
		intent.putParcelableArrayListExtra(SAVE_ADDED_FAVORITES, addedFavorites);
		setResult(RESULT_OK, intent);
		super.finish();

		// Make activity transition animated, only if entered from main application
		if (!(getIntent().getAction() != null && getIntent().getAction().equals(GcmBroadcastReceiver.NOTIFICATION_ACTION))
				&& !(getIntent().getDataString() != null && !getIntent().getDataString().isEmpty())) {
			overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
		}
	}
}
