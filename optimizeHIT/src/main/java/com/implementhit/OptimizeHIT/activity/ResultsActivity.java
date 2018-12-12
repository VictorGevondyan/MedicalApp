package com.implementhit.OptimizeHIT.activity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.fragments.SolutionFragment;
import com.implementhit.OptimizeHIT.fragments.SolutionFragment.SolutionFragmentListener;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

public class ResultsActivity extends SuperActivity implements SolutionFragmentListener {

	public static final String OPTI_QUERY_EXTRA = "voiceQueryExtra";
	public static final String HAS_WATSON_ACCESS_EXTRA = "hasWatsonAccessExtra";
	public static final String HAS_VOICE_ACCESS_EXTRA = "hasVoiceAccessExtra";
	public static final String WATSON_HTML_EXTRA = "watsonHtmlExtra";
	
	public static final int REQUEST_CODE = 9921;
	
	private static String watsonHtml;
	
	private SolutionFragment fragment = new SolutionFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_results);
		
		if (savedInstanceState != null) {
			fragment = (SolutionFragment) getSupportFragmentManager().getFragment(savedInstanceState, "fragment");
		} else {
			fragment = new SolutionFragment();

			getSupportFragmentManager()
				.beginTransaction()
				.add(R.id.fragment, fragment)
				.commit();
		}
		
		fragment.setFragmentListener(this);
	}
	
	@Override
	protected void refreshStateAfterLogin(boolean isInitialLogin) {
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		
		fragment.onWindowFocusChanged(hasFocus);
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
		getSupportFragmentManager().putFragment(outState, "fragment", fragment);
		
		super.onSaveInstanceState(outState);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
	}
	
	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}
		
		savedLastClickTime = System.currentTimeMillis();

		fragment.onBackPressed();
	}
	
	/*
	 * Watson Helpers
	 */
	
	public static void setWatsonHtml(String watsonHtml) {
		ResultsActivity.watsonHtml = watsonHtml;
	}
	
	public static String getWatsonHtml() {
		return ResultsActivity.watsonHtml;
	}
	
	/**
	 * SolutionFragmentListener Methods
	 */

	@Override
	public void checkMarkClicked(ArrayList<Solution> addedFavorites, ArrayList<Solution> removedFavorites) {
		overridePendingTransition(R.anim.hold, R.anim.slide_left_in);
		finish();
	}

	@Override
	public void backClicked(ArrayList<Solution> addedFavorites, ArrayList<Solution> removedFavorites) {
		overridePendingTransition(R.anim.hold, R.anim.slide_left_in);
		finish();
	}
}