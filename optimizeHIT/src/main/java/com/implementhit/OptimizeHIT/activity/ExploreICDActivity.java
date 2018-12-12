package com.implementhit.OptimizeHIT.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.DocumentationInfoAdapter;
import com.implementhit.OptimizeHIT.adapter.ICD9RecordListAdapter;
import com.implementhit.OptimizeHIT.adapter.SubordinateAdapter;
import com.implementhit.OptimizeHIT.adapter.SubordinateAdapter.SubordinateAdapterListener;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.database.ICDDatabase;
import com.implementhit.OptimizeHIT.dialogs.InputDialog;
import com.implementhit.OptimizeHIT.dialogs.InputDialog.InputDialogHandler;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.dialogs.PickerDialog;
import com.implementhit.OptimizeHIT.dialogs.PickerDialog.PickerListener;
import com.implementhit.OptimizeHIT.dialogs.YesNoDialog;
import com.implementhit.OptimizeHIT.dialogs.YesNoDialog.YesNoDialogListener;
import com.implementhit.OptimizeHIT.models.ICD9Related;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;
import com.implementhit.OptimizeHIT.models.ICDRecordExtended;
import com.implementhit.OptimizeHIT.models.IDCRecord;
import com.implementhit.OptimizeHIT.models.NavigationModel;
import com.implementhit.OptimizeHIT.models.Settings;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.StringUtils;
import com.implementhit.OptimizeHIT.views.FractionListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Stack;

public class ExploreICDActivity extends SuperActivity implements YesNoDialogListener, SubordinateAdapterListener, PickerListener, InputDialogHandler {
	private final String GROUP_PICKER_SHOWN = "saveGroupPickerShown";
	
	public static final String SUPERBILLS = "superbills";
	public static final String ADDITIONAL_INFO = "additionalInfo";
	public static final String RELATED_RECORDS = "relatedRecords";
	public static final String DESCRIPTION = "description";
	public static final String BILLABLE = "billable";
	public static final String CODE = "code";
	
	private static final String CODE_TO_ADD = "codeToAdd";
	private static final String DESCRIPTION_TO_ADD = "descriptionToAdd";
	private static final String BILLABLE_TO_ADD = "billableToAdd";
	private static final String ADDED_ICON_POSITION = "addedIconPosition";
	private static final String SCROLLVIEW_POSITION = "scrollViewPosition";
	private static final String SUBORDINATE_LIST_POSITION = "subordinateListPosition";
	private static final String HISTORY_LEVEL = "historyLevel";
	private static final String HISTORY = "history";
	private static final String STATES = "states";
	private static final String HAS_CHANGED = "hasChanged";
	
	private String rootCode;
    private String fullTitle;
	private String description;

    private String clickedItemCode;
    private String clickedItemDescription;


	private boolean billable;
	private ICDAdditionalInfo[] icd10Info;
	private ICD9Related[] idc9Related;
	
	private FractionListView[] subordinateListViews = new FractionListView[2];
	private SubordinateAdapter[] subOrdinateAdapters = new SubordinateAdapter[2];
	private int currentListView = 0;
	private int currentLevel = 0;
	private Stack<NavigationModel> subordinateHistory;
	
	private String codeToAdd;
	private String descriptionToAdd;
	private int addIconPosition = -1;
	private boolean billableToAdd = true;
	
	private boolean[] states;
	
	private boolean hasChanged = false;
	private boolean groupPickerShown = false;
	
	private ICDDatabase icdDatabase;
	private Settings settings;

    LoadingDialog processingDialog;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_explore_icd);
		
		icdDatabase = ICDDatabase.sharedDatabase(this);
		settings = DBTalker.sharedDB(this).initSettings();

        processingDialog = new LoadingDialog(this);

		rootCode = getIntent().getStringExtra(CODE);

		description = getIntent().getStringExtra(DESCRIPTION);
		billable = getIntent().getBooleanExtra(BILLABLE, true);

		Parcelable[] infoParcel = getIntent().getParcelableArrayExtra(ADDITIONAL_INFO);
		icd10Info = Arrays.copyOf(infoParcel, infoParcel.length, ICDAdditionalInfo[].class);
		Parcelable[] relatedParcel = getIntent().getParcelableArrayExtra(RELATED_RECORDS);
		idc9Related = Arrays.copyOf(relatedParcel, relatedParcel.length, ICD9Related[].class);

		TextView actionTitleTextView = (TextView) findViewById(R.id.content_title);
		actionTitleTextView.setText(R.string.explore_icd_10);
		
		TextView titleView = (TextView) findViewById(R.id.title);

		fullTitle = StringUtils.userFriendlyICD(rootCode) + " - " + description;

		OptimizeHIT.sendScreen(
				GAnalyticsScreenNames.EXPLORE_ICD_10,
				fullTitle,
				null, null);

		titleView.setText(fullTitle);
		Button sideMenuButton = (Button) findViewById(R.id.left_button);
		sideMenuButton.setTypeface(FontsHelper.sharedHelper(this).fontello());
		Button superbillActionButton = (Button) findViewById(R.id.right_button);
		superbillActionButton.setTypeface(FontsHelper.sharedHelper(this).fontello());
		
		if (icdDatabase.hasSuperbill(rootCode)) {
			superbillActionButton.setText(R.string.icon_check);
		} else {
			superbillActionButton.setText(R.string.icon_plus);
		}

		if (savedInstanceState != null) {
			groupPickerShown = savedInstanceState.getBoolean(GROUP_PICKER_SHOWN);
			
			if (groupPickerShown) {
				showGroupPicker();
			}
		}

		if (savedInstanceState != null) {
			descriptionToAdd = savedInstanceState.getString(DESCRIPTION_TO_ADD);
			codeToAdd = savedInstanceState.getString(CODE_TO_ADD);
			billableToAdd = savedInstanceState.getBoolean(BILLABLE_TO_ADD, true);
			addIconPosition = savedInstanceState.getInt(ADDED_ICON_POSITION, -1);
		}

		if (!billable) {
			if (savedInstanceState != null) {
				currentLevel = savedInstanceState.getInt(HISTORY_LEVEL, 0);
				hasChanged = savedInstanceState.getBoolean(HAS_CHANGED, false);
				
				Stack<NavigationModel> serializable = (Stack<NavigationModel>) savedInstanceState.getSerializable(HISTORY);
				subordinateHistory = serializable;
			} else {
				currentLevel = 0;
				addIconPosition = -1;
				subordinateHistory = new Stack<NavigationModel>();
				
				ICDRecordExtended[] records = icdDatabase.getChildIcdCodes(rootCode);
				
				subordinateHistory.push(new NavigationModel(description, rootCode, records));
			}
			
			subOrdinateAdapters[0] = new SubordinateAdapter(this, subordinateHistory.peek().getSubordinates(), this);
			subOrdinateAdapters[1] = new SubordinateAdapter(this, new ICDRecordExtended[0], this);
			
			subordinateListViews[0] = (FractionListView) findViewById(R.id.subordinates_list_first);
			subordinateListViews[1] = (FractionListView) findViewById(R.id.subordinates_list_second);
			
			subordinateListViews[0].setTotalItemCount(subOrdinateAdapters[0].getCount());

			subordinateListViews[0].setOnItemClickListener(subordinateClickListener);
			subordinateListViews[1].setOnItemClickListener(subordinateClickListener);

			subordinateListViews[0].setAdapter(subOrdinateAdapters[0]);
			subordinateListViews[1].setAdapter(subOrdinateAdapters[1]);
			
			if (currentLevel == 0) {
				findViewById(R.id.back_navigation_section).setVisibility(View.GONE);
			} else {
				NavigationModel navigationModel = subordinateHistory.peek();
				
				TextView suboridinateTitle = (TextView) findViewById(R.id.subordinate_title);
				suboridinateTitle.setText(navigationModel.getCode() + " - " + navigationModel.getDescription());
			}
			
			RelativeLayout backButton = (RelativeLayout) findViewById(R.id.back_navigation_button);
			backButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.little_rounded_rect_orange, User.sharedUser(this).primaryColor()));
			backButton.setOnClickListener(backNavigationClickListener);
			
			if (savedInstanceState != null) {
				Parcelable listViewState = savedInstanceState.getParcelable(SUBORDINATE_LIST_POSITION);
				subordinateListViews[0].onRestoreInstanceState(listViewState);
			}
		} else {
			findViewById(R.id.subordinate_section).setVisibility(View.GONE);
		}
		
		if (icd10Info == null || icd10Info.length == 0) {
			findViewById(R.id.documentation_guidance_section).setVisibility(View.GONE);
		} else {
			if (savedInstanceState != null) {
				states = savedInstanceState.getBooleanArray(STATES);
			} else {
				states = new boolean[icd10Info.length];
			}
			
			LinearLayout guidanceList = (LinearLayout) findViewById(R.id.documentation_guidance_list);
			DocumentationInfoAdapter adapter = new DocumentationInfoAdapter(this, icd10Info, states);
			
			for (int position = 0 ; position < icd10Info.length ; position++) {
				View view = adapter.getView(position, commandClickListener);
				guidanceList.addView(view);
			}
		}
		
		if (idc9Related == null || idc9Related.length == 0) {
			findViewById(R.id.corresponding_icd9_list).setVisibility(View.GONE);
		} else {
			LinearLayout correspondingIcd9List = (LinearLayout) findViewById(R.id.corresponding_icd9_list);
			ICD9RecordListAdapter adapter = new ICD9RecordListAdapter(this, R.layout.item_icd_9_record, idc9Related);
			
			for (int position = 0 ; position < idc9Related.length ; position++) {
				View view = adapter.getView(position, null, null);
				correspondingIcd9List.addView(view);
			}
		}
		
		final int scrollY = savedInstanceState == null ? 0 : savedInstanceState.getInt(SCROLLVIEW_POSITION, 0);
		final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
		scrollView.post(new Runnable() {            
		    @Override
		    public void run() {
		    	scrollView.scrollTo(0, scrollY);
		    }
		});

		InputDialog inputDialog = (InputDialog) getSupportFragmentManager().findFragmentByTag("INPUT_DIALOG");
		
		if (inputDialog != null) {
			inputDialog.setHandler(this);
		}

		Typeface fontelloTypeface = FontsHelper.sharedHelper(this).fontello();
		TextView listBackIcon = (TextView) findViewById(R.id.back_icon);
		listBackIcon.setTypeface(fontelloTypeface);

	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putBoolean(BILLABLE_TO_ADD, billableToAdd);
		outState.putString(CODE_TO_ADD, codeToAdd);
		outState.putString(DESCRIPTION_TO_ADD, descriptionToAdd);
		
		outState.putInt(ADDED_ICON_POSITION, addIconPosition);
		
		outState.putBoolean(GROUP_PICKER_SHOWN, groupPickerShown);
		
		ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
		outState.putInt(SCROLLVIEW_POSITION, scrollView.getScrollY());
		
		if (subordinateListViews[currentListView] != null) {
			outState.putParcelable(SUBORDINATE_LIST_POSITION, subordinateListViews[currentListView].onSaveInstanceState());
		}
		
		outState.putInt(HISTORY_LEVEL, currentLevel);
		outState.putSerializable(HISTORY, subordinateHistory);
		outState.putBoolean(HAS_CHANGED, hasChanged);
		outState.putBooleanArray(STATES, states);
		
		super.onSaveInstanceState(outState);
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
	public void finish() {
		super.finish();
		
		overridePendingTransition(R.anim.hold, R.anim.slide_down_out);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		IntentFilter intentFilter = new IntentFilter(APITalker.ACTION_SUPERBILL_UPDATED);
		registerReceiver(superbillReloadedReceiver, intentFilter);
	}
	
	@Override
	protected void onPause() {
		unregisterReceiver(superbillReloadedReceiver);
		
		super.onPause();
	}
	
	public void superbillAction(View view) {
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}
		
		savedLastClickTime = System.currentTimeMillis();

		if (icdDatabase.hasSuperbill(rootCode)) {
			NotificationHelper.showNotification(getString(R.string.already_in_superbill), getString(R.string.code_is_already_in_superbill), false, this);
		} else {
			codeToAdd = rootCode;
			billableToAdd = billable;
			descriptionToAdd = description;
			addIconPosition = -2;

			if (settings.isGroupingEnabled()) {
				showGroupPicker();
			} else {
				YesNoDialog yesNoDialog = new YesNoDialog();
				yesNoDialog.setupDialog(getString(R.string.add_to_superbill_question), String.format(getString(R.string.do_you_want_add_to_superbill), StringUtils.userFriendlyICD(rootCode)));
				yesNoDialog.setHandler(this);
				yesNoDialog.show(getSupportFragmentManager(), "YesNoDialog");
			}
		}
	}
	
	public void backButtonClicked(View view) {
		if (System.currentTimeMillis() - savedLastClickTime < 200) {
			return;
		}
		
		savedLastClickTime = System.currentTimeMillis();

		Intent resultIntent = new Intent();
		resultIntent.putExtra(FindCodeActivity.HAS_CHANGED, true);
		setResult(RESULT_OK, resultIntent);
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
		subOrdinateAdapters[currentListView].notifyDataSetChanged();
		subordinateListViews[currentListView].setTotalItemCount(subOrdinateAdapters[currentListView].getCount());

		Button superbillActionButton = (Button) findViewById(R.id.right_button);
		
		if (icdDatabase.hasSuperbill(rootCode)) {
			superbillActionButton.setText(R.string.icon_check);
		} else {
			superbillActionButton.setText(R.string.icon_plus);
		}
	}
	
	/**
	 * YesNoDialogListener Methods
	 */

	@Override
	public void onDialogAction(String actionCode) {

		if (actionCode.equals(YesNoDialog.ACTION_YES) && codeToAdd != null) {

			icdDatabase.addSuperbill(
					codeToAdd,
					"ungrouped",
					descriptionToAdd,
					billableToAdd,
					0,
					null);

			// TODO: ASK IF IT NEEDED TO SEND EVENT HERE.

			hasChanged = true;
			
			if (addIconPosition == -2) {
				Button superbillActionButton = (Button) findViewById(R.id.right_button);
				superbillActionButton.setText(R.string.icon_check);
			} else if (addIconPosition >= 0) {
				subOrdinateAdapters[currentListView].notifyDataSetChanged();
				subordinateListViews[currentListView].setTotalItemCount(subOrdinateAdapters[currentListView].getCount());
			}

		}
		
		codeToAdd = null;


        if( actionCode.equals( YesNoDialog.ACTION_MORE_INFO ) ){

            onFrontNavigation( true, clickedItemCode, clickedItemDescription );

//            processingDialog.show();

        }

	}

	/**
	 * SubordinateAdapterListener Methods
	 */

	@Override
	public void onFrontNavigation(boolean canNavigate, String code, String description) {

        clickedItemCode = code;
        clickedItemDescription = description;

		if (canNavigate) {
			int nextListView = (currentListView + 1) % 2;
//			AnimatorSet animatorSet = new AnimatorSet();
//			animatorSet
//				.play(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "xFraction", 0, -1))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "y", 0, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "alpha", 1, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "scaleY", 1, 0.5f))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "xFraction", 1, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "y", 0, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "alpha", 0, 1))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "scaleY", 1.5f, 1));
//			animatorSet.setDuration(250);
//			animatorSet.start();

            subordinateListViews[currentListView].setVisibility( View.GONE );
            subordinateListViews[nextListView].setVisibility( View.VISIBLE );

			TextView suboridinateTitle = (TextView) findViewById(R.id.subordinate_title);
			suboridinateTitle.setText(StringUtils.userFriendlyICD(code) + " - " + description);
			
			ICDRecordExtended[] subSuperbills = icdDatabase.getChildIcdCodes(code);
			subordinateHistory.push(new NavigationModel(description, code, subSuperbills));
			currentLevel++;
			subOrdinateAdapters[nextListView].setItems(subSuperbills);
			subordinateListViews[nextListView].setVisibility(View.VISIBLE);
			subordinateListViews[nextListView].setTotalItemCount(subOrdinateAdapters[nextListView].getCount());
			currentListView = nextListView;
			
			findViewById(R.id.back_navigation_section).setVisibility(View.VISIBLE);
		} else {
			NotificationHelper.showNotification( getString(R.string.title_billable_code),
                    fullTitle + "\n\n" + getString(R.string.message_billable_code), false, this);
		}

	}

	@Override
	public void onSubordinateAction(boolean canAdd, String code, String description, boolean billable, int ordering, int position) {
		if (!canAdd) {
			NotificationHelper.showNotification(getString(R.string.already_in_superbill), getString(R.string.code_is_already_in_superbill), false, this);
		} else {
			codeToAdd = code;
			descriptionToAdd = description;
			billableToAdd = billable;
			addIconPosition = position;
			
			if (settings.isGroupingEnabled()) {
				showGroupPicker();
			} else {
				YesNoDialog yesNoDialog = new YesNoDialog();
				yesNoDialog.setupDialog(getString(R.string.add_to_superbill_question), String.format(getString(R.string.do_you_want_add_to_superbill), StringUtils.userFriendlyICD(code)));
				yesNoDialog.setHandler(this);
				yesNoDialog.show(getSupportFragmentManager(), "YesNoDialog");
			}
		}
	}
	
	private OnItemClickListener subordinateClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if (System.currentTimeMillis() - savedLastClickTime < 200) {
				return;
			}
			
			savedLastClickTime = System.currentTimeMillis();

			IDCRecord record = subOrdinateAdapters[currentListView].getItem(position);

			onFrontNavigation( !record.getBillable(), record.getCode(), record.getDescription() );

		}

	};
	
	/**
	 * Navigation Click Listener
	 */
	
	private OnClickListener backNavigationClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			if (System.currentTimeMillis() - savedLastClickTime < 200) {
				return;
			}
			
			savedLastClickTime = System.currentTimeMillis();

			int nextListView = (currentListView + 1) % 2;
//			AnimatorSet animatorSet = new AnimatorSet();
//			animatorSet
//				.play(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "xFraction", -1, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "y", 0, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "alpha", 0, 1))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[nextListView], "scaleY", 0.5f, 1))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "xFraction", 0, 1))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "y", 0, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "alpha", 1, 0))
//				.with(ObjectAnimator.ofFloat(subordinateListViews[currentListView], "scaleY", 1, 1.5f));
//			animatorSet.setDuration(250);
//			animatorSet.start();

            subordinateListViews[currentListView].setVisibility( View.GONE );
            subordinateListViews[nextListView].setVisibility( View.VISIBLE );

			subordinateHistory.pop();
			NavigationModel navigationModel = subordinateHistory.peek();
			
			TextView suboridinateTitle = (TextView) findViewById(R.id.subordinate_title);
			suboridinateTitle.setText(StringUtils.userFriendlyICD(navigationModel.getCode()) + " - " + navigationModel.getDescription());


			currentLevel--;
			subOrdinateAdapters[nextListView].setItems(navigationModel.getSubordinates());
			subordinateListViews[nextListView].setVisibility(View.VISIBLE);
			subordinateListViews[nextListView].setTotalItemCount(subOrdinateAdapters[nextListView].getCount());
			currentListView = nextListView;
			
			if (currentLevel == 0) {
				findViewById(R.id.back_navigation_section).setVisibility(View.GONE);
			}
		}
	};
	
	private View.OnClickListener commandClickListener = new View.OnClickListener() {
		
		@Override
		public void onClick(View view) {
			if (System.currentTimeMillis() - savedLastClickTime < 200) {
				return;
			}
			
			savedLastClickTime = System.currentTimeMillis();

			final int position = ((Integer) view.getTag()).intValue();
			states[position] = !states[position];
			
			LinearLayout guidanceList = (LinearLayout) findViewById(R.id.documentation_guidance_list);
			final LinearLayout container = (LinearLayout) guidanceList.getChildAt(position).findViewById(R.id.guidance_content);
			
			TextView command = (TextView) guidanceList.getChildAt(position).findViewById(R.id.command);
			command.setText(states[position] ? "(close)" : "(expand)");

			Animation animation = new ScaleAnimation(1.0f, 1.0f, states[position] ? 0.0f : 1.0f, states[position] ?  1.0f : 0.0f);
			animation.setDuration(200);
			animation.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {
					
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if (!states[position]) {
						container.setVisibility(View.GONE);
					}
				}
			});
			
			container.startAnimation(animation);
			
			if (states[position]) {
				container.setVisibility(View.VISIBLE);
			}
		}
	};
	
	/**
	 * PickerListener Methods
	 */

	@Override
	public void onPick(String data) {
		groupPickerShown = false;

		if (data.equals(getString(R.string.create_group))) {
			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.ADD_NEW_GROUP.CATEGORY,
					GAnalitycsEventNames.ADD_NEW_GROUP.ACTION,
					GAnalitycsEventNames.ADD_NEW_GROUP.LABEL);

			Bundle arguments = new Bundle();
			arguments.putString(InputDialog.TITLE, getString(R.string.name_group));
			arguments.putString(InputDialog.DESCRIPTION, "");
			arguments.putString(InputDialog.OK_BUTTON, getString(R.string.add));
			arguments.putString(InputDialog.ERROR_DESCRIPTION, getString(R.string.group_name_cannot_blank));
			arguments.putString(InputDialog.ERROR_DEFAULT, "Ungrouped");
			arguments.putInt(InputDialog.INVALID_MIN, 1);
			InputDialog inputDialog = new InputDialog();
			inputDialog.setHandler(this);
			inputDialog.setArguments(arguments);
			inputDialog.show(getSupportFragmentManager(), "INPUT_DIALOG");
		} else {
			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.ADD_TO_SUPERBILL.CATEGORY,
					GAnalitycsEventNames.ADD_TO_SUPERBILL.ACTION,
					GAnalitycsEventNames.ADD_TO_SUPERBILL.LABEL);

			icdDatabase.addSuperbill(codeToAdd, data.toLowerCase(Locale.US), descriptionToAdd, billableToAdd, 0, null);
			
			hasChanged = true;
			
			if (addIconPosition == -2) {
				Button superbillActionButton = (Button) findViewById(R.id.right_button);
				superbillActionButton.setText(R.string.icon_check);
			} else if (addIconPosition >= 0) {
				subOrdinateAdapters[currentListView].notifyDataSetChanged();
				subordinateListViews[currentListView].setTotalItemCount(subOrdinateAdapters[currentListView].getCount());
			}

			codeToAdd = null;
		}
	}
	
	@Override
	public void onPickerDismissed() {
		groupPickerShown = false;
	}
	
	private void showGroupPicker() {
		groupPickerShown = true;
		
		ArrayList<ICDRecordExtended> recordsList = icdDatabase.getUserGroups();
		String[] groups = new String[recordsList.size() + 1];
		int index = 0;
		
		for (ICDRecordExtended record : recordsList) {
			groups[index] = StringUtils.capitalizeWords(record.getCode());
			index++;
		}
		
		groups[index] = getString(R.string.create_group);

		PickerDialog pickerDialog = new PickerDialog(this, this);
		pickerDialog.setArguments(groups, getString(R.string.add_to_superbill_group));
		pickerDialog.show();
	}
	
	/**
	 * InputDialogHandler Methods
	 */

	@Override
	public void onInputSubmitted(String input) {
		icdDatabase.addSuperbill(codeToAdd, input.toLowerCase(Locale.US), descriptionToAdd, billableToAdd, 0, null);

		hasChanged = true;
		
		if (addIconPosition == -2) {
			Button superbillActionButton = (Button) findViewById(R.id.right_button);
			superbillActionButton.setText(R.string.icon_check);
		} else if (addIconPosition >= 0) {
			subOrdinateAdapters[currentListView].notifyDataSetChanged();
			subordinateListViews[currentListView].setTotalItemCount(subOrdinateAdapters[currentListView].getCount());
		}

		OptimizeHIT.sendEvent(
				GAnalitycsEventNames.ADD_TO_SUPERBILL.CATEGORY,
				GAnalitycsEventNames.ADD_TO_SUPERBILL.ACTION,
				GAnalitycsEventNames.ADD_TO_SUPERBILL.LABEL);

		codeToAdd = null;
	}

	@Override
	public void onInputCanceled() {
		codeToAdd = null;
	}

	/**
	 * BroadcastReceivers
	 */
	
	private BroadcastReceiver superbillReloadedReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			refreshStateAfterLogin(false);
		}

	};

}