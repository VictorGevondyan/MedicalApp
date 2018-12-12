package com.implementhit.OptimizeHIT.fragments;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SolutionActivity;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.SolutionRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;
import com.implementhit.OptimizeHIT.api.FragmentPositionProvider;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.CheckConnectionHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import java.util.ArrayList;

@SuppressLint("SetJavaScriptEnabled")
public class DashboardFragment extends Fragment implements SolutionRequestListener, OnBackPressedListener,
        User.OnLoginLogoutListener {

    private static final String SAVE_DASHBOARD_PAGE = "saveDashboardPage";
    private static final String SAVE_DASHBOARD_WEB_STATE = "saveDashboardWebState";

    private static final String OHITAPP = "ohitapp://";
    private static final String SUPPORT_URL = "http://support";

    private final String OPTI_QUERY_PREFERENCES = "optiQueryPreferences";
    private final String LOGOUT_LOGIN = "logoutLogin";

    private View dashboardView;
    LinearLayout dashboardContainer;
    private WebView dashboardWebView;

    private String currentUrl;

    private LoadingDialog loadingDashboardDialog;
    private LoadingDialog loadingSolutionDialog;

    private DashboardFragmentListener listener;
    private FragmentPositionProvider positionProvider;

    private Bundle webState;

    private boolean isConnected = false;

    private boolean isLogoutLogin = false;

    public DashboardFragment() {
//		 setRetainInstance(false);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        OptimizeHIT.sendScreen(GAnalyticsScreenNames.DASHBOARD_SCREEN, null, null, null);

        dashboardView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        User user = User.sharedUser(getActivity());


        user.setOnLoginLogoutListener(this);

        if (savedInstanceState != null) {
            currentUrl = savedInstanceState.getString(SAVE_DASHBOARD_PAGE);
            webState = savedInstanceState.getBundle(SAVE_DASHBOARD_WEB_STATE);
        }

        // We want the Dashboard page to open from beginning after user logout-login.
        // For that reason we do the below operations.
        isLogoutLogin = getLogoutLogin(getActivity());
        if( isLogoutLogin ){
            currentUrl = null;
            webState = null;
            setLogoutLogin( false, getActivity() );
        }

        // The end.


        if (currentUrl == null || currentUrl.length() == 0 || currentUrl.equals("about:blank") ) {
            currentUrl = APITalker.DASHBOARD_URL + user.hash();
        }

        loadingDashboardDialog = new LoadingDialog(getActivity());
        loadingSolutionDialog = new LoadingDialog(getActivity());
        WebChromeClient webClient = new WebChromeClient();

        isConnected = CheckConnectionHelper.isNetworkAvailable(getActivity());

        // If it is the beginning of exploring Dashboard screen ( the screen created from beginning, not resumed after going to background or orientation change),
        // we create and initialize the WebView.

        if (dashboardWebView == null) {

            dashboardWebView = new WebView(getActivity());
            dashboardWebView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

            dashboardWebView.setVerticalScrollBarEnabled(true);
            dashboardWebView.setHorizontalScrollBarEnabled(true);
            dashboardWebView.requestFocusFromTouch();
            dashboardWebView.getSettings().setAppCachePath(getActivity().getCacheDir().getAbsolutePath());
            dashboardWebView.getSettings().setAppCacheEnabled(true);
            dashboardWebView.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
            dashboardWebView.setWebChromeClient(webClient);
            dashboardWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            dashboardWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            dashboardWebView.setWebViewClient( dashboardWebViewClient );

            // We save WebView state on orientation change. If it is null, it means, that or it is the first time we enter the page
            // , etc. , so we must set up some WebView parts.
            if ( webState == null ) {

                if ( isConnected ) {
                    Locker.lock(getActivity());
                    loadingDashboardDialog.show();
                    dashboardWebView.loadUrl(currentUrl);

                } /* else {
					NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, (SuperActivity) getActivity());
					dashboardWebView.loadUrl("about:blank");
				}*/

                // Some settings we need to make what we want

            } else {
                // If webState is not null, it means, that orientation change happens, so we must restore WebView state.

                dashboardWebView.restoreState(webState);
            }

        } else {

            // If  WebView != null , it means, that Dashboard screen was resumed , so we must restore WebView state or set it from scratch.

            if (webState == null && isConnected) {

                if ( currentUrl == null || currentUrl.isEmpty()) {
                    Locker.lock(getActivity());
                    loadingDashboardDialog.show();
                }

                dashboardWebView.loadUrl(currentUrl);

                // Some settings we need to make what we want

            } else {
                dashboardWebView.restoreState(webState);
            }

        }

        initWebViewSettings();

        /* We don’t declare the WebView inside the layout file, but we declare a container instead.
         It is the position where our WebView will be placed inside the activity.
         It is done in order to save and restore WebView state properly.
         Every time the fragment view is created, we restore the WebView state ( if it is not null ), and attach it to the container.
         So the WebView not created again, and for example after orientation change we have the same WebView state as before it.
         Also we detach the WebView, when the fragment view is destroyed. */
        dashboardContainer = (LinearLayout) dashboardView.findViewById(R.id.dashboard_container);
        dashboardContainer.addView(dashboardWebView);

        dashboardWebView.setOnTouchListener(dashboardWebViewTouchListener);

        if (webState != null && !currentUrl.equals(APITalker.DASHBOARD_URL) && !dashboardWebView.canGoBack()) {
            dashboardWebView.restoreState(webState);
        }

        getActivity().getWindow().getDecorView().getViewTreeObserver()
                .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        dashboardView.requestLayout();
                    }

                });

        return dashboardView;

    }

    WebViewClient dashboardWebViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {

//            view.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (!isAdded())
//                        return;
//                }
//            }, 200);
            if (!url.equals("about:blank")) {
                currentUrl = url;
            }

            if (url.startsWith(APITalker.DASHBOARD_URL)) {
                view.clearHistory();
                loadingDashboardDialog.dismiss();
                Locker.unlock(getActivity());
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url == null) {
                return false;
            }

            if (listener != null && listener.onDeepLinkClicked(url)) {
                return true;
            }

            return false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            int supportUrlAccessCount = 0;
            if ((failingUrl.startsWith(SUPPORT_URL) && supportUrlAccessCount == 2)
                    || failingUrl.startsWith(OHITAPP)) {
                return;
            }

            if (positionProvider != null && positionProvider.getFragmentPosition() == 0) {
                showNoInternetError();
            }

            view.stopLoading();
        }

    };

    public void initWebViewSettings(){

        dashboardWebView.getSettings().setSupportZoom(true);
        dashboardWebView.getSettings().setBuiltInZoomControls(true);
        dashboardWebView.getSettings().setDisplayZoomControls(false);
        dashboardWebView.getSettings().setLoadWithOverviewMode(true);
        dashboardWebView.getSettings().setUseWideViewPort(true);
        dashboardWebView.getSettings().setJavaScriptEnabled(true);
        dashboardWebView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        dashboardWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        dashboardWebView.setWebChromeClient( new WebChromeClient() );

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof FragmentPositionProvider) {
            positionProvider = (FragmentPositionProvider) getActivity();
        }

        if (isConnected) {
            reloadWebView();
        } else  if (positionProvider != null && positionProvider.getFragmentPosition() == 0){
            showNoInternetError();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        positionProvider = null;
    }

    @Override
    public void onDestroyView() {
        LinearLayout dashboardContainer = (LinearLayout) dashboardView.findViewById(R.id.dashboard_container);
        dashboardContainer.removeView(dashboardWebView);

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        String currentPage = getCurrentPage();
        outState.putString(SAVE_DASHBOARD_PAGE, currentPage);
        outState.putBundle(SAVE_DASHBOARD_WEB_STATE, getCurrentWebViewState());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof DashboardFragmentListener) {
            listener = (DashboardFragmentListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    View.OnTouchListener dashboardWebViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if(!isConnected){
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, (SuperActivity) getActivity());
                }

                return true;
            }

            return false;
        }
    };

    public String getCurrentPage() {
        return dashboardWebView.getUrl();
    }

    public Bundle getCurrentWebViewState() {
        Bundle outState = new Bundle();
        dashboardWebView.saveState(outState);
        return outState;
    }

    @Override
    public void solutionSuccess(int solutionId, String title, String html, String speech) {
        loadingSolutionDialog.dismiss();

        Solution solution = new Solution(solutionId, title);

        ArrayList<Solution> solutions = new ArrayList<>();
        solutions.add(solution);

        ActivityOptions options = ActivityOptions.makeCustomAnimation(getActivity(), R.anim.hold, R.anim.slide_left_in);
        Intent solutionIntent = new Intent(getActivity(), SolutionActivity.class);
        solutionIntent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, APITalker.ACCESS_METHODS.ACCESS_METHOD_DEEPLINKSOLUTION);
        solutionIntent.putExtra(SolutionActivity.EXTRA_HTML, html);
        solutionIntent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);
        solutionIntent.putParcelableArrayListExtra(SolutionActivity.EXTRA_SOLUTIONS, solutions);
        solutionIntent.putExtra(SolutionActivity.EXTRA_POSITION, 0);
        startActivityForResult(solutionIntent, SolutionActivity.REQUEST_CODE, options.toBundle());

        Locker.unlock(getActivity());
    }

    @Override
    public void solutionFailure(String error) {
        loadingSolutionDialog.dismiss();

        NotificationHelper.showNotification(error, (SuperActivity) getActivity());

        Locker.unlock(getActivity());
    }

    @Override
    public boolean onBackPressed() {
        if (dashboardWebView != null && dashboardWebView.canGoBack()) {
            dashboardWebView.goBack();

            return true;
        }

        return false;
    }

    public void showNoInternetError() {
        loadingDashboardDialog.dismiss();
        Locker.unlock(getActivity());
        NotificationHelper.showNotification(TalkersConstants.JUST_FAILURE, (SuperActivity) getActivity());
    }

    public void reloadWebView() {

        if (dashboardWebView == null) {
            return;
        }

        if (SuperActivity.FORCE_RELOAD_DASHBOARD == true) {
            SuperActivity.FORCE_RELOAD_DASHBOARD = false;
            dashboardWebView.loadUrl(currentUrl);
            return;
        }

        if (dashboardWebView.getUrl() == null
                || dashboardWebView.getUrl().isEmpty()
                || dashboardWebView.getUrl().equals("about:blank")) {

            dashboardWebView.loadUrl(currentUrl);

        }

    }

    public void onConnectionUpdate(boolean isConnected){
        this.isConnected = isConnected;

        if (isConnected) {
            reloadWebView();
        }
    }

    public interface DashboardFragmentListener {
        boolean onDeepLinkClicked(String deepLink);
    }

    /**
     * The OnLoginLogoutListener interface method implementation, that we need in order to properly handle
     * the WebView state after login-logout
     */

    @Override
    public void onLoginLogout( Context context ) {
        setLogoutLogin(true, context );
    }

    /**
     * We set and get the variable from persistent storage ( SharedPreferences ), because we need its value to remain between
     * DashboardFragment recreation
     */
    public  void setLogoutLogin( boolean logoutLogin, Context context ){

        isLogoutLogin = logoutLogin;
        SharedPreferences sharedPreferences = context.getSharedPreferences(  OPTI_QUERY_PREFERENCES ,Context.MODE_PRIVATE );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean( LOGOUT_LOGIN, isLogoutLogin );
        editor.commit();

    }

    public boolean getLogoutLogin( Context context ){
        SharedPreferences sharedPreferences = context.getSharedPreferences( OPTI_QUERY_PREFERENCES ,Context.MODE_PRIVATE);
        return  sharedPreferences.getBoolean(  LOGOUT_LOGIN, false );
    }

}





























