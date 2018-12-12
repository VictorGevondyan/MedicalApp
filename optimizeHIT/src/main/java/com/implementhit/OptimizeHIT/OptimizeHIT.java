package com.implementhit.OptimizeHIT;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDexApplication;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.CheckDataUpdateRequestListener;
import com.implementhit.OptimizeHIT.api.HistoryRequestListener;
import com.implementhit.OptimizeHIT.api.PeersSearchRequestListener;
import com.implementhit.OptimizeHIT.api.UpdateLocationHandler;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.database.ICDDatabase;
import com.implementhit.OptimizeHIT.models.User;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;

public class OptimizeHIT extends MultiDexApplication
		implements LocationListener, UpdateLocationHandler, CheckDataUpdateRequestListener, HistoryRequestListener, PeersSearchRequestListener {
	public static int customDimension_Title_Id = 1;
	public static int customDimension_VoiceQuery_Id = 2;
	public static int customDimension_SolutionAccessedUsing_Id = 4;

	public String PROPERTY_ID = "UA-45575210-3";

	private static Region BEACON_REGION = new com.estimote.sdk.Region(
			"monitored region",
			UUID.fromString("64B9F424-F1CB-3C82-87F3-ED0FE3661BD9"),
			null, null);

	private static Tracker tracker;
	private static OptimizeHIT sharedApplication;

	private GoogleAnalytics analytics;
	private LocationManager locationManager;
	private BeaconManager beaconManager;

	private static boolean isInForeground;

	private APITalker apiTalker;
	private User user;
	private String hash;
	private DBTalker dbTalker;

	public static OptimizeHIT sharedApplication() {
		return sharedApplication;
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {
		super.onCreate();

		SuperActivity.FROM_LOGGED_OUT = false;

		analytics = GoogleAnalytics.getInstance(this);
		tracker = analytics.newTracker(PROPERTY_ID);
		sharedApplication = this;
		DBTalker.removeLink();

		dbTalker = DBTalker.sharedDB(this);
		dbTalker.flushBannerNotificationsTable();

		user = User.sharedUser(this);
		user.setLastAccessedPage(-1);

		hash = user.hash();
		apiTalker = APITalker.sharedTalker();

		if ( hash != null) {
			OptimizeHIT.sharedApplication().updateData();

			apiTalker.getSuggestedLearning(hash, getApplicationContext(), null);
			apiTalker.getNotifications(getApplicationContext(), hash, null);
			apiTalker.getPopularQuestions(hash, getApplicationContext(), null);
			apiTalker.downloadSuperbill(hash, ICDDatabase.sharedDatabase(this));
			apiTalker.getUserHistory(hash, dbTalker);
			apiTalker.getPeerFavorites(hash,dbTalker);
		}

		startBeaconMonitoring();

		SharedPreferences sharedPreferences = getSharedPreferences("MY_SHARED_PREFS", 0);
		SharedPreferences.Editor sharedPreferencesEditor = sharedPreferences.edit();
        sharedPreferencesEditor.putBoolean("NOT IS SHOWN", false);
		sharedPreferencesEditor.commit();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public static void sendEvent(String categoryId, String actionId, String labelId) {
		// Build and send an Event.
		tracker.send(
				new HitBuilders.EventBuilder().setCategory(categoryId).setAction(actionId).setLabel(labelId).build());
	}

	/**
	 * A function for sending screen view statistic to the Google Analytics.
	 * customDimension_Title and customDimension_VoiceQuery can be "null"
	 */

	public static void sendScreen(String screenName, String customDimension_Title, String customDimension_VoiceQuery, String customDimension_SolutionAccessedUsing ) {

		// Create a builder to use for setting custom dimensions
		HitBuilders.ScreenViewBuilder builder = new HitBuilders.ScreenViewBuilder();

		// Set screen name.
		tracker.setScreenName(screenName);

		if (customDimension_Title != null) {
			builder.setCustomDimension(customDimension_Title_Id, customDimension_Title);
		} else if (customDimension_VoiceQuery != null) {
			builder.setCustomDimension(customDimension_VoiceQuery_Id, customDimension_VoiceQuery);
		} else if (customDimension_VoiceQuery != null) {
			builder.setCustomDimension(customDimension_SolutionAccessedUsing_Id, customDimension_SolutionAccessedUsing);
		}

		// Build and send a Screen.
		tracker.send(builder.build());
	}

	/**
	 * Data Update Methods
	 */

	public void updateLocation() {
		locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
		}

		locationManager.requestSingleUpdate(criteria, this, null);
	}

	public void updateData() {
		apiTalker.checkDataUpdate(hash, user.trigger(), this);
	}

	/**
	 * LocationListener Methods
	 */

	@Override
	public void onLocationChanged(Location location) {
		apiTalker.updateLocation(hash, location.getLatitude(),
                location.getLongitude(), location.getAccuracy(), this);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	/**
	 * UpdateLocationHandler Methods
	 */

	@Override
	public void onLocationUpdateSuccess() {
		apiTalker.getSuggestedLearning(hash, this, null);
		apiTalker.downloadData(this, hash, false, dbTalker, null);
	}

	@Override
	public void onLocationUpdateFail(String error) {

	}

	/**
	 * Application State Methods
	 */

	public static boolean isInForeground() {
		return isInForeground;
	}

	public static void setIsInForeground(boolean isInForeground) {
		OptimizeHIT.isInForeground = isInForeground;
	}

	/**
	 * CheckDataUpdateHandler Methods
	 */

	@Override
	public void onCheckDataUpdateSuccess(long trigger) {

		if (user.updateTrigger(trigger)) {
			apiTalker.downloadData(this, hash, false, dbTalker, null);
		} else {
			Intent intent = new Intent();
			intent.setAction(APITalker.ACTION_UPDATE_DATA_TERMINATED);
			sendBroadcast(intent);
		}

	}

	@Override
	public void onCheckDataUpdateFail(String error) {
		Intent intent = new Intent();
		intent.setAction(APITalker.ACTION_UPDATE_DATA_TERMINATED);
		sendBroadcast(intent);
	}

	/**
	 * Beacon Monitoring
	 */

	private void startBeaconMonitoring() {
		beaconManager = new BeaconManager(getApplicationContext());

		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
			return;
		}

		beaconManager.setMonitoringListener(new BeaconManager.MonitoringListener() {

			@Override
			public void onEnteredRegion(com.estimote.sdk.Region region, List<Beacon> beacons) {

				if (beacons.isEmpty() || !User.sharedUser(getApplicationContext()).isLoggedIn()) {
					return;
				}

				apiTalker.sendBeaconInRange(getApplicationContext(), hash, beacons.get(0).getMajor(), beacons.get(0).getMinor());

			}

			@Override
			public void onExitedRegion(com.estimote.sdk.Region region) {
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						beaconManager.stopMonitoring(BEACON_REGION);
						beaconManager.startMonitoring(BEACON_REGION);
					}
				}, 60000);
			}
		});

		beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
			@Override
			public void onServiceReady() {
				beaconManager.stopMonitoring(BEACON_REGION);
				beaconManager.startMonitoring(BEACON_REGION);
			}
		});
	}

	@Override
	public void historyRequestSuccess(JSONObject userHistory) {
		dbTalker.insertUserHistory( userHistory );
	}

	@Override
	public void historyRequestFailure(String error) {

	}

	@Override
	public void peersSearchSuccess( JSONArray peerSolutions) {
		dbTalker.insertPeerFavorites( peerSolutions );
	}

	@Override
	public void peersSearchFailure(String error) {

	}
}























