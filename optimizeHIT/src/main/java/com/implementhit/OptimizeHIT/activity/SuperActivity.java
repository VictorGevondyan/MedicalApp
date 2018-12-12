package com.implementhit.OptimizeHIT.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

public abstract class SuperActivity extends AppCompatActivity {
	public static final String IS_STARTED_FOR_HASH_EXPIRE = "isStartedForHashExpire";
	public static final String RETURN_BACK = "returnBack";
	public static final String FROM_LOG_OUT = "fromLogOut";
	public static Boolean FORCE_RELOAD_DASHBOARD = false;
	public static Boolean FROM_LOGGED_OUT = false;
	public static Boolean IS_LIBRARY_OFFLINE_DIALOG_SHOWN = false;
	public static int FRAGMENT_POSITION;

	public static long savedLastClickTime = 0;
	public static long savedLastWebViewInteractionTime = 0;

	private final String SAVE_IS_LOGGED_OUT = "saveIsLoggedOut";
	private final String SAVE_MANUAL_LOGGED_OUT = "saveManualLoggedOut";
	private final String SAVE_ERROR = "saveError";
	private final String SAVE_MESSAGE = "saveMessage";

    public static final String PREFERENCES_NAME = "optimizePreferences";
    public static final String DESTROY_CREATE_DIFFERENCE = "destroyCreateDifference";


    public String error = null;
	public String message = null;

	private boolean isLoggedOut;
	private boolean isManualLoggedOut;

    private static int destroyCreateDifference = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			int color = ColorUtil.getShiftedColor(User.sharedUser(this).primaryColor(), 0.5f);


			if (this instanceof LoginActivity
					|| this instanceof SplashActivity) {
				color = getResources().getColor(R.color.dark_orange);
			}

			getWindow().setNavigationBarColor(color);
			getWindow().setStatusBarColor(color);
		}

//        destroyCreateDifference++;
//
		if (savedInstanceState != null) {
			error = savedInstanceState.getString(SAVE_ERROR);
			message = savedInstanceState.getString(SAVE_MESSAGE);
			isLoggedOut = savedInstanceState.getBoolean(SAVE_IS_LOGGED_OUT);
			isManualLoggedOut = savedInstanceState.getBoolean(SAVE_MANUAL_LOGGED_OUT);
		} else {
			isLoggedOut = !User.sharedUser(this).isLoggedIn();
			isManualLoggedOut = false;
		}
//
//        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(DESTROY_CREATE_DIFFERENCE, destroyCreateDifference);
//        editor.commit();
//
//		Toast.makeText(this, "CREATED", Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVE_ERROR, error);
		outState.putString(SAVE_MESSAGE, message);
		outState.putBoolean(SAVE_IS_LOGGED_OUT, isLoggedOut);
		outState.putBoolean(SAVE_MANUAL_LOGGED_OUT, isManualLoggedOut);

		super.onSaveInstanceState(outState);
	}

	protected void onConnectionUpdate(boolean isConnected) {
	}

	@Override
	protected void onPause() {
		unregisterReceiver(networkBroadcastReceiver);
		OptimizeHIT.setIsInForeground(false);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		OptimizeHIT.setIsInForeground(true);

		IntentFilter networkFilter = new IntentFilter();
		networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkBroadcastReceiver, networkFilter);

		if (isLoggedOut) {
			if (isManualLoggedOut) {
				NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.cancelAll();
			}

			isLoggedOut = !User.sharedUser(this).isLoggedIn();
			refreshStateAfterLogin(isManualLoggedOut);
			isManualLoggedOut = false;
		}
	}

	@Override
	protected void onPostResume() {

		super.onPostResume();

//		if (error != null && !error.isEmpty() ) {
//
//			if (message == null) {
//				NotificationHelper.showNotification( error, this );
//			} else {
//
//				if(  error.equals( getString(R.string.already_exists) ) ){
//					NotificationHelper.showAlreadyAddedNotification(error, message, this);
//				} else {
//					NotificationHelper.showNotification( error, message, true, this );
//				}
//
//			}
//
//		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

//		Toast.makeText(this, "DESTROYED", Toast.LENGTH_LONG).show();
//
//        destroyCreateDifference--;
//
//        if( ( destroyCreateDifference  == 0 )  && !User.sharedUser(this).isKeepLogin()){
//            User.sharedUser(this).logoutUser();
//        }
//
//        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt(DESTROY_CREATE_DIFFERENCE, destroyCreateDifference);
//        editor.commit();
	}

	private BroadcastReceiver networkBroadcastReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
	    	NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
	    	boolean isConnected = activeNetwork != null &&
	    	                      activeNetwork.isConnectedOrConnecting();

	    	SuperActivity.this.onConnectionUpdate(isConnected);
	    }
	};

	protected abstract void refreshStateAfterLogin(boolean isInitialLogin);

	public void setUserLoggedOut(boolean isLoggedOut) {
		this.isLoggedOut = isLoggedOut;
	}

	public void setUserManualLoggedOut(boolean isManualLoggedOut) {
		this.isManualLoggedOut = isManualLoggedOut;
	}
}
