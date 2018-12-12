package com.implementhit.OptimizeHIT.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.models.User;

public class SplashActivity extends SuperActivity {
	private Handler worker = new Handler();
	private Runnable task = new Runnable() {
		public void run() {
			Intent intent;

			if (User.sharedUser(getApplicationContext()).isLoggedIn()) {
				intent = new Intent(SplashActivity.this, MenuActivity.class);
			} else {
				intent = new Intent(SplashActivity.this, LoginActivity.class);
			}

			startActivity(intent);
			SplashActivity.this.finish();
    	}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_splash);
	}

	@Override
	protected void refreshStateAfterLogin(boolean isInitialLogin) {
	}

	@Override
	public void onStop() {
		worker.removeCallbacks(task);

		super.onStop();
	}

	@Override
	public void onStart() {
		worker.postDelayed(task, 2000);

		super.onStart();
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
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onConnectionUpdate(boolean isConnected) {
	}
}
