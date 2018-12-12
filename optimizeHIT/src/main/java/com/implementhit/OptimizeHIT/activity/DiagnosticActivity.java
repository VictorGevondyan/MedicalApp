package com.implementhit.OptimizeHIT.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.DiagnosticsAdapter;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.CheckServerReachabilityRequestListener;
import com.implementhit.OptimizeHIT.api.SupportTicketRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.CustomEditText;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.PermissionHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DiagnosticActivity extends SuperActivity implements View.OnClickListener, CheckServerReachabilityRequestListener, SupportTicketRequestListener,
		AdapterView.OnItemClickListener {

	private static final String CHECK_OP_NO_THROW = "checkOpNoThrow";
	private static final String OP_POST_NOTIFICATION = "OP_POST_NOTIFICATION";

	private static final String SAVE_SUBJECT = "saveSubject";
	private static final String SAVE_ISSUE_DESCRIPTION = "saveDescription";

	int INTERNET_INDEX = 0;
	int SERVER_REACHABLE_INDEX = 1;
	int MICROPHONE_ACCESS_INDEX = 2;
	int PUSH_NOTIFICATION_INDEX = 3;
	int LOCATION_SERVICES_INDEX = 4;

	private Boolean[] diagnosticsData = {false, true, false, false, false};

	private CustomEditText subjectCustomEditText;
	private CustomEditText descriptionCustomEditText;
	private LoadingDialog loadingDialog;

	private User user;
	private DiagnosticsAdapter diagnosticsAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_diagnostic);

		OptimizeHIT.sendScreen(GAnalyticsScreenNames.SUPPORT_SCREEN, null, null, null);

		user = User.sharedUser(this);

		GridView diagnosticsGridView = (GridView) findViewById(R.id.diagnostics_grid);
		diagnosticsAdapter = new DiagnosticsAdapter(this, diagnosticsData);
		diagnosticsGridView.setAdapter(diagnosticsAdapter);
		diagnosticsGridView.setOnItemClickListener(this);

		subjectCustomEditText = (CustomEditText) findViewById(R.id.subject);
		descriptionCustomEditText = (CustomEditText) findViewById(R.id.description);

		if (savedInstanceState != null) {
			subjectCustomEditText.setText(savedInstanceState.getString(SAVE_SUBJECT, ""));
			descriptionCustomEditText.setText(savedInstanceState.getString(SAVE_ISSUE_DESCRIPTION, ""));
		}

		Button submitButton = (Button) findViewById(R.id.submit_button);
		submitButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.little_rounded_rect_orange, User.sharedUser(this).primaryColor()));
		submitButton.setOnClickListener(this);

		Button cancelButton = (Button) findViewById(R.id.right_button);
		cancelButton.setTypeface(FontsHelper.sharedHelper(this).fontello());
		cancelButton.setText(R.string.icon_cancel_circled_dark);
		cancelButton.setOnClickListener(this);
		cancelButton.setVisibility(View.VISIBLE);

		TextView titleTextView = (TextView)findViewById(R.id.content_title);
		titleTextView.setText(R.string.submit_a_ticket);

	}

	/*
	 * OnItemClickListener Methods
	 */

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		NotificationHelper.showNotification(
				getResources().getStringArray(R.array.diagnostics_labels)[position],
				diagnosticsData[position]
						? getResources().getStringArray(R.array.diagnostics_ok_messages)[position]
						: getResources().getStringArray(R.array.diagnostics_error_messages)[position],
				false,
				this
		);
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

		if (view.getId() == R.id.right_button) {
			onBackPressed();
			return;
		}

		String subject = subjectCustomEditText.getText().toString().trim();
		String description = descriptionCustomEditText.getText().toString().trim();

		if (subject.isEmpty() || description.isEmpty()) {
			NotificationHelper.showNotification(getString(R.string.error), getString(R.string.both_subject_and_description_required), true, (SuperActivity) this);
			return;
		}

		String yes = getString(R.string.yes);
		String no = getString(R.string.no);

		PackageInfo pInfo;
		String appVersion = "Unable to Define";
		try {
			pInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			appVersion = pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}

		String diagnostics = getString(R.string.diagnostics_clipboard,
				diagnosticsData[INTERNET_INDEX] ? yes : no,
				diagnosticsData[SERVER_REACHABLE_INDEX] ? yes : no,
				diagnosticsData[MICROPHONE_ACCESS_INDEX] ? yes : no,
				diagnosticsData[PUSH_NOTIFICATION_INDEX] ? yes : no,
				diagnosticsData[LOCATION_SERVICES_INDEX] ? yes : no,
				appVersion,
				android.os.Build.MANUFACTURER + " " + android.os.Build.PRODUCT,
				android.os.Build.VERSION.SDK_INT);

		if (!diagnosticsData[INTERNET_INDEX]) {
			NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, this);
			return;
		}

		loadingDialog = new LoadingDialog(this);
		loadingDialog.show();
		APITalker.sharedTalker().sendSupportTicket(
				subject,
				description,
				diagnostics,
				user.hash(),
				this
		);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

		outState.putString(SAVE_SUBJECT, subjectCustomEditText.getText().toString());
		outState.putString(SAVE_ISSUE_DESCRIPTION, descriptionCustomEditText.getText().toString());
	}

	@Override
	public void onResume() {
		super.onResume();

		diagnosticsData[INTERNET_INDEX] = isNetworkAvailable();

		setupIndicators();
	}

	@Override
	public void onBackPressed() {

		super.onBackPressed();

		overridePendingTransition( R.anim.hold,  R.anim.slide_down_out );

	}


	public void onConnectionUpdate(boolean isConnected) {
		diagnosticsData[INTERNET_INDEX] = isConnected;

		setupIndicators();

		if (!isConnected && (loadingDialog != null) && loadingDialog.isShowing()) {
			loadingDialog.dismiss();
			NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, this);
		}
	}

	@Override
	protected void refreshStateAfterLogin(boolean isInitialLogin) {

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		setupIndicators();
	}

	/**
	 * Indicator Methods
	 */

	public void setupIndicators() {
		if (!diagnosticsData[INTERNET_INDEX]) {
			diagnosticsData[SERVER_REACHABLE_INDEX] = false;
			diagnosticsData[LOCATION_SERVICES_INDEX] = false;
			diagnosticsData[PUSH_NOTIFICATION_INDEX] = false;
		} else {
			APITalker.sharedTalker().checkServersReachability(this);
			diagnosticsData[LOCATION_SERVICES_INDEX] = isLocationServicesAvailable();
			diagnosticsData[PUSH_NOTIFICATION_INDEX] = User.sharedUser(this).isPushNotificationsOn() && arePushNotificationsOn();
		}

		diagnosticsData[MICROPHONE_ACCESS_INDEX] = PermissionHelper.isMicrophoneAvailable(this);
		diagnosticsAdapter.refreshDiagnostics(diagnosticsData);
	}

	/**
	 * Checking State Methods
	 */

	private boolean isNetworkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	private boolean isLocationServicesAvailable() {
		boolean gpsPermitted = PermissionHelper.checkIfHasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				|| PermissionHelper.checkIfHasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

		if (!gpsPermitted) {
			return false;
		}

		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		boolean gpsEnabled = false;
		boolean networkEnabled = false;

		try {
			gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
		}

		try {
			networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
		}

		if (!gpsEnabled && !networkEnabled) {
			return false;
		} else {
			return true;
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@SuppressLint("NewApi")
	private boolean arePushNotificationsOn() {

		AppOpsManager mAppOps = (AppOpsManager) this.getApplicationContext()
				.getSystemService(Context.APP_OPS_SERVICE);

		ApplicationInfo appInfo = this.getApplicationContext().getApplicationInfo();

		String pkg = this.getApplicationContext().getPackageName();

		int uid = appInfo.uid;

		Class appOpsClass = null;

		try {
			appOpsClass = Class.forName(AppOpsManager.class.getName());

			Method checkOpNoThrowMethod = appOpsClass
					.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE,
							String.class);

			Field opPostNotificationValue = appOpsClass
					.getDeclaredField(OP_POST_NOTIFICATION);
			int value = (int) opPostNotificationValue.get(Integer.class);

			return ((int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg) == AppOpsManager.MODE_ALLOWED);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	/**
	 * CheckServerReachabilityHandler Methods
	 */

	@Override
	public void onCheckServerReachabilitySuccess() {
		diagnosticsData[SERVER_REACHABLE_INDEX] = true;
		diagnosticsAdapter.refreshDiagnostics(diagnosticsData);
	}

	@Override
	public void onCheckServerReachabilityFail() {
		diagnosticsData[SERVER_REACHABLE_INDEX] = false;
		diagnosticsAdapter.refreshDiagnostics(diagnosticsData);
	}

	/**
	 * SupportTicketRequestListener Methods
	 */

	@Override
	public void onSupportTicketRequestSuccess(String title, String message) {
		loadingDialog.dismiss();

		subjectCustomEditText.setText("");
		descriptionCustomEditText.setText("");

		NotificationHelper.showNotification(title, message, false, this);
	}

	@Override
	public void onSupportTicketRequestFail(String error, String message) {
		loadingDialog.dismiss();

		if (message == null || message.isEmpty()) {
			NotificationHelper.showNotification(error, this);
		} else {
			NotificationHelper.showNotification(error, message, true, this);
		}
	}
}