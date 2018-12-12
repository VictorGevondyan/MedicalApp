package com.implementhit.OptimizeHIT.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.ICDCodesListAdapter;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.HelperConstants;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.models.ICD9Related;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;
import com.implementhit.OptimizeHIT.models.IDCRecord;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.ExploreSuperbillRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;

public class ICDCodesListActivity extends SuperActivity implements OnItemClickListener, ExploreSuperbillRequestListener {
	public static final int REQUEST_CODE = 22012;
	public static IDCRecord[] records;

	private static final String LIST_VIEW_STATE = "listState";
	private Parcelable listViewState;
	private ListView codesListView;
	private ICDCodesListAdapter codesAdapter;
	private static final String HAS_CHANGED = "hasChanged";

	private LoadingDialog processingDialog;

	private boolean hasChanged = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		OptimizeHIT.sendScreen(
				GAnalyticsScreenNames.ICD_10_CODES,
				null,
				null, null);

		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_icd_codes);

		TextView backButton = ( TextView )findViewById(R.id.back_button);
		FontsHelper fontsHelper = FontsHelper.sharedHelper(this);
		backButton.setTypeface(fontsHelper.fontello());

		if (records == null || records.length == 0) {
			findViewById(R.id.no_result).setVisibility(View.VISIBLE);
		} else {
			codesListView = (ListView) findViewById(R.id.icd_codes_list_view);
			codesAdapter = new ICDCodesListAdapter(this,
					R.layout.item_icd_code_list, records);
			codesListView.setAdapter(codesAdapter);
			codesListView.setOnItemClickListener(this);

			if (savedInstanceState != null){
				listViewState = savedInstanceState.getParcelable(LIST_VIEW_STATE);
				codesListView.onRestoreInstanceState(listViewState);
				hasChanged = savedInstanceState.getBoolean(HAS_CHANGED);
			}

			processingDialog = new LoadingDialog(this);
		}
	}

	@Override
	public void finish() {
		super.finish();

		overridePendingTransition(R.anim.hold, R.anim.slide_down_out);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		super.onSaveInstanceState(outState);

		if (records != null && records.length > 0 && codesListView != null) {
			listViewState = codesListView.onSaveInstanceState();
			outState.putParcelable(LIST_VIEW_STATE, listViewState);
			outState.putBoolean(HAS_CHANGED, hasChanged);
		}

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE) {
			hasChanged = data.getBooleanExtra(HAS_CHANGED, false);
		} else if (requestCode == HelperConstants.RESULT_HASH_EXPIRED) {
			NotificationHelper.showNotification(TalkersConstants.HASH_INVALID, this);
		}
	}

	public void onBack(View view){
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}

		savedLastClickTime = System.currentTimeMillis();

		Intent intent = new Intent();
		intent.putExtra(HAS_CHANGED, hasChanged);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onBackPressed() {
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}

		savedLastClickTime = System.currentTimeMillis();

		Intent resultIntent = new Intent();
		resultIntent.putExtra(FindCodeActivity.HAS_CHANGED, true);
		setResult(RESULT_OK, resultIntent);

		super.onBackPressed();
	}

	@Override
	protected void refreshStateAfterLogin(boolean isInitialLogin) {
	}

	/**
	 * OnItemClickListener Methods
	 */

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}

		savedLastClickTime = System.currentTimeMillis();

		OptimizeHIT.sendEvent(
				GAnalitycsEventNames.EXPLORE_ICD_10.CATEGORY,
				GAnalitycsEventNames.EXPLORE_ICD_10.ACTION,
				GAnalitycsEventNames.EXPLORE_ICD_10.LABEL);

		Locker.lock(this);
		IDCRecord record = codesAdapter.getItem(position);
		processingDialog.show();
		APITalker.sharedTalker().exploreSuperbill(User.sharedUser(this).hash(), record.getCode(), record.getDescription(), this);
	}

	/**
	 * ExploreSuperbillHandler Methods
	 */

	@Override
	public void exploreSuperbillSuccess(
			ICD9Related[] idc9Related, ICDAdditionalInfo[] icd10Info,
			boolean billable, String code, String description) {
		processingDialog.dismiss();
		ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up_in, R.anim.hold);
		Intent exploreIntent = new Intent(this, ExploreICDActivity.class);
		exploreIntent.putExtra(ExploreICDActivity.CODE, code);
		exploreIntent.putExtra(ExploreICDActivity.DESCRIPTION, description);
		exploreIntent.putExtra(ExploreICDActivity.BILLABLE, billable);
		exploreIntent.putExtra(ExploreICDActivity.ADDITIONAL_INFO, icd10Info);
		exploreIntent.putExtra(ExploreICDActivity.RELATED_RECORDS, idc9Related);
		startActivityForResult(exploreIntent, REQUEST_CODE, options.toBundle());

		Locker.unlock(this);
	}

	@Override
	public void exploreSuperbillFailure(String error) {
		processingDialog.dismiss();
		NotificationHelper.showNotification(error, this);
		Locker.unlock(this);
	}
}
