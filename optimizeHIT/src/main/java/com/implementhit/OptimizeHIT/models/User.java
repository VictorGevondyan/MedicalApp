package com.implementhit.OptimizeHIT.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.badge.BadgeCounter;
import com.implementhit.OptimizeHIT.database.DBTalker;

public class User {
	public static final int MAX_SPEECH_SPEED = 300;
	public static final int MIN_SPEECH_SPEED = 25;

	private static final String USER_PREFERENCES = "userPreferences";
	private static final String USERNAME = "username";
	private static final String FIRST_NAME = "firstName";
	private static final String LAST_NAME = "lastName";
	private static final String DOMAIN = "domain";
	private static final String IMAGE_URL = "imageUrl";
	private static final String PRIMARY_COLOR = "primaryColor";
	private static final String DOMAIN_LABEL = "domainLable";
	private static final String HASH = "hash";
	private static final String TRIGGER = "trigger";
	private static final String IS_LOGGED_IN = "isLoggedIn";
	private static final String VOICE_ACCESS = "voiceAccess";
	private static final String WATSON_ACCESS = "watsonAccess";
	private static final String FIND_A_CODE = "findACode";
	private static final String REACTIVATIONS = "reactivations";
	private static final String DASHBOARD_BADGE = "dashboardBadge";
	private static final String UNREAD_NOTIFICATIONS_COUNT = "unreadNotificationsCount";
	private static final String IS_PUSH_NOTIFICATIONS_ON = "isPushNotificationsOn";
	private static final String LAST_ACCESSED_PAGE = "lastAccessedPage";

	private static User user;

	private String username = "";
	private String firstName = "";
	private String lastName = "";
	private String domain = "";
	private String imageUrl = "";
	private String primaryColor = "";
	private String domainLabel = "";
	private String hash = "";
	private int reactivations = 0;
	private int unreadNotifications = 0;
	private int dashboardBadgeCount = 0;
	private int lastAccessedPage = -1;
	private long trigger = 0;
	private boolean isLoggedIn = false;
	private boolean voiceAccess = false;
	private boolean watsonAccess = false;
	private boolean findACode = false;
	private boolean isPushNotificationsOn = false;

	private Context context;
	private Superbills superbills;

    // This interface variable enables access to the methods of interface
	private OnLoginLogoutListener loginLogoutListener;

	private User(Context context) {
		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);

		username = preferences.getString(USERNAME, "");
		domain = preferences.getString(DOMAIN, "");
		imageUrl = preferences.getString(IMAGE_URL, "");
		primaryColor = preferences.getString(PRIMARY_COLOR, "");
		domainLabel = preferences.getString(DOMAIN_LABEL, "");
		hash = preferences.getString(HASH, "");
		firstName = preferences.getString(FIRST_NAME, "");
		lastName = preferences.getString(LAST_NAME, "");
		reactivations = preferences.getInt(REACTIVATIONS, 0);
		unreadNotifications = preferences.getInt(UNREAD_NOTIFICATIONS_COUNT, 0);
		trigger = preferences.getLong(TRIGGER, 0);
		isLoggedIn = preferences.getBoolean(IS_LOGGED_IN, false);
		voiceAccess = preferences.getBoolean(VOICE_ACCESS, false);
		watsonAccess = preferences.getBoolean(WATSON_ACCESS, false);
		findACode = preferences.getBoolean(FIND_A_CODE, false);

		superbills = new Superbills(context, username);

		isPushNotificationsOn = preferences.getBoolean(IS_PUSH_NOTIFICATIONS_ON, false);

		this.context = context;

	}

	public static User sharedUser(Context context) {
		if (user == null) {
			user = new User(context);
		}

		return user;
	}

	public void saveDomain(String domain, String domainLabel, String imageUrl, String primaryColor) {
		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();
		editor.putString(DOMAIN, domain);
		editor.putString(DOMAIN_LABEL, domainLabel);
		editor.putString(IMAGE_URL, imageUrl);
		editor.putString(PRIMARY_COLOR, primaryColor);
		editor.commit();

		this.domainLabel = domainLabel;
		this.domain = domain;
		this.imageUrl = imageUrl;
		this.primaryColor = primaryColor;
	}

	public void saveUser(String username, String firstName, String lastName, String domain, String domainLabel, String hash, long trigger,
			boolean voiceAccess, boolean watsonAccess, boolean findACode) {
		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putString(USERNAME, username);
		editor.putString(DOMAIN, domain);
		editor.putString(DOMAIN_LABEL, domainLabel);
		editor.putString(HASH, hash);
		editor.putString(FIRST_NAME, firstName);
		editor.putString(LAST_NAME, lastName);
		editor.putLong(TRIGGER, trigger);
		editor.putBoolean(IS_LOGGED_IN, true);
		editor.putBoolean(VOICE_ACCESS, voiceAccess);
		editor.putBoolean(WATSON_ACCESS, watsonAccess);
		editor.putBoolean(FIND_A_CODE, findACode);

		this.username = username;
		this.lastName = lastName;
		this.firstName = firstName;
		this.domain = domain;
		this.domainLabel = domainLabel;
		this.hash = hash;
		this.trigger = trigger;
		this.isLoggedIn = true;
		this.voiceAccess = voiceAccess;
		this.watsonAccess = watsonAccess;
		this.findACode = findACode;

		setReactivations(0);
		setUnreadNotifications(0);

		superbills.setUsername(username);

		editor.commit();
	}

	public String imageUrl() {
		return imageUrl;
	}

	@SuppressWarnings("Range")
	public int  primaryColor() {
		int primaryColorInt;
		try {
			primaryColorInt = Color.parseColor(primaryColor);
		} catch (Exception exception) {
			primaryColorInt = context.getResources().getColor(R.color.dark_green);
		}
		return primaryColorInt;
	}

	public String username() {
		return username;
	}

    public String lastName() {
        return lastName;
    }

    public String firstName() {
        return firstName;
    }

    public String domain() {
		return domain;
	}

	public String domainLabel() {
		return domainLabel;
	}

	public String hash() {
		return hash;
	}

	public int reactivations() {
		return reactivations;
	}

	public int unreadNotifications() {
		return unreadNotifications;
	}

	public int dashboardBadgeCount() {
		return dashboardBadgeCount;
	}

	public long trigger() {
		return trigger;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public boolean isPushNotificationsOn() {
		return isPushNotificationsOn;
	}

	public boolean voiceAccess() {
		return voiceAccess;
	}

	public boolean watsonAccess() {
		return watsonAccess;
	}

	public Superbills getSuperbills() {
		return superbills;
	}

	public boolean getFindACodePermission() {
		return findACode;
	}

	public boolean updateTrigger(long newTrigger) {
		boolean needsDbUpdate = newTrigger > trigger;
		trigger = newTrigger;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putLong(TRIGGER, trigger);
		editor.commit();

		return needsDbUpdate;
	}

	public void updatePermissions(boolean voiceAccess, boolean watsonAccess, boolean findACode) {
		this.voiceAccess = voiceAccess;
		this.watsonAccess = watsonAccess;
		this.findACode = findACode;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putBoolean(VOICE_ACCESS, voiceAccess);
		editor.putBoolean(WATSON_ACCESS, watsonAccess);
		editor.putBoolean(FIND_A_CODE, findACode);
		editor.commit();
	}

	public void logoutUser() {
		isLoggedIn = false;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putBoolean(IS_LOGGED_IN, false);
		editor.commit();

		DBTalker.sharedDB(context).flushBannerNotificationsTable();

		// loginLogoutListener can be null, if it is not set in corresponding class.
		// For example we have not entered DashboardView and the listener is not set.
		if( loginLogoutListener != null ) {
			// We call the method of interface, which DashboardFragment is implemented, in order to set boolean variable
			loginLogoutListener.onLoginLogout(context);
		}

	}

	synchronized public void setReactivations(int reactivations) {
		this.reactivations = reactivations;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putInt(REACTIVATIONS, reactivations);
		editor.commit();

		updateIconBadge();
	}

	public void setDashboardBadgeCount(int dashboardBadgeCount) {
		this.dashboardBadgeCount = dashboardBadgeCount;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putInt(DASHBOARD_BADGE, reactivations);
		editor.commit();

		updateIconBadge();
	}

	public void setUnreadNotifications(int unreadNotifications) {
		if (unreadNotifications < 0) {
			return;
		}

		this.unreadNotifications = unreadNotifications;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putInt(UNREAD_NOTIFICATIONS_COUNT, unreadNotifications);
		editor.commit();

		updateIconBadge();
	}

	public void setIsPushNotificationsOn(boolean isPushNotificationsOn) {
		this.isPushNotificationsOn = isPushNotificationsOn;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putBoolean(IS_PUSH_NOTIFICATIONS_ON, isPushNotificationsOn);
		editor.commit();
	}

	public int getLastAccessedPage() {
		return lastAccessedPage;
	}

	public void setLastAccessedPage(int lastAccessedPage) {
		this.lastAccessedPage = lastAccessedPage;

		SharedPreferences preferences = context.getSharedPreferences(USER_PREFERENCES, 0);
		Editor editor = preferences.edit();

		editor.putInt(LAST_ACCESSED_PAGE, lastAccessedPage);
		editor.commit();
	}

	private void updateIconBadge() {
		BadgeCounter.updateNotificationBadge(context, this.reactivations + this.unreadNotifications + this.dashboardBadgeCount);
	}

    // We want the Dashboard page to open from beginning after user logout-login. For this we need below interface.
    // DashboardFragment implement the function of this interface. In this implementation we set the boolean variable,
    // which indicates, that we come to Dashboard screen after login-logout. And then, accordingly to that variable
    // value, we do the job
	public interface OnLoginLogoutListener {
		void onLoginLogout( Context context );
	}

    public void setOnLoginLogoutListener( OnLoginLogoutListener loginLogoutListener ){
        this.loginLogoutListener = loginLogoutListener;
    }

}
