package com.implementhit.OptimizeHIT.activity;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.AppInfo;
import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.ViewPagerAdapter;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.OnGetDashboardBadgeCountListener;
import com.implementhit.OptimizeHIT.api.SolutionRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;
import com.implementhit.OptimizeHIT.api.FragmentPositionProvider;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.fragments.DashboardFragment;
import com.implementhit.OptimizeHIT.fragments.LibraryFragment;
import com.implementhit.OptimizeHIT.fragments.NotificationsFragment;
import com.implementhit.OptimizeHIT.fragments.NotificationsFragment.NotificationActionListener;
import com.implementhit.OptimizeHIT.fragments.OnBackPressedListener;
import com.implementhit.OptimizeHIT.fragments.OptiQueryFragment;
import com.implementhit.OptimizeHIT.fragments.OptiQueryFragment.VoiceSearchFragmentHandler;
import com.implementhit.OptimizeHIT.gcm.GcmBroadcastReceiver;
import com.implementhit.OptimizeHIT.models.Notification;
import com.implementhit.OptimizeHIT.models.Settings;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.CheckConnectionHelper;
import com.implementhit.OptimizeHIT.util.ExternalSignalParser;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;
import com.implementhit.OptimizeHIT.util.PermissionHelper;
import com.nuance.speechkit.Session;

import org.json.JSONArray;

import java.util.ArrayList;

public class MenuActivity extends SuperActivity
        implements SolutionRequestListener, VoiceSearchFragmentHandler, DashboardFragment.DashboardFragmentListener
        , NotificationActionListener, ViewPager.OnPageChangeListener, LibraryFragment.LibraryFragmentListener
        , ExternalSignalParser.ExternalSignalFeedbackReceiver, OnGetDashboardBadgeCountListener,
        FragmentPositionProvider, LibraryFragment.OnSearchSolutionsListener {

    private static int counter = 0;

    private static final int[] MENU_ITEM_IDS = {
            R.id.dashboard_item,
            R.id.library_item,
            R.id.opti_query_item,
            R.id.notification_item
    };

    private String[] screenTitles;
    private String[] menuIcons;

    private final String SAVE_BANNER_STATE = "saveBannerState";
    private final String SAVE_NOTIFICATION_ID = "saveNotificationId";
    private final String SAVED_NOTIFICATION_TO_SHOW_AFTER_LOGIN_ID = "savedNotificationToShowAfterLoginId";
    private final String SAVED_EXTERNAL_URL_TO_SHOW_AFTER_LOGIN_ID = "savedExternalUrlToShowAfterLoginId";
    private final String SAVE_DEEP_LINK_ALREADY_SHOWN = "deepLinkAlreadyShown";

    public static final int REQUEST_CODE = 9913;

    private User user;
    private String userHash;
    Settings settings;
    private DBTalker dbTalker;

    private FontsHelper fontsHelper;
    private LoadingDialog loadingDialog;

    private boolean solutionRequestInProgress = false;

    private boolean bannerIsShown = false;
    private boolean loadFromNotification = false;
    private boolean loadFromDeepLink = false;
    private boolean deepLinkAlreadyShown = false;

    boolean openExternalResource = false;

    private RelativeLayout bannerContainer;

    private int currentFragmentIndex = 0;
    private int notificationId;

    // This is the id of notification which is received, when user is logged in, BUT is clicked, when user is logged out.
    // In this case, first the app comes here, to MainActivity. Then we redirect the app to LoginActivity. But the
    // user immediately sees the Login screen. Then, when user is logged in, we must show notifications content.
    private int notificationToShowAfterLoginId = 0;

    // This is the url of link, clicked from the app corresponding web site, from the same android device browser,
    // on which the app is installed. Additional meaning is the same as previous.
    private String externalUrlToShowAfterLogin = null;

    private static Session nuanceSession;

    private ViewPager fragmentsViewPager;
    private ViewPagerAdapter fragmentsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        getWindow().setFeatureInt(Window.FEATURE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);

        if ((PermissionHelper.checkIfHasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                || PermissionHelper.checkIfHasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION))
                && savedInstanceState == null) {
            // Check if permission to update user location is granted
            // Check if this is the first app launch (savedInstanceState is null)
            // TODO Implement more elegant solution
            OptimizeHIT.sharedApplication().updateLocation();
        }

        fontsHelper = FontsHelper.sharedHelper(this);
        user = User.sharedUser(this);
        userHash = user.hash();

        dbTalker = DBTalker.sharedDB(this);

        screenTitles = getResources().getStringArray(R.array.menu_titles);
        menuIcons = getResources().getStringArray(R.array.menu_icons);

        setContentView(R.layout.activity_menu);

        // A banner view which appear when user receive a push notification
        bannerContainer = (RelativeLayout) findViewById(R.id.banner_container);

        initializeUserSettings();

        if (savedInstanceState != null) {
            deepLinkAlreadyShown = savedInstanceState.getBoolean(SAVE_DEEP_LINK_ALREADY_SHOWN, false);
            notificationToShowAfterLoginId = savedInstanceState.getInt(SAVED_NOTIFICATION_TO_SHOW_AFTER_LOGIN_ID);
            externalUrlToShowAfterLogin = savedInstanceState.getString(SAVED_EXTERNAL_URL_TO_SHOW_AFTER_LOGIN_ID);
            //isOfflineDialogShown = savedInstanceState.getBoolean(SAVE_OFFLINE_NOTIFICATION ,false);
        }

        checkForIntentContent(getIntent(), false);

        if (savedInstanceState != null) {
            user.setLastAccessedPage(currentFragmentIndex);

            boolean state = savedInstanceState.getBoolean(SAVE_BANNER_STATE);

            if (state) {
                notificationId = savedInstanceState.getInt(SAVE_NOTIFICATION_ID);
                Notification notification = dbTalker.getNotification(notificationId);
                showBanner(notification);
            }

        }

        setupUI();
        setSpeechRecognizerKit();

        if (getIntent().hasExtra(DBTalker.EXTRA_NOTIFICATION)) {
            Notification immediateNotification = (Notification) getIntent().getSerializableExtra(DBTalker.EXTRA_NOTIFICATION);
            getIntent().removeExtra(DBTalker.EXTRA_NOTIFICATION);

            if (immediateNotification.getCorrespondSolutionId() != null
                    && !immediateNotification.getCorrespondSolutionId().isEmpty()) {
                openNotification(immediateNotification);
            }
        }

        fragmentsPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Log.d( "WE ARE IN ON CREATE", "ON CREATE" );
        fragmentsViewPager = (ViewPager) findViewById(R.id.view_pager);
        fragmentsViewPager.addOnPageChangeListener(this);
        fragmentsViewPager.setAdapter(fragmentsPagerAdapter);
        fragmentsViewPager.setCurrentItem(currentFragmentIndex, false);

        onPageSelected(fragmentsViewPager.getCurrentItem());
    }

    @Override
    protected void refreshStateAfterLogin(boolean isInitialLogin) {
        if (isInitialLogin) {
            int currentFragmentIndex = settings.defaultScreen();
            user.setLastAccessedPage(currentFragmentIndex);
            setNotificationsBadge();
            setReactivationsBadge();

            fragmentsViewPager.setCurrentItem(currentFragmentIndex);

            animateBannerGone();
        }

        TextView domainLabel = (TextView) findViewById(R.id.domain_label);
        TextView email = (TextView) findViewById(R.id.email);

        if (domainLabel != null) {
            domainLabel.setText(user.domainLabel());
        }

        if (email != null) {
            email.setText(user.username());
        }
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
    public void onBackPressed() {
        int currentFragmentIndex = fragmentsViewPager.getCurrentItem();
        Fragment fragment = (Fragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, currentFragmentIndex);

        if (fragment instanceof OnBackPressedListener) {
            OnBackPressedListener backPressedListener = (OnBackPressedListener) fragment;

            if (backPressedListener.onBackPressed()) {
                return;
            }

        }

        user.setLastAccessedPage(-1);

        super.onBackPressed();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(SAVE_DEEP_LINK_ALREADY_SHOWN, deepLinkAlreadyShown);
        outState.putBoolean(SAVE_BANNER_STATE, bannerIsShown);
        outState.putInt(SAVE_NOTIFICATION_ID, notificationId);
        outState.putInt(SAVED_NOTIFICATION_TO_SHOW_AFTER_LOGIN_ID, notificationToShowAfterLoginId);
        outState.putString(SAVED_EXTERNAL_URL_TO_SHOW_AFTER_LOGIN_ID, externalUrlToShowAfterLogin);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (SuperActivity.IS_LIBRARY_OFFLINE_DIALOG_SHOWN) {
            NotificationHelper.showNotification( getString( R.string.title_no_internet_refresh ),
                    getString( R.string.message_no_internet_refresh ), true, MenuActivity.this );
        }

        findViewById(R.id.right_button).clearAnimation();

        IntentFilter showNotificationFilter = new IntentFilter(APITalker.ACTION_SHOW_NOTIFICATION);
        registerReceiver(notificationShowReceiver, showNotificationFilter);

        IntentFilter updateNotificationsFilter = new IntentFilter(APITalker.ACTION_UPDATE_NOTIFICATIONS);
        registerReceiver(notificationsUpdatedReceiver, updateNotificationsFilter);

        IntentFilter suggestedFilter = new IntentFilter(APITalker.ACTION_SUGGESTED_LEARNING_UPDATED);
        registerReceiver(suggestionsUpdatedReceiver, suggestedFilter);

        IntentFilter optiQueryQuestionsFilter = new IntentFilter(APITalker.ACTION_SHOW_QUESTIONS);
        registerReceiver(optiQueryQuestionsShowReceiver, optiQueryQuestionsFilter);

        IntentFilter updateDataFilter = new IntentFilter(APITalker.ACTION_NEW_SOLUTION_DATA);
        registerReceiver(newSolutionDataReceiver, updateDataFilter);

        IntentFilter updateDataFailFilter = new IntentFilter(APITalker.ACTION_UPDATE_DATA_TERMINATED);
        registerReceiver(updateDataFailReceiver, updateDataFailFilter);


        // We check if there is a notification, that received, when user was logged in, but clicked, when user was logged out.
        // Or if there is corresponding external  link. If there is, we show the corresponding data.
        if (user.isLoggedIn()) {

            // if notificationToShowAfterLoginId != 0, it means that we have a corresponding notification which is clicked, when user is logged out,
            // and we must show its content when user is logged in.
            if (notificationToShowAfterLoginId != -1) {
                openNotification(notificationToShowAfterLoginId);

                // We make it 0, because if we not do so, the notification corresponding data will be shown twice( i. e. app will think ,
                // that the data not shown yet, and will show it again )
                notificationToShowAfterLoginId = -1;

            }

            // if notificationToShowAfterLoginId != 0, it means that we have a corresponding notification which is clicked, when user is logged out,
            // and we must show its content when user is logged in.
            if (externalUrlToShowAfterLogin != null) {
                openDeepLinkUrl(externalUrlToShowAfterLogin);

                // Same as the previous
                externalUrlToShowAfterLogin = null;
            }
        }

        currentFragmentIndex = fragmentsViewPager.getCurrentItem();
        if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS) {

            NotificationsFragment notificationsFragment = (NotificationsFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, currentFragmentIndex);
            if(notificationsFragment != null) {
                notificationsFragment.reloadNotifications();
            }

        }

        setNotificationsBadge();
        setReactivationsBadge();
        onDashboardBadgeCountSuccess(user.dashboardBadgeCount());

        ArrayList<Integer> bannerNotificationIds = dbTalker.getBannerNotifications();

        for (Integer bannerNotificationId : bannerNotificationIds) {
            Notification notification = dbTalker.getNotification(bannerNotificationId);

            if (notification != null && !notification.isRead()) {
                notificationId = bannerNotificationId;
                showBanner(notification);

                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(notificationId);

                return;
            }
        }

        initializeUserSettings();

        fragmentsViewPager = (ViewPager) findViewById(R.id.view_pager);
        fragmentsViewPager.setCurrentItem(currentFragmentIndex, false);
        Log.d( "WE ARE IN ON RESUME", "ON RESUME" );
        onPageSelected(fragmentsViewPager.getCurrentItem());

        APITalker
                .sharedTalker()
                .getDashboardBadgeCount(
                        userHash,
                        this
                );

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        deepLinkAlreadyShown = false;
        checkForIntentContent(intent, true);
    }

    @Override
    public void onPause() {
        unregisterReceiver(notificationShowReceiver);
        unregisterReceiver(notificationsUpdatedReceiver);
        unregisterReceiver(suggestionsUpdatedReceiver);
        unregisterReceiver(optiQueryQuestionsShowReceiver);
        unregisterReceiver(newSolutionDataReceiver);
        unregisterReceiver(updateDataFailReceiver);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onConnectionUpdate(boolean isConnected) {
        super.onConnectionUpdate(isConnected);

        int fragmentIndex = fragmentsViewPager.getCurrentItem();

        if (fragmentIndex == ViewPagerAdapter.POSITION_OPTI_QUERY) {
                OptiQueryFragment optiQueryFragment = (OptiQueryFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, fragmentIndex);

            if (optiQueryFragment != null) {
                optiQueryFragment.setMicrophoneActive(isConnected && PermissionHelper.isMicrophoneAvailable(this));
            }
        } else if (fragmentIndex == ViewPagerAdapter.POSITION_DASHBOARD) {
            DashboardFragment dashboardFragment = (DashboardFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, fragmentIndex);

            if (dashboardFragment != null) {
                dashboardFragment.onConnectionUpdate(isConnected);
            }
        }
    }

    /**
     * Intent Helpers
     */

    private void checkForIntentContent(Intent newIntent, boolean needsManualFragmentChange) {
        // We write this piece of code because it may be the case when MenuActivity is opened directly from push notification, but user is not logged in.
        // In that case we must redirect the user to login screen. But we must not forget adout showing notification content after user is logged in.
        if (!user.isLoggedIn()) {
            if (newIntent.hasExtra(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID)) {
                notificationToShowAfterLoginId = newIntent.getExtras().getInt(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID);
            }

            if (newIntent.getDataString() != null
                    && !newIntent.getDataString().isEmpty()
                    && !deepLinkAlreadyShown) {

                externalUrlToShowAfterLogin = newIntent.getDataString();
            }

            NotificationHelper.showLoginPage(this, false, false);
            return;
        }

        if (newIntent.hasExtra(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID)) {
            int notificationId = newIntent.getExtras().getInt(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID);
            openNotification(notificationId);
            newIntent.removeExtra(GcmBroadcastReceiver.EXTRA_NOTIFICATION_ID);
        }

        if (newIntent.getDataString() != null
                && !newIntent.getDataString().isEmpty()
                && !deepLinkAlreadyShown) {
            String url = newIntent.getDataString();
            openDeepLinkUrl(url);
        }
    }

    public void proceedCategoryAndSubcategory(Notification notification) {

        int subCategoryId = notification.getCorrespondSubCategoryId();
        int categoryId = notification.getCorrespondCategoryId();

        Solution category = dbTalker.getCategory(categoryId);
        Solution subCategory = dbTalker.getSubCategory(subCategoryId);


        if (category != null) {

            String solutionTypeExtra;

            if (subCategory == null) {
                solutionTypeExtra = SimpleSolutionsListActivity.TYPE_SUBCATEGORIES;
            } else {
                solutionTypeExtra = SimpleSolutionsListActivity.TYPE_SOLUTIONS;
            }

            openExternalResource = true;

            Intent intent = new Intent(this, SimpleSolutionsListActivity.class);
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_CATEGORY, category);
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_SUBCATEGORY, subCategory);
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_SOLUTIONS_TYPE, solutionTypeExtra);

            startActivity(intent);

        }
    }

    private void openDeepLinkUrl(String url) {
        deepLinkAlreadyShown = true;
        ExternalSignalParser.processDeepLink(this, url, this);
    }

    private void setupUI() {
        int index;
        View menuItem;
        TextView menuItemIcon;
        TextView menuItemTitle;

        Typeface fontelloTypeface = fontsHelper.fontello();

        Button rightButton = (Button) findViewById(R.id.right_button);
        rightButton.setTypeface(fontsHelper.fontello());

        LinearLayout leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
        leftButtonLayout.setVisibility(View.VISIBLE);

        Button leftButton = (Button) findViewById(R.id.left_button);
        leftButton.setTypeface(fontsHelper.fontello());
        leftButton.setText(R.string.icon_profile);

        leftButton.setOnClickListener(onBarButtonClickListener);
        rightButton.setOnClickListener(onBarButtonClickListener);

        for (index = 0; index < MENU_ITEM_IDS.length; index++) {

            menuItem = findViewById(MENU_ITEM_IDS[index]);
            menuItem.setOnClickListener(onMenuClickListener);

            menuItemIcon = (TextView) menuItem.findViewById(R.id.item_icon);
            menuItemTitle = (TextView) menuItem.findViewById(R.id.item_title);

            menuItemIcon.setText(menuIcons[index]);
            menuItemIcon.setTypeface(fontelloTypeface);

            menuItemTitle.setText(screenTitles[index]);

        }
    }

    private void setupRightButton() {
        Button rightButton = (Button) findViewById(R.id.right_button);

        //this is for set our badge button visible just in case
        rightButton.setVisibility(View.VISIBLE);
        //and setting it clickable for prophylaxis
        rightButton.setAlpha(1f);
        rightButton.setClickable(true);

        int pagerCurrentItem = fragmentsViewPager.getCurrentItem();

        if (pagerCurrentItem == ViewPagerAdapter.POSITION_NOTIFICATIONS) {
            rightButton.setText(R.string.icon_check_list);
            rightButton.setVisibility(View.VISIBLE);
            findViewById(R.id.right_button).clearAnimation();
            if (user.unreadNotifications() > 0) {
                rightButton.setAlpha(1f);
                rightButton.setClickable(true);
            } else {
                rightButton.setAlpha(0.5f);
                rightButton.setClickable(false);
            }
        } else if (pagerCurrentItem == ViewPagerAdapter.POSITION_LIBRARY) {
            rightButton.setText(R.string.icon_refresh);
            rightButton.setVisibility(View.VISIBLE);
            rightButton.setAlpha(1f);
            rightButton.setClickable(true);
        } else if (pagerCurrentItem == ViewPagerAdapter.POSITION_DASHBOARD ||
                    pagerCurrentItem == ViewPagerAdapter.POSITION_OPTI_QUERY) {

            rightButton.clearAnimation();
            rightButton.setVisibility(View.GONE);
            rightButton.setAlpha(1f);
            rightButton.setClickable(true);

        } else {
            rightButton.setVisibility(View.GONE);
        }
    }

    private OnClickListener onBarButtonClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {

            long systemTime = System.currentTimeMillis();

            if (systemTime - savedLastClickTime < 200
                    || systemTime - savedLastWebViewInteractionTime < 500) {
                return;
            }

            savedLastClickTime = System.currentTimeMillis();

            if (view.getId() == R.id.left_button) {

                Intent intent = new Intent(MenuActivity.this, UserProfileActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_up_in, R.anim.hold);

            } else if ( view.getId() == R.id.right_button ) {

                int currentFragmentIndex = fragmentsViewPager.getCurrentItem();

                if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS) {
                    NotificationsFragment fragment = (NotificationsFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, ViewPagerAdapter.POSITION_NOTIFICATIONS);
                    fragment.onMarkAllNotificationsRead();
                } else if (currentFragmentIndex == ViewPagerAdapter.POSITION_LIBRARY) {

                    Animation animationRotateCenter = AnimationUtils.loadAnimation(
                            MenuActivity.this, R.anim.rotated_load);
                    animationRotateCenter.setFillAfter(true);
                    animationRotateCenter.setInterpolator(new LinearInterpolator());
                    animationRotateCenter.setRepeatCount(Animation.INFINITE);

                    Button refreshButton = (Button)findViewById(R.id.right_button);
                    refreshButton.startAnimation(animationRotateCenter);

                    if( ! CheckConnectionHelper.isNetworkAvailable(MenuActivity.this) ){
                        refreshButton.clearAnimation();
                        SuperActivity.IS_LIBRARY_OFFLINE_DIALOG_SHOWN = true;
                        NotificationHelper.showNotification( getString( R.string.title_no_internet_refresh ),
                                getString( R.string.message_no_internet_refresh ), true, MenuActivity.this );
                        return;
                    }

                    OptimizeHIT.sharedApplication().updateData();

                    APITalker.sharedTalker().getUserHistory(
                            User.sharedUser(MenuActivity.this).hash(),
                            DBTalker.sharedDB(MenuActivity.this)
                    );

                    APITalker.sharedTalker().getNotifications(
                            MenuActivity.this,
                            User.sharedUser(MenuActivity.this).hash(),
                            null
                    );
                }
            }
        }

    };

    private OnClickListener onMenuClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            int fragmentPosition = 0;
            int clickedViewId = view.getId();

            int index;
            for (index = 0; index < MENU_ITEM_IDS.length; index++) {
                if (clickedViewId == MENU_ITEM_IDS[index]) {
                    fragmentPosition = index;
                }
            }
            fragmentsViewPager.setCurrentItem(fragmentPosition);
        }
    };

    /*
     * SolutionHandler Methods
     */

    @Override
    public void solutionSuccess(int solutionId, String title, String html, String speech) {

        dbTalker.saveHistory(solutionId, title, false);

        int currentSolution = 0;
        ArrayList<Solution> solutions = new ArrayList<>();

        int currentFragmentIndex = fragmentsViewPager.getCurrentItem();

//		if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS
//				|| loadFromNotification
//				|| loadFromDeepLink) {
        solutions.add(new Solution(solutionId, title));
//		}

        String accessMethod = "";

        if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS || loadFromNotification) {
            accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_PUSH_NOTIFICATION;
        } else if (currentFragmentIndex == ViewPagerAdapter.POSITION_LIBRARY) {
            accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_BROWSE;
        } else if (loadFromDeepLink) {
            accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_DEEPLINKSOLUTION;
        }

        loadFromNotification = false;
        loadFromDeepLink = false;

        loadingDialog.dismiss();

        Locker.unlock(this);

        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_left_in, R.anim.hold);
        Intent solutionIntent = new Intent(this, SolutionActivity.class);
        solutionIntent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, accessMethod);
        solutionIntent.putParcelableArrayListExtra(SolutionActivity.EXTRA_SOLUTIONS, solutions);
        solutionIntent.putExtra(SolutionActivity.EXTRA_POSITION, currentSolution);
        solutionIntent.putExtra(SolutionActivity.EXTRA_HTML, html);
        solutionIntent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);

        startActivityForResult(solutionIntent, SolutionActivity.REQUEST_CODE, options.toBundle());

        solutionRequestInProgress = false;

    }

    @Override
    public void solutionFailure(String error) {
        loadingDialog.dismiss();

        NotificationHelper.showNotification(error, this);

        solutionRequestInProgress = false;

        Locker.unlock(this);
    }

    /**
     * LibraryFragmentListener Methods
     */

    @Override
    public void openSubCategory(Solution solution) {
        Intent intent = new Intent(this, SimpleSolutionsListActivity.class);
        intent.putExtra(SimpleSolutionsListActivity.EXTRA_SOLUTIONS_TYPE, SimpleSolutionsListActivity.TYPE_SUBCATEGORIES);
        intent.putExtra(SimpleSolutionsListActivity.EXTRA_CATEGORY, solution);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.hold);
    }

    @Override
    public void openSolution(Solution solution, String solutionType) {

        if (solutionRequestInProgress) {
            return;
        }

        solutionRequestInProgress = true;
        loadingDialog = new LoadingDialog(this);
        loadingDialog.show();

        Locker.lock(this);
        APITalker.sharedTalker().getSolution(userHash, solution.solutionId(), solutionType, this);

    }

    @Override
    public void openAlphabeticalSolutions(Solution category) {
        Intent intent = new Intent(this, AlphabeticalSolutionsListActivity.class);
        intent.putExtra(AlphabeticalSolutionsListActivity.EXTRA_CATEGORY, category);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.hold);
    }

    @Override
    public void openLibraryItem(LibraryFragment.LibraryItem libraryItem) {
        Intent intent = null;

        if (libraryItem == LibraryFragment.LibraryItem.SUGGESTED_LEARNING) {
            intent = new Intent(this, SuggestedSolutionsActivity.class);
        } else if (libraryItem == LibraryFragment.LibraryItem.FIND_THE_CODE) {
            intent = new Intent(this, FindCodeActivity.class);
        } else if (libraryItem == LibraryFragment.LibraryItem.HISTORY) {
            intent = new Intent(this, SimpleSolutionsListActivity.class);
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_SOLUTIONS_TYPE, SimpleSolutionsListActivity.TYPE_HISTORY);
        }  else if (libraryItem == LibraryFragment.LibraryItem.FAVORITES) {
            intent = new Intent(this, FavoriteSolutionsActivity.class);
//            intent.putExtra(EditableSolutionsListActivity.EXTRA_SOLUTIONS_TYPE, EditableSolutionsListActivity.TYPE_FAVORITES);
        }

        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.hold);
    }

    /*
     * VoiceSearchFragmentHandler Methods
     */

    @Override
    public void browse() {
//		changeFragment(1, false, false, false, null, null);
    }

    /*
     * Broadcast Receivers
     */

    private BroadcastReceiver notificationShowReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle savedState = new Bundle();
            savedState.putBoolean(SAVE_BANNER_STATE, bannerIsShown);

            notificationId = intent.getIntExtra(DBTalker.EXTRA_NOTIFICATION_ID, -1);
            Notification receivedNotification = dbTalker.getNotification(notificationId);

            int currentFragmentIndex = fragmentsViewPager.getCurrentItem();

            if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS) {
                counter++;
                Log.d("COUNTER: ", counter + "");
                NotificationsFragment notificationsFragment = /*(NotificationsFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, currentFragmentIndex);*/(NotificationsFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, currentFragmentIndex);
                if( notificationsFragment != null ) {
                    notificationsFragment.addNotification(receivedNotification);
                }
            }

            showBanner(receivedNotification);
            setNotificationsBadge();
        }



    };

    private BroadcastReceiver notificationsUpdatedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int currentFragmentIndex = fragmentsViewPager.getCurrentItem();

            if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS) {
                NotificationsFragment notificationsFragment = (NotificationsFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, currentFragmentIndex);
                if( notificationsFragment != null ) {
                    notificationsFragment.reloadNotifications();
                }
            }

            setNotificationsBadge();
        }
    };

    private BroadcastReceiver suggestionsUpdatedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            setReactivationsBadge();
        }
    };

    private BroadcastReceiver optiQueryQuestionsShowReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int currentFragmentIndex = fragmentsViewPager.getCurrentItem();
            if (currentFragmentIndex == ViewPagerAdapter.POSITION_OPTI_QUERY) {
                OptiQueryFragment optiQueryFragment = (OptiQueryFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, currentFragmentIndex);

                if (optiQueryFragment != null) {
                    optiQueryFragment.loadQuestionsFromDatabase();
                }
            }
        }
    };

    private BroadcastReceiver newSolutionDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Button refreshButton = (Button)findViewById(R.id.right_button);
            if( refreshButton != null ) {
               Animation animation =  refreshButton.getAnimation();
                if( animation != null){
                    animation.cancel();
                }
            }
        }
    };

    private BroadcastReceiver updateDataFailReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Button refreshButton = (Button)findViewById(R.id.right_button);
            if( refreshButton != null ) {
                Animation animation =  refreshButton.getAnimation();
                if( animation != null){
                    animation.cancel();
                }
            }
        }
    };

    /*
     * NotificationActionListener Methods
     */

    @Override
    public void setNotificationsBadge() {
        int currentFragmentIndex = fragmentsViewPager.getCurrentItem();

        if (currentFragmentIndex == ViewPagerAdapter.POSITION_NOTIFICATIONS) {
            if (user.unreadNotifications() == 0) {
                //findViewById(R.id.right_button).setVisibility(View.GONE);
                findViewById(R.id.right_button).setAlpha(0.5f);
                findViewById(R.id.right_button).setClickable(false);

            } else {
                findViewById(R.id.right_button).setVisibility(View.VISIBLE);
                findViewById(R.id.right_button).setAlpha(1f);
                findViewById(R.id.right_button).setClickable(true);
            }
        }

        LinearLayout barPagerMenu = (LinearLayout) findViewById(R.id.bottom_bar);
        View itemNotifications = barPagerMenu.findViewById(R.id.notification_item);

        TextView unreadNotificationsBadge = (TextView) itemNotifications.findViewById(R.id.badge);
        int unreadNotifications = user.unreadNotifications();

        if (unreadNotifications > 0) {
            unreadNotificationsBadge.setText(unreadNotifications + "");
            unreadNotificationsBadge.setVisibility(View.VISIBLE);
        } else {
            unreadNotificationsBadge.setVisibility(View.GONE);
        }
    }

    @Override
    public void hideBannerForNotification(int notificationId, boolean allRead) {
        if (allRead || this.notificationId == notificationId) {
            animateBannerGone();
        }
    }

    private void setReactivationsBadge() {
        LinearLayout barPagerMenu = (LinearLayout) findViewById(R.id.bottom_bar);

        View itemLibrary = barPagerMenu.findViewById(R.id.library_item);

        TextView unreadReactivationsBadge = (TextView) itemLibrary.findViewById(R.id.badge);
        int unreadReactivations = user.reactivations();

        if (unreadReactivations > 0) {
            unreadReactivationsBadge.setText(unreadReactivations + "");
            unreadReactivationsBadge.setVisibility(View.VISIBLE);
        } else {
            unreadReactivationsBadge.setVisibility(View.GONE);
        }
    }

    /*
     * Banner Related Methods
     */

    public void showBanner(Notification notification) {

//        if (bannerIsShown) {
//            return;
//        }

        bannerIsShown = true;

        bannerContainer.setVisibility(View.INVISIBLE);
        bannerContainer.setOnClickListener(onBannerClickListener);

        Animation slideDownInAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_down_in);
        slideDownInAnimation.setFillAfter(true);
        slideDownInAnimation.setAnimationListener(onBannerSlideInAnimationListener);

        bannerContainer.startAnimation(slideDownInAnimation);

        dbTalker.removeBannerNotification(notificationId);

        TextView notificationIcon = (TextView) findViewById(R.id.notification_icon);
        TextView notificationText = (TextView) findViewById(R.id.banner_text);
        TextView bannerCancelIcon = (TextView) findViewById(R.id.banner_cancel_icon);

        bannerCancelIcon.setOnClickListener(onBannerCancelClickListener);

        String notificationTextString = notification.getNotificationText();

        notificationIcon.setTypeface(fontsHelper.fontello());
        notificationText.setText(notificationTextString);
        bannerCancelIcon.setTypeface(fontsHelper.fontello());
    }

    OnClickListener onBannerCancelClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (System.currentTimeMillis() - savedLastClickTime < 200) {
                return;
            }

            savedLastClickTime = System.currentTimeMillis();

            Notification notification = dbTalker.getNotification(notificationId);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notification.getNotificationId());

            animateBannerGone();
            hideBannerForNotification( notificationId, false );

//            if (!CheckConnectionHelper.isNetworkAvailable(MenuActivity.this)) {
//                NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, MenuActivity.this);
//            } else {
//                openNotification(notification);
//            }
        }

    };

    OnClickListener onBannerClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            if (System.currentTimeMillis() - savedLastClickTime < 200) {
                return;
            }

            savedLastClickTime = System.currentTimeMillis();

            Notification notification = dbTalker.getNotification(notificationId);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(notification.getNotificationId());

            animateBannerGone();

            if (!CheckConnectionHelper.isNetworkAvailable(MenuActivity.this)) {
                NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, MenuActivity.this);

                return;
            }

            openNotification(notification);
            hideBannerForNotification( notificationId, false);
        }
    };

    Animation.AnimationListener onBannerSlideInAnimationListener = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            if (bannerIsShown) {
                bannerContainer.setVisibility(View.VISIBLE);
                bannerContainer.bringToFront();
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (!bannerIsShown) {
                bannerContainer.setVisibility(View.GONE);
                findViewById(R.id.menu_action_bar).bringToFront();
            }
        }

    };

    /*
     * Some Notification related methods
     */

    private void openNotification(int notificationId) {
        Notification notification = dbTalker.getNotification(notificationId);

        if (notification == null) {
            return;
        }

        openNotification(notification);
    }

    private void openNotification(Notification notification) {
        loadFromNotification = true;

        String correspondSolutionId = notification.getCorrespondSolutionId();
        notification.checkSolutionId();

        if( ( correspondSolutionId != null ) && ( !correspondSolutionId.equals("")  )  ) {
            Solution correspondSolution = new Solution(Integer.parseInt(correspondSolutionId), "");
            openSolution(correspondSolution, APITalker.CALL_TYPES.CALL_TYPE_PUSH_NOTIFICATION);
        }

        makeNotificationRead(notification);
        ExternalSignalParser.processNotification(this, notification, this);
    }

    private void makeNotificationRead(Notification notification) {
        if (!notification.isRead()) {
            notification.setReadStatus(true);
            int unreadNotificationsCount = user.unreadNotifications();
            unreadNotificationsCount--;
            user.setUnreadNotifications(unreadNotificationsCount);

            int notificationId = notification.getNotificationId();
            String notificationIdString = String.valueOf(notificationId);

            ArrayList<String> notificationIds = new ArrayList<>();
            notificationIds.add(notificationIdString);

            dbTalker.markNotificationsReadInDb(notificationIds);

            JSONArray jsonArray = new JSONArray();
            jsonArray.put(notificationIdString);
            String jsonFormattedString = jsonArray.toString();

            APITalker.sharedTalker().markNotificationRead(userHash, jsonFormattedString, null);

            sendBroadcast(new Intent(APITalker.ACTION_UPDATE_NOTIFICATIONS));
        }
    }

    private void animateBannerGone() {

        if (!bannerIsShown) {
            return;
        }

//        ArrayList<Integer> badgeNotificationIds = dbTalker.getBannerNotifications();
//
//        for (Integer badgeNotificationId : badgeNotificationIds) {
//            Notification notification = dbTalker.getNotification(badgeNotificationId);
//
//            if (notification != null && !notification.isRead()) {
//                notificationId = badgeNotificationId;
//                showBanner(notification);
//
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.cancel(notificationId);
//
//                return;
//            }
//
//        }

        bannerIsShown = false;
        Animation slideUpOutAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up_out);
        slideUpOutAnimation.setFillAfter(true);
        slideUpOutAnimation.setAnimationListener(onBannerSlideInAnimationListener);
        bannerContainer.startAnimation(slideUpOutAnimation);

        bannerContainer.setOnClickListener(null);
    }

    /*
     * Speech Kit Methods
     */

    public static Session getNuanceSession() {
        return nuanceSession;
    }

    public void setSpeechRecognizerKit() {
        if (nuanceSession == null) {
            nuanceSession = Session.Factory.session(this, AppInfo.SERVER_URI, AppInfo.APP_KEY);
        }
    }

    /*
     * OnPageChangeListener
     */

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        int index;
        View menuItem;
        TextView menuItemIcon;
        TextView menuItemTitle;

        currentFragmentIndex = position;

        user.setLastAccessedPage(currentFragmentIndex);

        int orangeColor = User.sharedUser(this).primaryColor();
        TextView titleTextView = (TextView) findViewById(R.id.content_title);
        String[] menuIconsBold = getResources().getStringArray(R.array.menu_icons_bold);

        for (index = 0; index < MENU_ITEM_IDS.length; index++) {

            menuItem = findViewById(MENU_ITEM_IDS[index]);
            menuItemIcon = (TextView) menuItem.findViewById(R.id.item_icon);
            menuItemTitle = (TextView) menuItem.findViewById(R.id.item_title);

            if (position == index) {

                titleTextView.setText(screenTitles[index]);
                menuItemIcon.setTextColor(orangeColor);
                menuItemIcon.setText(menuIconsBold[index]);
                menuItemTitle.setTextColor(orangeColor);

                OptimizeHIT.sendEvent(
                        GAnalitycsEventNames.MENU_SELECT.CATEGORY,
                        GAnalitycsEventNames.MENU_SELECT.ACTION,
                        screenTitles[index]);

            } else {

                menuItemIcon.setTextColor(getResources().getColor(R.color.text_light_grey));
                menuItemIcon.setText(menuIcons[index]);
                menuItemTitle.setTextColor(getResources().getColor(R.color.text_light_grey));

            }

//            if( position == ViewPagerAdapter.POSITION_LIBRARY ){
//                LibraryFragment libraryFragment = (LibraryFragment) fragmentsPagerAdapter.instantiateItem(fragmentsViewPager, position);
//                if( libraryFragment != libraryFragment ) {
//                    libraryFragment.setOnSearchSolutionsListener(this);
//                }
//            }

        }


        Log.d( "WE ARE IN ONPS", fragmentsViewPager + "" );
        fragmentsViewPager = (ViewPager) findViewById(R.id.view_pager);
        fragmentsViewPager.setCurrentItem(position);
//___We do this for handle page not loading when comes with no internet,turns on and navigate else and come back from page
        if (fragmentsViewPager.getCurrentItem() == ViewPagerAdapter.POSITION_DASHBOARD) {
            SuperActivity.FORCE_RELOAD_DASHBOARD = true;
        } //__end_
        setupRightButton();
    }

    /*
     * ExternalSignalParser.ExternalSignalFeedbackReceiver Methods
     */

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onSuggestionsSignal(ExternalSignalParser.SignalSource signalSource) {
        openLibraryItem(LibraryFragment.LibraryItem.SUGGESTED_LEARNING);
    }

    @Override
    public void onSolutionSignal(int solutionId, ExternalSignalParser.SignalSource signalSource) {

            Solution correspondSolution = new Solution( solutionId, "" );
            openSolution(correspondSolution, APITalker.CALL_TYPES.CALL_TYPE_PUSH_NOTIFICATION);

    }

    @Override
    public void onCategorySignal(Solution category, ExternalSignalParser.SignalSource signalSource) {
        if (category.hasCME()) {
            openAlphabeticalSolutions(category);
        } else {
            openSubCategory(category);
        }
    }

    @Override
    public void onSubcategorySignal(Solution category, Solution subcategory, ExternalSignalParser.SignalSource signalSource) {
        Intent intent = new Intent(this, SimpleSolutionsListActivity.class);
        intent.putExtra(SimpleSolutionsListActivity.EXTRA_SOLUTIONS_TYPE, SimpleSolutionsListActivity.TYPE_SOLUTIONS);
        intent.putExtra(SimpleSolutionsListActivity.EXTRA_CATEGORY, category);
        intent.putExtra(SimpleSolutionsListActivity.EXTRA_SUBCATEGORY, subcategory);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.hold);
    }

    @Override
    public void onNoSignal(ExternalSignalParser.SignalSource signalSource) {
        onPageSelected(ViewPagerAdapter.POSITION_NOTIFICATIONS);
    }

    @Override
    public void onLogout(ExternalSignalParser.SignalSource signalSource) {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
        overridePendingTransition(R.anim.slide_up_in, R.anim.hold);
    }

    @Override
    public void onDashboardSignal(ExternalSignalParser.SignalSource signalSource) {
            onPageSelected(ViewPagerAdapter.POSITION_DASHBOARD);
    }

    @Override
    public void onOptiQuerySignal(ExternalSignalParser.SignalSource signalSource) {
            onPageSelected(ViewPagerAdapter.POSITION_OPTI_QUERY);
    }

    @Override
    public void onLibrarySignal(ExternalSignalParser.SignalSource signalSource) {
            onPageSelected(ViewPagerAdapter.POSITION_LIBRARY);
    }

    @Override
    public void onFindTheCodeSignal(ExternalSignalParser.SignalSource signalSource) {
            Intent intent = new Intent( this, FindCodeActivity.class);
            startActivity(intent);
    }

    /*
     * DashboardFragmentListener Methods
     */

    @Override
    public boolean onDeepLinkClicked(String deepLink) {
        return ExternalSignalParser.processDeepLink(this, deepLink, this);
    }

    /*
     * OnGetDashboardBadgeListener
     */

    @Override
    public void onDashboardBadgeCountSuccess(int dashboardBadgeCount) {
        user.setDashboardBadgeCount(dashboardBadgeCount);

        LinearLayout barPagerMenu = (LinearLayout) findViewById(R.id.bottom_bar);
        View itemNotifications = barPagerMenu.findViewById(R.id.dashboard_item);

        TextView badgeCountTextView = (TextView) itemNotifications.findViewById(R.id.badge);
        int badgeCount = user.dashboardBadgeCount();

        if (badgeCount > 0) {
            badgeCountTextView.setText(String.valueOf(badgeCount));
            badgeCountTextView.setVisibility(View.VISIBLE);
        } else {
            badgeCountTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDashboardBadgeCountFailure(String error) {

    }

    @Override
    public void onHideRefreshButton() {
        Button rightButton = (Button) findViewById(R.id.right_button);
        rightButton.setVisibility(View.GONE);
    }

    @Override
    public void onShowRefreshButton() {
        Button rightButton = (Button) findViewById(R.id.right_button);
        rightButton.setVisibility(View.VISIBLE);
    }

    private void initializeUserSettings() {
        settings = dbTalker.initSettings();

        int lastAccessedPage = user.getLastAccessedPage();
        if (lastAccessedPage == -1) {
            currentFragmentIndex = settings.defaultScreen();
        } else {
            currentFragmentIndex = lastAccessedPage;
        }
    }

    @Override
    public int getFragmentPosition() {
        return fragmentsViewPager.getCurrentItem();
    }
}