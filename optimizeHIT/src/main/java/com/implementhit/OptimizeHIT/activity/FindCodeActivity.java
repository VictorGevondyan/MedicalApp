package com.implementhit.OptimizeHIT.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.SubordinateAdapter;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.ExploreSuperbillRequestListener;
import com.implementhit.OptimizeHIT.api.FindCodeRequestListener;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.database.ICDDatabase;
import com.implementhit.OptimizeHIT.dialogs.InputDialog;
import com.implementhit.OptimizeHIT.dialogs.InputDialog.InputDialogHandler;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.dialogs.PickerDialog;
import com.implementhit.OptimizeHIT.dialogs.PickerDialog.PickerListener;
import com.implementhit.OptimizeHIT.dialogs.SuperbillActionDialog;
import com.implementhit.OptimizeHIT.dialogs.SuperbillActionDialog.SuperbillActionListener;
import com.implementhit.OptimizeHIT.dialogs.YesNoDialog;
import com.implementhit.OptimizeHIT.models.ICD9Related;
import com.implementhit.OptimizeHIT.models.ICDAdditionalInfo;
import com.implementhit.OptimizeHIT.models.ICDRecordExtended;
import com.implementhit.OptimizeHIT.models.IDCRecord;
import com.implementhit.OptimizeHIT.models.NavigationModel;
import com.implementhit.OptimizeHIT.models.Settings;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.CustomEditText;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.StringUtils;
import com.implementhit.OptimizeHIT.views.FractionListView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;

public class FindCodeActivity extends SuperActivity implements OnClickListener,
		FindCodeRequestListener, SubordinateAdapter.SubordinateAdapterListener, SuperbillActionListener,
		ExploreSuperbillRequestListener, PickerListener, InputDialogHandler, OnEditorActionListener,
        YesNoDialog.YesNoDialogListener {

	public static final String NEW_SUPERBILL_ACTION = "newSuperbillAction";
	public static final int REQUEST_CODE = 2913;

	public static final String HAS_CHANGED = "hasChanged";

	private final String SAVE_IS_BILLABLE = "saveIsBillable";
	private final String SAVE_FIND_CODE_QUESTION = "saveFindCodeQuestion";
	private final String SAVE_CURSOR_POSITION = "saveCursorPosition";

	private final String CURRENT_GROUP = "currentGroup";
	private final String ORDERING_TP_ADD = "orderingToAdd";
	private final String CODE_TO_ADD = "codeToAdd";
	private final String DESCRIPTION_TO_ADD = "descriptionToAdd";
	private final String BILLABLE_TO_ADD = "billableToAdd";
	private final String ADDED_ICON_POSITION = "addedIconPosition";
	private final String SCROLLVIEW_POSITION = "scrollViewPosition";
	private final String HISTORY_LEVEL = "historyLevel";
	private final String HISTORY = "history";
	private final String IS_ACTION_DIALOG_OPEN = "isActionDialogOpen";
	private final String GROUP_PICKER_SHOWN = "saveGroupPickerShown";
	private final String GROUP_PICKER_TITLE = "saveGroupPickerTitle";
	private final String ACTION_DIALOG_SUPPLEMENT_ACTION = "saveActionTitleSupplementAction";
	private final String ACTION_DIALOG_MAY_REMOVE = "actionDialogMayRemove";

    // String constants for info of the item, which "$" sign is clicked. We need it for handling appeared dialog's "More Info"
    // button click
    private final String CLICKED_ITEM_CODE = "clickedItemCode";
    private final String CLICKED_ITEM_DESCRIPTION = "clickedItemDescription";

	private ScrollView findCodeScrollView;
	private LoadingDialog processingDialog;

    /**
     * We have two List Views. If we click first list's item, the second List View appears.
     * They hide and appear with view.setVisibility( GONE ( respectively VISIBLE ) ) calls.
     * Both Lists are instances of custom ListView, called FractionListView.
     * FractionListView's height is set based on it's children's count and height.
     * It is done so, because we want that the ListView would not have been scrollable,
     * and the entire page scrolls, when we attempt to scroll list view.
     * ( If it has the items, that not fit on the screen, of course :) ).
     */
	private FractionListView[] subordinateListViews = new FractionListView[2];
	private SubordinateAdapter[] subOrdinateAdapters = new SubordinateAdapter[2];
	private int currentListView;

    /**
     * As we can have several lists, and with pressing the item of one list,
     * we open another list, we can say, that there is several levels of lists navigation.
     * These variables represent that levels
     */
    private int currentLevel;
	private int superbillLevel;

    /**
     * Stack of navigation history between lists. Each element of stack represents one list's data.
     */
	private Stack<NavigationModel> subordinateHistory;
	private String currentGroup = null;

	private String codeToAdd;
	private String descriptionToAdd;
	private int orderingToAdd = 0;
	private int addIconPosition = -1;
	private boolean billableToAdd = true;

    private String clickedItemCode;
    private String clickedItemDescription;

    /**
     * This Dialog is intended for adding or removing the list item from the initial level list,
     * adding it to the group ( with creation of new group first , if needed), etc..
     * The dialog is shown by clicking " ... " button.
     */
	private SuperbillActionDialog superbillActionDialog;


    /**
     * This dialog is called, when " Move to Group " button of SuperbillActionDialog is clicked.
     * It is intended for moving the List item to the group, or create a new group.
     * When the item from PickerDialog is selected, the InputDialog appears. ( see onInput...() methods ).
     */
    private PickerDialog groupPicker;

	private boolean isActionDialogOpen = false;
	private boolean groupPickerShown = false;

	private Settings settings;
	private ICDDatabase icdDatabase;
	private AtomicBoolean listItemClicked;

	public FindCodeActivity() {
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		OptimizeHIT.sendScreen(
				GAnalyticsScreenNames.FIND_CODE_SCREEN,
				null,
				null, null);

		setContentView(R.layout.activity_find_the_code);

		Button searchButton = (Button) findViewById(R.id.button_search);
		searchButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.little_rounded_rect_orange, User.sharedUser(this).primaryColor()));
		searchButton.setOnClickListener(this);

		findCodeScrollView = (ScrollView) findViewById(R.id.find_code_scroll);

		settings = DBTalker.sharedDB(this).initSettings();

		trackKeyboard();

		CustomEditText questionText = (CustomEditText) findViewById(R.id.question);
		questionText.setFocusable(true);
		questionText.setFocusableInTouchMode(true);
		questionText.addTextChangedListener(queryTextWatcher);
		questionText.setRawInputType(InputType.TYPE_CLASS_TEXT);
		questionText.setOnEditorActionListener(this);

		icdDatabase = ICDDatabase.sharedDatabase(this);

		SwitchCompat billableSwitch = (SwitchCompat) findViewById(R.id.find_billable_switch);
		billableSwitch.setOnTouchListener(switchTouchListener);
		ColorUtil.changeSwitchColor(billableSwitch, User.sharedUser(this).primaryColor());

		if (savedInstanceState != null) {
			if (questionText != null && questionText.length() > 0) {
				questionText.setText(savedInstanceState.getString(SAVE_FIND_CODE_QUESTION));
				questionText.setSelection(savedInstanceState.getInt(SAVE_CURSOR_POSITION));
			}

			billableSwitch.setChecked(savedInstanceState.getBoolean(SAVE_IS_BILLABLE, false));
			isActionDialogOpen = savedInstanceState.getBoolean(IS_ACTION_DIALOG_OPEN);

			if (isActionDialogOpen) {
				String actionTitle = savedInstanceState.getString(ACTION_DIALOG_SUPPLEMENT_ACTION);
				boolean canRemove = savedInstanceState.getBoolean(ACTION_DIALOG_MAY_REMOVE);

				superbillActionDialog = new SuperbillActionDialog(this, this);
				superbillActionDialog.canRemove(canRemove);

				if (actionTitle != null && !actionTitle.isEmpty()) {
					superbillActionDialog.setAdditionalAction(actionTitle);
				}

				superbillActionDialog.show();
			}

			groupPickerShown = savedInstanceState.getBoolean(GROUP_PICKER_SHOWN);

			if (groupPickerShown) {
				String groupPickerTitle = savedInstanceState.getString(GROUP_PICKER_TITLE);

				showGroupPicker(groupPickerTitle);
			}

			descriptionToAdd = savedInstanceState.getString(DESCRIPTION_TO_ADD);
			codeToAdd = savedInstanceState.getString(CODE_TO_ADD);
			billableToAdd = savedInstanceState.getBoolean(BILLABLE_TO_ADD, true);
			addIconPosition = savedInstanceState.getInt(ADDED_ICON_POSITION, -1);
			orderingToAdd = savedInstanceState.getInt(ORDERING_TP_ADD, 0);
			currentLevel = savedInstanceState.getInt(HISTORY_LEVEL, 0);
			currentGroup = savedInstanceState.getString(CURRENT_GROUP, null);

            // We get saved info of item, which "$" sign is clicked, to handle
            // corresponding dialog appearance
            clickedItemCode = savedInstanceState.getString(CLICKED_ITEM_CODE);
            clickedItemDescription = savedInstanceState.getString(CLICKED_ITEM_DESCRIPTION);

			if (settings.isGroupingEnabled()) {
				superbillLevel = 1;
			} else {
				superbillLevel = 0;
			}

			Serializable serializable = savedInstanceState.getSerializable(HISTORY);

			if (serializable instanceof Stack<?>) {
				subordinateHistory = (Stack<NavigationModel>) serializable;
			} else {

				subordinateHistory = new Stack<>();
				ArrayList<NavigationModel> navigationArray = (ArrayList<NavigationModel>) serializable;

				for (NavigationModel model : navigationArray) {
					subordinateHistory.push(model);
				}

			}

		} else {

			currentLevel = 0;
			addIconPosition = -1;
			subordinateHistory = new Stack<>();

			if (settings.isGroupingEnabled()) {

				superbillLevel = 1;

				ArrayList<ICDRecordExtended> recordsList = icdDatabase.getUserGroups();
				ICDRecordExtended[] records = new ICDRecordExtended[recordsList.size()];
				recordsList.toArray(records);

				subordinateHistory.push(new NavigationModel(null, null, records));

			} else {

				superbillLevel = 0;

				ArrayList<ICDRecordExtended> recordsList = icdDatabase.getUserSuperbill(null);
				ICDRecordExtended[] records = new ICDRecordExtended[recordsList.size()];
				recordsList.toArray(records);

				subordinateHistory.push(new NavigationModel(null, null, records));

			}

			if (icdDatabase.getUserSuperbill(null).size() == 0) {
				findViewById(R.id.no_result).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.no_result).setVisibility(View.GONE);
			}

		}

		subOrdinateAdapters[0] = new SubordinateAdapter(this, subordinateHistory.peek().getSubordinates(), this);
		subOrdinateAdapters[1] = new SubordinateAdapter(this, new ICDRecordExtended[0], this);

		subOrdinateAdapters[0].setIsRemovable(true);
		subOrdinateAdapters[1].setIsRemovable(true);

		subordinateListViews[0] = (FractionListView) findViewById(R.id.subordinates_list_first);
		subordinateListViews[1] = (FractionListView) findViewById(R.id.subordinates_list_second);

		subordinateListViews[0].setAdapter(subOrdinateAdapters[0]);
		subordinateListViews[1].setAdapter(subOrdinateAdapters[1]);

		subordinateListViews[0].setOnItemClickListener(subordinateClickListener);
		subordinateListViews[1].setOnItemClickListener(subordinateClickListener);


		FractionListView.setListViewHeightBasedOnChildren(subordinateListViews[0]);
		FractionListView.setListViewHeightBasedOnChildren(subordinateListViews[1]);

		subordinateListViews[0].setIsScrollable(false);
		subordinateListViews[1].setIsScrollable(false);

		changeNavBar();

		if (currentLevel == 0 && settings.isDisplayingEnableGrouping() && !settings.isGroupingEnabled()) {
			findViewById(R.id.no_grouping).setVisibility(View.VISIBLE);
		} else {
			findViewById(R.id.no_grouping).setVisibility(View.INVISIBLE);
		}

		RelativeLayout backNavigationButton = (RelativeLayout) findViewById(R.id.back_navigation_button);
		backNavigationButton.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.little_rounded_rect_orange, User.sharedUser(this).primaryColor()));
		backNavigationButton.setOnClickListener(backNavigationClickListener);

		processingDialog = new LoadingDialog(this);

		LinearLayout searchContainer = (LinearLayout) findViewById(R.id.find_code_container);
		searchContainer.setFocusable(true);
		searchContainer.setFocusableInTouchMode(true);
		searchContainer.requestFocus();
		searchContainer.requestFocus();

		Typeface fontelloTypeface = FontsHelper.sharedHelper(this).fontello();

		TextView clearButton = (TextView) findViewById(R.id.clear_icon);
		clearButton.setOnClickListener(this);
		clearButton.setTypeface(fontelloTypeface);

		InputDialog inputDialog = (InputDialog) getSupportFragmentManager().findFragmentByTag("INPUT_DIALOG");

		if (inputDialog != null) {
			inputDialog.setHandler(this);
		}

		listItemClicked = new AtomicBoolean();

		LinearLayout leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
		leftButtonLayout.setVisibility(View.VISIBLE);

		Button backButton = (Button) findViewById(R.id.left_button);
		backButton.setTypeface(fontelloTypeface);
		backButton.setText(R.string.icon_angle_left);
		backButton.setOnClickListener(this);

		TextView listBackIcon = (TextView) findViewById(R.id.back_icon);
		listBackIcon.setTypeface(fontelloTypeface);

		TextView titleTextView = (TextView) findViewById(R.id.content_title);
		titleTextView.setText(R.string.find_the_code);

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		CustomEditText questionText = (CustomEditText) findViewById(R.id.question);
		SwitchCompat billableSwith = (SwitchCompat) findViewById(R.id.find_billable_switch);

		outState.putString(SAVE_FIND_CODE_QUESTION, questionText.getText().toString());
		outState.putInt(SAVE_CURSOR_POSITION, questionText.getSelectionStart());
		outState.putBoolean(SAVE_IS_BILLABLE, billableSwith.isChecked());

		if (groupPicker != null) {
			outState.putBoolean(GROUP_PICKER_SHOWN, groupPickerShown);
			outState.putString(GROUP_PICKER_TITLE, groupPicker.getPickerTitle());
		}

		outState.putBoolean(BILLABLE_TO_ADD, billableToAdd);
        outState.putString(CODE_TO_ADD, codeToAdd);
        outState.putString(DESCRIPTION_TO_ADD, descriptionToAdd);
		outState.putInt(ADDED_ICON_POSITION, addIconPosition);
		outState.putInt(ORDERING_TP_ADD, orderingToAdd);

        // Save the info of item, which "$" sign is clicked, in order to handle
        // the corresponding dialog during device rotate
        outState.putString(CLICKED_ITEM_CODE, clickedItemCode);
        outState.putString(CLICKED_ITEM_DESCRIPTION, clickedItemDescription);

		outState.putString(CURRENT_GROUP, currentGroup);

		outState.putInt(SCROLLVIEW_POSITION, findCodeScrollView.getScrollY());
		outState.putInt(HISTORY_LEVEL, currentLevel);
		outState.putSerializable(HISTORY, subordinateHistory);

		if (superbillActionDialog != null) {
			outState.putString(ACTION_DIALOG_SUPPLEMENT_ACTION,superbillActionDialog.getAdditionalAction());
			outState.putBoolean(ACTION_DIALOG_MAY_REMOVE, superbillActionDialog.getCanRemove());
			outState.putBoolean(IS_ACTION_DIALOG_OPEN, isActionDialogOpen);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onResume() {
		super.onResume();

		IntentFilter newSuperbillFilter = new IntentFilter(NEW_SUPERBILL_ACTION);
		registerReceiver(newSuperbillBroadcastReceiver, newSuperbillFilter);

		IntentFilter supebillUpdateFilter = new IntentFilter(APITalker.ACTION_SUPERBILL_UPDATED);
		registerReceiver(superbillReloadedReceiver, supebillUpdateFilter);
	}

	@Override
	protected void refreshStateAfterLogin(boolean isInitialLogin) {
		
	}

	@Override
	public void onPause() {
		unregisterReceiver(newSuperbillBroadcastReceiver);
		unregisterReceiver(superbillReloadedReceiver);

		super.onPause();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		ICDCodesListActivity.records = null;

		if (requestCode == REQUEST_CODE) {
			if (data != null && data.getBooleanExtra(HAS_CHANGED, false)) {
				reloadSuperbill();
			}
		}

	}

	@Override
	public void onClick(View view) {

		if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
			return;
		}

		if(listItemClicked.get()){
			return;
		}

		listItemClicked.set(true);

		SuperActivity.savedLastClickTime = System.currentTimeMillis();

		if(view.getId() == R.id.button_search) {
			queryCodes();
		} else if (view.getId() == R.id.clear_icon) {
			CustomEditText queryCustomEditText = (CustomEditText) findViewById(R.id.question);
			queryCustomEditText.setText("");
		} else if (view.getId() == R.id.left_button) {
			onBackPressed();
		}

		unblockClicks();

	}

	public void queryCodes() {
		CustomEditText queryCustomEditText = (CustomEditText) findViewById(R.id.question);
		String searchWord = queryCustomEditText.getText().toString();

		if (searchWord.length() == 0) {
			Locker.unlock(this);

			NotificationHelper.showNotification(
					getString(R.string.error),
					getString(R.string.type_to_search_icd),
					true,
					this);

			return;
		}

		Locker.lock(this);

		//showProgressDialog();
        processingDialog.show();
        processingDialog.setTitle(R.string.searching_for_code);

		SwitchCompat billableSwitch = (SwitchCompat) findViewById(R.id.find_billable_switch);
		APITalker.sharedTalker().findCode(User.sharedUser(this).hash(), searchWord, billableSwitch.isChecked(), this);
	}

	@Override
	public void findCodeSuccess(IDCRecord[] records) {
		processingDialog.dismiss();

		// TODO: EXPLORE CRASH
		ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up_in, R.anim.hold);
		Intent icdCodesIntent = new Intent(this, ICDCodesListActivity.class);
		ICDCodesListActivity.records = records;
		startActivityForResult(icdCodesIntent, REQUEST_CODE, options.toBundle());

		Locker.unlock(this);
	}

	@Override
	public void findCodeFailure(String error) {
		processingDialog.dismiss();

		NotificationHelper.showNotification(error, this);

		Locker.unlock(this);
	}

	/**
	 * Navigation Bar Methods
	 */

	public void changeNavBar() {

		if (currentLevel == 0) {
			findViewById(R.id.back_navigation_section).setVisibility(View.GONE);
		} else {

			String message;

			if (currentLevel == 1 && settings.isGroupingEnabled()) {
				message = getResources().getString(R.string.group) + " " + StringUtils.capitalizeWords(currentGroup);
			} else {
				NavigationModel navigationModel = subordinateHistory.peek();
				message = StringUtils.userFriendlyICD(navigationModel.getCode());

				message = message + " - " + navigationModel.getDescription();
			}

			TextView suboridinateTitle = (TextView) findViewById(R.id.subordinate_title);
			findViewById(R.id.back_navigation_section).setVisibility(View.VISIBLE);
			suboridinateTitle.setText(message);

		}

	}

	/**
	 * Soft Keyboard Helpers
	 */

	public void trackKeyboard() {
		final View activityRootView = findViewById(R.id.find_code_scroll);
		activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
			    activityRootView.requestLayout();
			}
		});
	}

	public void closeKeyboardSoft() {
		LinearLayout searchPanel = (LinearLayout) findViewById(R.id.find_code_container);
		searchPanel.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
		}
	}

	/**
	 * SubordinateAdapterListener Methods
	 */

	@Override
	public void onFrontNavigation(boolean canNavigate, String code, String description) {

        clickedItemCode = code;
        clickedItemDescription = description;

		if( listItemClicked.get() ){
			return;
		}

		listItemClicked.set(true);

		closeKeyboardSoft();

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


			ICDRecordExtended[] subSuperbills;

			if (description == null) {
				ArrayList<ICDRecordExtended> recordList = icdDatabase.getUserSuperbill(code);
				subSuperbills = new ICDRecordExtended[recordList.size()];
				recordList.toArray(subSuperbills);
				currentGroup = code;
			} else {
				subSuperbills = icdDatabase.getChildIcdCodes(code);
			}

			subordinateHistory.push(
					new NavigationModel(
							description,
							code,
							subSuperbills
					));
			currentLevel++;

			subOrdinateAdapters[nextListView].setItems(subSuperbills);
            subordinateListViews[nextListView].setVisibility(View.VISIBLE);
			FractionListView.setListViewHeightBasedOnChildren(subordinateListViews[nextListView]);
			currentListView = nextListView;

			findCodeScrollView.scrollTo(0, findCodeScrollView.getTop());

			changeNavBar();

		} else {
			String codeFullTitle = StringUtils.userFriendlyICD( code ) + " - " + description;
            showBillableDialog( codeFullTitle );
		}

		unblockClicks();

	}

    private void showBillableDialog( String codeFullTitle ){

        YesNoDialog yesNoDialog = new YesNoDialog();
        yesNoDialog.setupDialog(
                getString(R.string.title_billable_code),
                codeFullTitle + "\n\n" + getString(R.string.message_billable_code));
        yesNoDialog.setHandler( FindCodeActivity.this );
        yesNoDialog.show(getSupportFragmentManager(), "yesNoDialog");

    }

    @Override
    public void onDialogAction(String actionCode) {

        if( actionCode.equals( YesNoDialog.ACTION_MORE_INFO ) ){

            //showProgressDialog();
            processingDialog.show();
            processingDialog.setTitle(R.string.loading);
            APITalker.sharedTalker().exploreSuperbill( User.sharedUser(this).hash(), clickedItemCode,
                    clickedItemDescription, this );

        }

    }

    /**
     * This dialog is shown when user clicks to a billable ( marked with "$" sign ) item in a list.
     */

    @Override
	public void onSubordinateAction(boolean canAdd, String code, String description, boolean billable, int ordering, int position) {
		if (listItemClicked.get()) {
			return;
		}

		listItemClicked.set(true);

		closeKeyboardSoft();

		superbillActionDialog = new SuperbillActionDialog(this, this);

		if (!canAdd) {
			codeToAdd = code;
			descriptionToAdd = description;
			addIconPosition = position;
			orderingToAdd = ordering;

			superbillActionDialog = new SuperbillActionDialog(this, this);
			superbillActionDialog.canRemove(true);

			if (settings.isGroupingEnabled()) {
				superbillActionDialog.setAdditionalAction(getString(R.string.move_to_group));
			}

			isActionDialogOpen = true;
		} else {
			codeToAdd = code;
			descriptionToAdd = description;
			billableToAdd = billable;
			addIconPosition = position;
			orderingToAdd = 0;

			superbillActionDialog.canRemove(false);

			if (settings.isGroupingEnabled()) {
				superbillActionDialog.setAdditionalAction(getString(R.string.add_to_superbill_question));
			} else {
				superbillActionDialog.setAdditionalAction(getString(R.string.add_to_main_superbill));
			}

			isActionDialogOpen = true;
		}

        // Determine the list level in which Superbill Action Dialog must have "Remove From Superbill" button
        int levelWithRemoveSuperbill = settings.isGroupingEnabled()? 1 : 0 ;

        if( currentLevel > levelWithRemoveSuperbill ) {
            superbillActionDialog.canRemove(false);

            if (settings.isGroupingEnabled()) {
                superbillActionDialog.setAdditionalAction(getString(R.string.add_to_superbill_question));
            } else {
                superbillActionDialog.setAdditionalAction(getString(R.string.add_to_main_superbill));
            }

        }

		String fullDescription = code + " - " + description;
		superbillActionDialog.setSubtitle(fullDescription);

        superbillActionDialog.show();

		unblockClicks();
	}

	private void unblockClicks() {
		final int listItemLockInterval = 200;
		Handler handler = new Handler();
		final Runnable runnable = new Runnable() {
			public void run() {
				listItemClicked.set(false);
			}
		};
		handler.postDelayed(runnable, listItemLockInterval);
	}

	/**
	 * Navigation Click Listener
	 */

	private OnClickListener backNavigationClickListener = new OnClickListener() {

		@Override
		public void onClick(View view) {

			if (view != null && System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
				return;
			}

			SuperActivity.savedLastClickTime = System.currentTimeMillis();

			closeKeyboardSoft();

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
			currentLevel--;

			changeNavBar();

			if (currentLevel == 0
					|| (settings.isGroupingEnabled() && currentLevel == 1)) {
				reloadSuperbill();
			}

			NavigationModel navigationModel = subordinateHistory.peek();

			subOrdinateAdapters[nextListView].setItems(navigationModel.getSubordinates());

            subordinateListViews[nextListView].setVisibility(View.VISIBLE);

			FractionListView.setListViewHeightBasedOnChildren(subordinateListViews[nextListView]);

			currentListView = nextListView;

            unblockClicks();

		}

	};

	/**
	 * Switch Click Listener
	 */

	OnTouchListener switchTouchListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View view, MotionEvent event) {

			if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
				return false;
			}

			SuperActivity.savedLastClickTime = System.currentTimeMillis();
			view.performClick();
			return true;

		}

	};

	/**
	 * SuperbillActionListener Methods
	 */

	@Override
	public void onSuperbillDialogAction(String action) {
		closeKeyboardSoft();

		if (action.equals(SuperbillActionDialog.ACTION_EXPLORE)) {

			Locker.lock(this);

			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.EXPLORE_ICD_10.CATEGORY,
					GAnalitycsEventNames.EXPLORE_ICD_10.ACTION,
					GAnalitycsEventNames.EXPLORE_ICD_10.LABEL);

			//showProgressDialog();
            processingDialog.show();
            processingDialog.setTitle(R.string.loading);
			APITalker.sharedTalker().exploreSuperbill(User.sharedUser(this).hash(), codeToAdd, descriptionToAdd, this);

			codeToAdd = null;

		} else if (action.equals(SuperbillActionDialog.ACTION_REMOVE)) {
			icdDatabase.removeSuperbill(codeToAdd);
			codeToAdd = null;
			reloadSuperbill();
		} else if (action.equals(getString(R.string.move_to_group))) {
			showGroupPicker(getString(R.string.select_a_group));

			if (settings.isGroupingEnabled() && currentLevel == superbillLevel) {
				currentLevel--;
			}

			reloadSuperbill();
		} else if (action.equals(getString(R.string.add_to_superbill_question))) {
			showGroupPicker(getString(R.string.add_to_superbill_group));
		} else if (action.equals(getString(R.string.add_to_main_superbill))) {
			OptimizeHIT.sendEvent(
					GAnalitycsEventNames.MOVE_TO_GROUP.CATEGORY,
					GAnalitycsEventNames.MOVE_TO_GROUP.ACTION,
					GAnalitycsEventNames.MOVE_TO_GROUP.LABEL);

            if( icdDatabase.hasSuperbill(codeToAdd) ){
                NotificationHelper.showAlreadyAddedNotification(getString(R.string.already_exists), getString(R.string.already_in_superbill_short), this);

                isActionDialogOpen = false;
                return;
            }

			icdDatabase.addSuperbill(codeToAdd, "ungrouped", descriptionToAdd, billableToAdd, orderingToAdd, null);

			if (settings.isGroupingEnabled() && currentLevel == superbillLevel && icdDatabase.getUserSuperbill(currentGroup).size() == 0) {
				currentLevel--;
			}

			reloadSuperbill();
		}

		isActionDialogOpen = false;
	}

	/**
	 * ExploreSuperbillHandler Methods
	 */

	@Override
	public void exploreSuperbillSuccess(
			ICD9Related[] icd9Related, ICDAdditionalInfo[] icd10Info,
			boolean billable, String code, String description) {

		processingDialog.dismiss();

//        ArrayList<ICD9Related> icd9RelatedArrayList = new ArrayList<>();
//
//        int index;
//        for( index = 0; index < icd9Related.length; index++ ){
//            icd9RelatedArrayList.add(icd9Related[index]);
//        }

		ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_up_in, R.anim.hold);
		Intent exploreIntent = new Intent(this, ExploreICDActivity.class);
		exploreIntent.putExtra(ExploreICDActivity.CODE, code);
		exploreIntent.putExtra(ExploreICDActivity.DESCRIPTION, description);
		exploreIntent.putExtra(ExploreICDActivity.BILLABLE, billable);
		exploreIntent.putExtra(ExploreICDActivity.ADDITIONAL_INFO, icd10Info);
		exploreIntent.putExtra(ExploreICDActivity.RELATED_RECORDS, icd9Related);
		startActivityForResult(exploreIntent, REQUEST_CODE, options.toBundle() );
		Locker.unlock(this);

	}

	@Override
	public void exploreSuperbillFailure(String error) {

		processingDialog.dismiss();
		NotificationHelper.showNotification(error, this);
		Locker.unlock(this);

	}

	/**
	 * Superbill Manage Helpers
	 */

	public void reloadSuperbill() {

		if (currentLevel == superbillLevel) {

			if (superbillLevel == 0) {

				if (icdDatabase.getUserSuperbill(null).size() == 0) {
					findViewById(R.id.no_result).setVisibility(View.VISIBLE);
					findViewById(R.id.back_navigation_section).setVisibility(View.GONE);
				} else {
					findViewById(R.id.no_result).setVisibility(View.GONE);
					changeNavBar();
				}

			} else if (superbillLevel == 1) {

				if (icdDatabase.getUserSuperbill(currentGroup).size() == 0) {
					currentLevel--;
					reloadSuperbill();
					return;
				}

			}

			subordinateHistory.pop();

			ArrayList<ICDRecordExtended> recordsList = icdDatabase.getUserSuperbill(currentGroup);
			ICDRecordExtended[] records = new ICDRecordExtended[recordsList.size()];
			recordsList.toArray(records);

			subordinateHistory.push(new NavigationModel(null, null, records));

			NavigationModel navigationModel = subordinateHistory.peek();

			subOrdinateAdapters[currentListView].setItems(navigationModel.getSubordinates());
			FractionListView.setListViewHeightBasedOnChildren(subordinateListViews[currentListView]);

		} else if (settings.isGroupingEnabled() && currentLevel == 0) {

			findViewById(R.id.back_navigation_section).setVisibility(View.GONE);

			if (icdDatabase.getUserSuperbill(null).size() == 0) {
				findViewById(R.id.no_result).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.no_result).setVisibility(View.GONE);
			}

			subordinateHistory.pop();

			ArrayList<ICDRecordExtended> recordsList = icdDatabase.getUserGroups();
			ICDRecordExtended[] records = new ICDRecordExtended[recordsList.size()];
			recordsList.toArray(records);

			subordinateHistory.push(new NavigationModel(null, null, records));

			NavigationModel navigationModel = subordinateHistory.peek();

			subOrdinateAdapters[currentListView].setItems(navigationModel.getSubordinates());
			FractionListView.setListViewHeightBasedOnChildren(subordinateListViews[currentListView]);

		}

	}

	/**
	 * Row Click Listener
	 */

	private OnItemClickListener subordinateClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
				return;
			}

			SuperActivity.savedLastClickTime = System.currentTimeMillis();

			IDCRecord record = subOrdinateAdapters[currentListView].getItem(position);

			if (record == null) {
				return;
			}

			onFrontNavigation(!record.getBillable(), record.getCode(), record.getDescription());

		}
	};

	/**
	 * TextWatcher for Query Field
	 */

	TextWatcher queryTextWatcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			if (s.length() == 0) {
				findViewById(R.id.clear_icon).setVisibility(View.INVISIBLE);
			} else {
				findViewById(R.id.clear_icon).setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {

		}

		@Override
		public void afterTextChanged(Editable s) {

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
					GAnalitycsEventNames.MOVE_TO_GROUP.CATEGORY,
					GAnalitycsEventNames.MOVE_TO_GROUP.ACTION,
					GAnalitycsEventNames.MOVE_TO_GROUP.LABEL);

			icdDatabase.addSuperbill(codeToAdd, data.toLowerCase(Locale.US), descriptionToAdd, billableToAdd, orderingToAdd, null);

			if (currentLevel == 0 && settings.isGroupingEnabled()) {
				reloadSuperbill();
			}
		}
	}

	@Override
	public void onPickerDismissed() {
		groupPickerShown = false;
	}

	private void showGroupPicker(String title) {
		orderingToAdd = 0;

		ArrayList<ICDRecordExtended> recordsList = icdDatabase.getUserGroups();
		String[] groups = new String[recordsList.size() + 1];
		int index = 0;

		for (ICDRecordExtended record : recordsList) {
			groups[index] = StringUtils.capitalizeWords(record.getCode());
			index++;
		}

		groups[index] = getString(R.string.create_group);

		groupPicker = new PickerDialog(this, this);
		groupPicker.setArguments(groups, title);
		groupPicker.show();

		groupPickerShown = true;
	}

	/**
	 * InputDialogHandler Methods.
     * Input Dialog appears when we select an item from PickerDialog.
     * The PickerDialog appears when we choose an item from SuperbillActionDialog.
     * SuperbillActionDialog appears when we press the " ... " button on List item. :)
	 */

	@Override
	public void onInputSubmitted(String input) {

		icdDatabase.addSuperbill(codeToAdd, input.toLowerCase(Locale.US), descriptionToAdd, billableToAdd, orderingToAdd, null);

		if (settings.isGroupingEnabled() && currentLevel == superbillLevel) {
			currentLevel--;
		}

		reloadSuperbill();

	}

	@Override
	public void onInputCanceled() {

	}

	/**
	 * Menu State Methods
	 */

	@Override
	public void onBackPressed() {
		if (currentLevel == 0) {
			super.onBackPressed();
			return;
		}

		backNavigationClickListener.onClick(null);
	}

	/**
	 * BroadcastReceivers
	 */

	private BroadcastReceiver superbillReloadedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			FindCodeActivity.this.reloadSuperbill();
		}
	};

	private BroadcastReceiver newSuperbillBroadcastReceiver = new BroadcastReceiver() {

	    @Override
	    public void onReceive(Context context, Intent intent) {
	    	FindCodeActivity.this.reloadSuperbill();
	    }
	};

	/**
	 * OnEditorActionListener Methods
	 */

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_DONE) {
			queryCodes();
			return true;
		}

		return false;
	}

//    private void showProgressDialog() {
//        processingDialog.show();
//        TextView loadingDialogText = (TextView) processingDialog.findViewById(R.id.loading_dialog_text);
//        loadingDialogText.setText(R.string.loading);
//    }

}
