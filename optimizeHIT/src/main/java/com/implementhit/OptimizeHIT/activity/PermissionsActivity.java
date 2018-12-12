package com.implementhit.OptimizeHIT.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.PermissionHelper;

public class PermissionsActivity extends Activity implements OnRequestPermissionsResultCallback {
	private final String SAVED_PERMISSION_STEP = "savedPermissionStep";
	
	private int permissionStep = 0;
	
	private int[] permissionIcons = {
		R.string.icon_mic,
//		R.string.icon_bell_alt,
		R.string.icon_map_pin
	};
	
	private int[] permissionsTitles = {
			R.string.permission_microphone_access_title,
//			R.string.permission_push_notifications_title,
			R.string.permission_location_access_title
	};
	
	private int[] permissionsHints = {
			R.string.permission_microphone_access_hint,
//			R.string.permission_push_notifications_hint,
			R.string.permission_location_access_hint
	};

	private int[] permissionsDeclines = {
			R.string.already_provided,
//			R.string.permission_push_notifications_decline,
			R.string.already_provided
	};
	
	private static String[][] permissions = {
		{Manifest.permission.RECORD_AUDIO},
//		{com.implementhit.OptimizeHIT.Manifest.permission.C2D_MESSAGE},
		{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
	};
	
	private TextView iconTextView;
	private TextView titleTextView;
	private TextView hintTextView;
	private TextView declineTextView;
	private Button proceedButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		OptimizeHIT.sendScreen(GAnalyticsScreenNames.FIRST_TIME_SETUP, null, null, null);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_permissions);
		
		iconTextView = (TextView) findViewById(R.id.permission_icon);
		titleTextView = (TextView) findViewById(R.id.title);
		hintTextView = (TextView) findViewById(R.id.hint);
		declineTextView = (TextView) findViewById(R.id.previously_provided);
		proceedButton = (Button) findViewById(R.id.proceed_button);
		
		if (savedInstanceState != null) {
			permissionStep = savedInstanceState.getInt(SAVED_PERMISSION_STEP);
		}
		
		iconTextView.setTypeface(FontsHelper.sharedHelper(this).fontello());
		iconTextView.setTextColor(User.sharedUser(this).primaryColor());
		
		for ( ; permissionStep < permissions.length ; permissionStep++) {
			boolean hasPermission = true;
			
			for (String permission : permissions[permissionStep]) {
				hasPermission = hasPermission & PermissionHelper.checkIfHasPermission(this, permission);
			}
			
			if (!hasPermission) {
				setupPermissionUI();
				return;
			}
		}
		
		setupPermissionUI();
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
	protected void onSaveInstanceState(Bundle outState) {
		outState.putInt(SAVED_PERMISSION_STEP, permissionStep);
		
		super.onSaveInstanceState(outState);
	}

	private void setupPermissionUI() {

		TextView noteTextView = (TextView) findViewById(R.id.note);
		TextView physicalLocationTextView = (TextView) findViewById(R.id.physical_location);
		TextView whenYouPressTextView = (TextView) findViewById(R.id.when_you_press);


		if (permissionStep < permissions.length) {
			iconTextView.setText(permissionIcons[permissionStep]);
			titleTextView.setText(permissionsTitles[permissionStep]);
			hintTextView.setText(permissionsHints[permissionStep]);
			declineTextView.setText(permissionsDeclines[permissionStep]);
			proceedButton.setText(R.string.proceed);

			if( permissionStep == 1 ){

				noteTextView.setVisibility(View.VISIBLE);
				physicalLocationTextView.setVisibility(View.VISIBLE);
				whenYouPressTextView.setVisibility(View.VISIBLE);

			}
		} else {
			iconTextView.setText(R.string.icon_check);
			titleTextView.setText(R.string.setup_complete);
			hintTextView.setText(R.string.thank_you_for_installing);
			declineTextView.setText("");
			proceedButton.setText(R.string.done);

			noteTextView.setVisibility(View.GONE);
			physicalLocationTextView.setVisibility(View.GONE);
			whenYouPressTextView.setVisibility(View.GONE);

		}
	}
	
	public void proceed(View view) {
		if (permissionStep < permissions.length) {
			ActivityCompat.requestPermissions(this,
	                permissions[permissionStep],
	                0);
		} else {
			Intent intent = new Intent(this, MenuActivity.class);
			startActivity(intent);
			this.finish();
		}
	}
	
	@SuppressLint("Override")
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		permissionStep++;
		setupPermissionUI();
	}
	
	public static boolean needsDisplay(Context context) {
		for (int index = 0 ; index < permissions.length ; index++) {
			for (String permission : permissions[index]) {
				boolean hasPermission = PermissionHelper.checkIfHasPermission(context, permission);
				
				if (!hasPermission) {
					return true;
				}
			}
		}
		
		return false;
	}
}