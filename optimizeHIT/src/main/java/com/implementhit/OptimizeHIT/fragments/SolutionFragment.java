package com.implementhit.OptimizeHIT.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.ResultsActivity;
import com.implementhit.OptimizeHIT.activity.SolutionActivity;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.adapter.SimpleSolutionsAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.analytics.GAnalitycsEventNames;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.APITalker.CALL_TYPES;
import com.implementhit.OptimizeHIT.api.FeedbackRequestListener;
import com.implementhit.OptimizeHIT.api.RateSolutionListener;
import com.implementhit.OptimizeHIT.api.SolutionRequestListener;
import com.implementhit.OptimizeHIT.api.TalkersConstants;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.FeedbackMenuDialog;
import com.implementhit.OptimizeHIT.dialogs.FeedbackMenuDialog.FeedbackMenuDialogHandler;
import com.implementhit.OptimizeHIT.dialogs.InputDialog;
import com.implementhit.OptimizeHIT.dialogs.InputDialog.InputDialogHandler;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.gcm.GcmBroadcastReceiver;
import com.implementhit.OptimizeHIT.models.Settings;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.HelperConstants;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class SolutionFragment extends Fragment implements OnCompletionListener,
        AnimationListener, OnInitListener, SolutionRequestListener,
        FeedbackMenuDialogHandler, FeedbackRequestListener, InputDialogHandler,
        OnClickListener, OnSolutionItemClickListener, RateSolutionListener {
    public static final String SPEECH_EXTRA = "speechExtra";
    public static final String HTML_EXTRA = "htmlExtra";
    public static final String SOLUTIONS_EXTRA_FRAGMENT = "solutionsExtraFragment";
    public static final String POSITION_EXTRA = "positionExtra";
    public static final String ACCESS_METHOD_EXTRA = "accessMethodExtra";
    public static final String VOICE_QUERY_EXTRA = "voiceQueryExtra";
    public static final String SAVE_REMOVED_FAVORITES = "saveRemovedFavorites";
    public static final String SAVE_ADDED_FAVORITES = "saveAddedFavorites";
    public static final String IS_VOICE_HISTORY = "isVoiceHistory";
    public static final String CURRENT_BAR_BUTTON = "currentBarButton";

    public static final int REQUEST_CODE = 9915;

    public static final String SPEECH_ERROR = "speechError";
    public static final String THANKS_ERROR_LOL = "thanksErrorLol";

    private static final String SAVE_HTMLS = "saveHtmls";
    private static final String SAVE_SPEECHES = "saveSpeeches";
    private static final String SAVE_FEEDBACK_MENU = "saveFeedbackMenu";
    private static final String SAVE_SPEEKING = "saveSpeeking";
    private static final String SAVE_SPOKED = "saveSpoked";
    private static final String SAVE_SPEECH_READY = "saveSpeechReady";
    private static final String SAVE_WATSON_PAGE = "saveWatsonPage";
    private static final String SAVE_WATSON_HAS_BACK_OPTION = "saveWatsonHasBackOption";
    private static final String SAVE_AUTOMATIC_CLOSED = "saveAutomaticClosed";
    private static final String SAVED_WEB_VIEW_SCROLL_POSITION = "savedScrollPosition";
    private static final String UTTERANCE_ID = "wpta";
    private static final String FILENAME = "optimizehitSpeech";

    public static final float MIN_SIGNIFICANT_RATING = 3.5f;

    private RecyclerView solutionsListView;

    private RelativeLayout playPanelRelativeLayout;
    private RelativeLayout toolbarRelativeLayout;
    private RelativeLayout solutionBackButtonRelativeLayout;
    private RelativeLayout solutionForwardButtonRelativeLayout;
    private RelativeLayout bottomBarRelativeLayout;
    private FrameLayout setAutomaticFrameLayout;
    private WebView watsonWebView;
    private WebView solutionWebView;
    private Button barLeftButton;
    private Button barRightButton;

    private TextView watsonLoadingTextView;
    private TextView playTextView;
    private TextView pauseTextView;
    private TextView playPanelTextView;
    private TextView titleTextView;

    private FontsHelper fonts;

    private int animationsDone = 0;

    private String mime = "text/html";
    private String encoding = "utf-8";

    private TextToSpeech textToSpeech;
    private AudioManager audioManager;
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean isSpeechReady = false;
    private boolean hasSpoked = false;
    private boolean setAutomaticClosed = false;

    private ArrayList<Solution> solutions;
    private Solution currentSolution;
    private int solutionPosition;
    private int currentSolutionId;

    private String[] speeches;
    private String[] htmls;

    private String accessMethod;
    private String voiceQuery;

    private LoadingDialog loadingDialog;
    private LoadingDialog sendDialog;
    private LoadingDialog speechDialog;

    private boolean forwardingSolution = false;
    private boolean backingSolution = false;

    private boolean isVoiceHistory = false;

    private boolean isLiked = false;
    private boolean isFavorite = false;

    private boolean isFeedbackMenuShown = false;

    private boolean watsonHasBackOption = false;

    private boolean hasWatsonAccess = false;
    private boolean hasVoiceAccess = true;
    private boolean showingResults = false;

    private boolean engineInitialized = false;

    private boolean needsToResumePlaying = false;

    // We need WebView to restore its scroll position on screen rotate, so we need to know whether the WebView is scrolled.
    private boolean isScrolled = false;

    private ArrayList<Solution> removedFavorites = new ArrayList<Solution>();
    private ArrayList<Solution> addedFavorites = new ArrayList<Solution>();

    private View solutionFragmentView;

    private SolutionFragmentListener listener;

    private SimpleSolutionsAdapter adapter;

    private int currentBarButton;

    private int scrollPosition = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {

        solutionFragmentView = inflater.inflate(R.layout.fragment_solution, parent, false);

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        fonts = FontsHelper.sharedHelper(getActivity());

        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);

        RelativeLayout playAndTextRelativeLayout = (RelativeLayout) solutionFragmentView.findViewById(R.id.play_and_text);
        View playContainer = solutionFragmentView.findViewById(R.id.play_container);

        titleTextView = (TextView) solutionFragmentView.findViewById(R.id.content_title);
        playPanelRelativeLayout = (RelativeLayout) solutionFragmentView.findViewById(R.id.play_panel);
        toolbarRelativeLayout = (RelativeLayout) solutionFragmentView.findViewById(R.id.toolbar);
        solutionBackButtonRelativeLayout = (RelativeLayout) solutionFragmentView.findViewById(R.id.solution_back_button);
        solutionForwardButtonRelativeLayout = (RelativeLayout) solutionFragmentView.findViewById(R.id.solution_forward_button);
        bottomBarRelativeLayout = (RelativeLayout) solutionFragmentView.findViewById(R.id.bottom_bar);
        setAutomaticFrameLayout = (FrameLayout) solutionFragmentView.findViewById(R.id.set_automatic);
        solutionsListView = (RecyclerView) solutionFragmentView.findViewById(R.id.solutions_list);
        watsonWebView = (WebView) solutionFragmentView.findViewById(R.id.watson);
        solutionWebView = (WebView) solutionFragmentView.findViewById(R.id.solution);
        barLeftButton = (Button) solutionFragmentView.findViewById(R.id.bar_button_left);
        barRightButton = (Button) solutionFragmentView.findViewById(R.id.bar_button_right);
        watsonLoadingTextView = (TextView) solutionFragmentView.findViewById(R.id.watson_loading);
        playTextView = (TextView) solutionFragmentView.findViewById(R.id.play_button);
        pauseTextView = (TextView) solutionFragmentView.findViewById(R.id.pause_button);
        playPanelTextView = (TextView) solutionFragmentView.findViewById(R.id.play_tts);

        playPanelTextView.setText(R.string.play_tts);
        playAndTextRelativeLayout.setOnClickListener(playClickListener);

        playContainer.setBackgroundColor(User.sharedUser(getActivity()).primaryColor());
        bottomBarRelativeLayout.setBackgroundColor(User.sharedUser(getActivity()).primaryColor());

        barLeftButton.setText(R.string.library_results);
        barRightButton.setText(R.string.ibm_watson);

        solutionFragmentView.findViewById(R.id.watson_action_bar_tabs).setVisibility(View.INVISIBLE);

        solutionsListView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setAutomaticClosed = getActivity()
                .getSharedPreferences("SolutionPreferences", 0)
                .getBoolean(User.sharedUser(getActivity()).username() + SAVE_AUTOMATIC_CLOSED, false);

        if (savedInstanceState != null) {
            isFeedbackMenuShown = savedInstanceState.getBoolean(SAVE_FEEDBACK_MENU);
            removedFavorites = savedInstanceState.getParcelableArrayList(SAVE_REMOVED_FAVORITES);
            addedFavorites = savedInstanceState.getParcelableArrayList(SAVE_ADDED_FAVORITES);

            htmls = savedInstanceState.getStringArray(SAVE_HTMLS);
            speeches = savedInstanceState.getStringArray(SAVE_SPEECHES);
        }

        setupDialogs();
        setupAnimations();
        setupIcons();

        setAutomaticFrameLayout.setVisibility(View.GONE);

        if (getActivity().getIntent().getDataString() != null
                && !getActivity().getIntent().getDataString().isEmpty()) {
            setupSchemeSolution();
        } else if (getActivity().getIntent().getAction() != null
                && getActivity().getIntent().getAction().equals(GcmBroadcastReceiver.NOTIFICATION_ACTION)) {
            setupPushNotification();
        } else {
            setupSolutionsAndWatson(savedInstanceState);
        }

        setupSolutionTools();

        if (savedInstanceState != null) {
            needsToResumePlaying = savedInstanceState.getBoolean(SAVE_SPEEKING);
            isSpeechReady = savedInstanceState.getBoolean(SAVE_SPEECH_READY);
            hasSpoked = savedInstanceState.getBoolean(SAVE_SPOKED);

            currentBarButton = savedInstanceState.getInt(CURRENT_BAR_BUTTON);

//            isScrolled = savedInstanceState.getBoolean(SAVED_WEB_VIEW_IS_SCROLLED);
            scrollPosition = savedInstanceState.getInt(SAVED_WEB_VIEW_SCROLL_POSITION);
        }

        if (isFeedbackMenuShown) {
            leaveFeedback(null);
        }

        InputDialog inputDialog = (InputDialog) getActivity().getSupportFragmentManager().findFragmentByTag("InputDialog");

        if (inputDialog != null) {
            inputDialog.setHandler(this);
        }

        AppCompatRatingBar ratingBar = (android.support.v7.widget.AppCompatRatingBar) solutionFragmentView.findViewById(R.id.rating_bar);
        ColorUtil.changeRatingBarColor(ratingBar, User.sharedUser(getActivity()).primaryColor());

        if( solutionPosition >= 0 ) {
            currentSolution = solutions.get(solutionPosition);
            ratingBar.setRating( currentSolution.rating() );
        }

        ratingBar.setOnRatingBarChangeListener(solutionRatingBarChangeListener);

        checkIsFavorite();

        return solutionFragmentView;

    }

    // !!!!!!!!!  Later, when we will get the items ( solutions, categories, etc. ), which are rated, from corresponding table of the Database,
    // we must join that table with the favorites table, as the rated item goes to that table.

    RatingBar.OnRatingBarChangeListener solutionRatingBarChangeListener = new RatingBar.OnRatingBarChangeListener() {

        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            String ratingString = String.valueOf(rating);

            OptimizeHIT.sendEvent(
                    GAnalitycsEventNames.RATE_SOLUTION.CATEGORY,
                    GAnalitycsEventNames.RATE_SOLUTION.ACTION,
                    ratingString);

            User user = User.sharedUser(getActivity());
            String hash = user.hash();

            if( currentSolution != null ) {
                currentSolution.setRating(rating);
                int currentSolutionId = currentSolution.solutionId();

                DBTalker dbTalker = DBTalker.sharedDB( getActivity() );
                dbTalker.addFavorite( currentSolutionId, rating, System.currentTimeMillis() / 1000 );
            }

            APITalker.sharedTalker().rateSolution( hash, getActivity().getApplicationContext(), currentSolutionId, rating,
                    SolutionFragment.this);

        }

    };

    @Override
    public void rateSolutionSuccess() {
        if( isAdded() ) {
            checkIsFavorite();
        }
    }

    @Override
    public void rateSolutionFail(String error) {

        Locker.unlock(getActivity());

        loadingDialog.dismiss();

    }


    public void onWindowFocusChanged(boolean hasFocus) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SolutionActivity.REQUEST_CODE) {
            if (resultCode == HelperConstants.RESULT_HASH_EXPIRED) {
                getActivity().setResult(HelperConstants.RESULT_HASH_EXPIRED);
                getActivity().finish();
            }
        }
    }

    @Override
    public void onResume() {
        textToSpeech = new TextToSpeech(getActivity(), this);

        if (needsToResumePlaying) {
            if (isSpeechReady) {
                playMediaPlayer(false);
                needsToResumePlaying = false;
            }
        }

        IntentFilter intentFilter = new IntentFilter(APITalker.ACTION_NEW_SOLUTION_DATA);
        getActivity().registerReceiver(newSolutionDataReceiver, intentFilter);

        refreshData();

        super.onResume();
    }

    @Override
    public void onPause() {
        if (mediaPlayer.isPlaying()) {
            pauseMediaPlayer();
            needsToResumePlaying = true;
        }

        textToSpeech.shutdown();

        getActivity().unregisterReceiver(newSolutionDataReceiver);

        refreshData();

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle watsonOutState = new Bundle();
        watsonWebView.saveState(watsonOutState);

        outState.putInt(CURRENT_BAR_BUTTON, currentBarButton);
        outState.putStringArray(SAVE_HTMLS, htmls);
        outState.putStringArray(SAVE_SPEECHES, speeches);
        outState.putBoolean(SAVE_FEEDBACK_MENU, isFeedbackMenuShown);
        outState.putBoolean(SAVE_SPEEKING, mediaPlayer.isPlaying() || needsToResumePlaying);
        outState.putBoolean(SAVE_SPEECH_READY, isSpeechReady);
        outState.putBoolean(SAVE_SPOKED, hasSpoked);
        outState.putBoolean(SAVE_WATSON_HAS_BACK_OPTION, watsonHasBackOption);
        outState.putParcelableArrayList(SAVE_REMOVED_FAVORITES, removedFavorites);
        outState.putParcelableArrayList(SAVE_ADDED_FAVORITES, addedFavorites);
        outState.putBundle(SAVE_WATSON_PAGE, watsonOutState);
        outState.putInt(SAVED_WEB_VIEW_SCROLL_POSITION, solutionWebView.getScrollY());

        getActivity().getIntent().putExtra(POSITION_EXTRA, solutionPosition);

        super.onSaveInstanceState(outState);
    }

    public void onBackPressed() {
        backButtonClicked(null);
    }

    /**
     * OnClickListener Methods
     */

    @Override
    public void onClick(View view) {
        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 50) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        if (view.getId() == R.id.right_button) {
            checkButtonClicked(view);
        } else if (view.getId() == R.id.left_button) {
            backButtonClicked(view);
        } else if (view.getId() == R.id.solution_back_button) {
            solutionBackward(view);
        } else if (view.getId() == R.id.solution_forward_button) {
            solutionForward(view);
        } else if (view.getId() == R.id.bar_button_left) {
            showSolutions();
        } else if (view.getId() == R.id.bar_button_right) {
            showWatson();
        } else if (view.getId() == R.id.close_set_automatic) {
            setAutomaticFrameLayout.setVisibility(View.GONE);
            setAutomaticClosed = true;
            getActivity()
                    .getSharedPreferences("SolutionPreferences", 0)
                    .edit()
                    .putBoolean(User.sharedUser(getActivity()).username() + SAVE_AUTOMATIC_CLOSED, true)
                    .commit();
        }
    }

    OnClickListener playClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            play(view);
        }
    };

    /**
     * Bar Navigation Methods
     */

    private void showSolutions() {
        if (currentBarButton == 0) {
            return;
        }

        if (solutionPosition > -1) {

            solutionWebView.setVisibility(View.VISIBLE);
            toolbarRelativeLayout.setVisibility(View.VISIBLE);
            bottomBarRelativeLayout.setVisibility(View.VISIBLE);
            playPanelRelativeLayout.setVisibility(View.VISIBLE);
            solutionBackButtonRelativeLayout.setVisibility(View.VISIBLE);
            solutionForwardButtonRelativeLayout.setVisibility(View.VISIBLE);

            titleTextView.setText(R.string.solution);

            sendScreen();

        } else {
            OptimizeHIT.sendScreen(
                    GAnalyticsScreenNames.MULTIPLE_OPTIQUERY_RESULTS_SCREEN,
                    null, null, null );

            solutionsListView.setVisibility(View.VISIBLE);
            bottomBarRelativeLayout.setVisibility(View.GONE);
            toolbarRelativeLayout.setVisibility(View.GONE);
        }

        if (setAutomaticClosed || solutionPosition < 0) {
            setAutomaticFrameLayout.setVisibility(View.GONE);
        } else {
            setAutomaticFrameLayout.setVisibility(View.VISIBLE);
        }

        watsonWebView.setVisibility(View.GONE);

        barLeftButton.setTextColor(User.sharedUser(getActivity()).primaryColor());
        barRightButton.setTextColor(getResources().getColor(R.color.background_light_grey));
        barLeftButton.setBackgroundResource(R.drawable.results_bar_left_active);
        barRightButton.setBackgroundResource(R.drawable.results_bar_right_inactive);

        currentBarButton = 0;
    }

    private void showWatson() {
        if (currentBarButton == 1) {
            return;
        }

        OptimizeHIT.sendScreen(GAnalyticsScreenNames.WATSON_SCREEN, null, null, null);

        if (mediaPlayer.isPlaying()) {
            pauseMediaPlayer();
        }

        watsonWebView.setVisibility(View.VISIBLE);
        bottomBarRelativeLayout.setVisibility(View.VISIBLE);
        solutionWebView.setVisibility(View.GONE);
        toolbarRelativeLayout.setVisibility(View.GONE);
        playPanelRelativeLayout.setVisibility(View.GONE);
        solutionForwardButtonRelativeLayout.setVisibility(View.GONE);

        if (!watsonHasBackOption) {
            solutionBackButtonRelativeLayout.setVisibility(View.GONE);
        }

        if (watsonHasBackOption) {
            watsonLoadingTextView.setVisibility(View.VISIBLE);
        }

        setAutomaticFrameLayout.setVisibility(View.GONE);

        barRightButton.setTextColor(User.sharedUser(getActivity()).primaryColor());
        barLeftButton.setTextColor(getResources().getColor(R.color.background_light_grey));
        barRightButton.setBackgroundResource(R.drawable.results_bar_right_active);
        barLeftButton.setBackgroundResource(R.drawable.results_bar_left_inactive);

        currentBarButton = 1;
    }

    /**
     * Like and Favorite Helper Methods
     */

    private void checkIsFavorite() {

        isFavorite = DBTalker.sharedDB(getActivity()).isFavorite(currentSolutionId);

        android.support.v7.widget.AppCompatRatingBar ratingBar = (android.support.v7.widget.AppCompatRatingBar) solutionFragmentView
                .findViewById(R.id.rating_bar);

        if( DBTalker.sharedDB(getActivity()).isFavorite(currentSolutionId) ) {
            Drawable progressDrawable = ratingBar.getProgressDrawable();
            DrawableCompat.setTint( progressDrawable, User.sharedUser(getActivity()).primaryColor());
        } else {
            Drawable progress = ratingBar.getProgressDrawable();
            DrawableCompat.setTint( progress, getResources().getColor(R.color.dark_green) );
//            if( currentSolution != null ) {
//                ratingBar.setRating(currentSolution.rating());
//            }
        }

    }

    /**
     * Bottom Bar Navigation Helpers
     */

    private void setupNavigation() {
        TextView solutionBack = (TextView) solutionFragmentView.findViewById(R.id.solution_back);
        TextView solutionForward = (TextView) solutionFragmentView.findViewById(R.id.solution_forward);

        int solutionsSize = solutions == null ? 0 : solutions.size();

        if (bottomBarRelativeLayout.getVisibility() == View.GONE) {
            bottomBarRelativeLayout.setVisibility(View.VISIBLE);
        }

        View playPanel = playPanelRelativeLayout;

        if (playPanel.getVisibility() == View.GONE) {
            playPanel.setVisibility(View.VISIBLE);
        }

        if (solutionPosition > 0) {
            solutionBackButtonRelativeLayout.setVisibility(View.VISIBLE);

            solutionBack.setText(solutions.get(solutionPosition - 1).title());

            TextView backwardIcon = (TextView) solutionFragmentView.findViewById(R.id.solution_backward_icon);
            backwardIcon.setVisibility(View.VISIBLE);
        } else {
            solutionBack.setText("");

            TextView backwardIcon = (TextView) solutionFragmentView.findViewById(R.id.solution_backward_icon);
            backwardIcon.setVisibility(View.INVISIBLE);
        }
        if (solutionPosition < solutionsSize - 1) {
            solutionForwardButtonRelativeLayout.setVisibility(View.VISIBLE);

            solutionForward.setText(solutions.get(solutionPosition + 1).title());

            TextView forwardIcon = (TextView) solutionFragmentView.findViewById(R.id.solution_forward_icon);
            forwardIcon.setVisibility(View.VISIBLE);
        } else {
            solutionForward.setText("");

            TextView forwardIcon = (TextView) solutionFragmentView.findViewById(R.id.solution_forward_icon);
            forwardIcon.setVisibility(View.INVISIBLE);
        }
    }

    public void solutionForward(View view) {
        if (solutionPosition < solutions.size() - 1) {
            Locker.lock(getActivity());

            loadingDialog.show();

            OptimizeHIT.sendEvent(
                    GAnalitycsEventNames.SOLUTION_BOTTOM_NAVIGATION.CATEGORY,
                    GAnalitycsEventNames.SOLUTION_BOTTOM_NAVIGATION.ACTION,
                    "Next");

            User user = User.sharedUser(getActivity());
            Solution solution = solutions.get(solutionPosition + 1);
            backingSolution = false;
            forwardingSolution = true;

            String callType = determineCallType();

            APITalker
                    .sharedTalker()
                    .getSolution(user.hash(), solution.solutionId(), callType, this);

            if (mediaPlayer.isPlaying()) {
                pauseMediaPlayer();
            }
        }
    }

    public void solutionBackward(View view) {
        if (currentBarButton == 1) {
            watsonWebView.loadData(ResultsActivity.getWatsonHtml(), mime, encoding);

            watsonHasBackOption = false;

            return;
        }

        if (solutionPosition > 0) {
            Locker.lock(getActivity());

            loadingDialog.show();

            OptimizeHIT.sendEvent(
                    GAnalitycsEventNames.SOLUTION_BOTTOM_NAVIGATION.CATEGORY,
                    GAnalitycsEventNames.SOLUTION_BOTTOM_NAVIGATION.ACTION,
                    "Previous");

            User user = User.sharedUser(getActivity());
            Solution solution = solutions.get(solutionPosition - 1);
            forwardingSolution = false;
            backingSolution = true;

            String callType = determineCallType();

            APITalker.sharedTalker().getSolution(user.hash(),
                    solution.solutionId(), callType, this);

            if (mediaPlayer.isPlaying()) {
                pauseMediaPlayer();
            }
        }
    }

    public void leaveFeedback(View view) {
        isFeedbackMenuShown = true;
        FeedbackMenuDialog feedbackMenuDialog = new FeedbackMenuDialog(
                getActivity(), this);
        feedbackMenuDialog.show();
    }

    /**
     * Action Bar Methods
     */

    private void backButtonClicked(View view) {
        if (showingResults && solutionPosition > -1 && currentBarButton == 0 && solutions.size() != 1) {
            bottomBarRelativeLayout.clearAnimation();
            playPanelRelativeLayout.clearAnimation();

            solutionPosition = -1;

            adapter = new SimpleSolutionsAdapter(getActivity(), solutions);
            solutionWebView.setVisibility(View.GONE);
            solutionsListView.setAdapter(adapter);
            adapter.setRecyclerView(solutionsListView);
            adapter.setOnSolutionItemClickListener(this);

            solutionsListView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();


            bottomBarRelativeLayout.setVisibility(View.GONE);
            playPanelRelativeLayout.setVisibility(View.GONE);
            toolbarRelativeLayout.setVisibility(View.GONE);

            setAutomaticFrameLayout.setVisibility(View.GONE);

            pauseMediaPlayer();

            return;
        }

        if (listener != null) {
            listener.backClicked(addedFavorites, removedFavorites);
        }
    }

    private void checkButtonClicked(View view) {
        if (listener != null) {
            listener.checkMarkClicked(addedFavorites, removedFavorites);
        }
    }

    /**
     * Play Methods
     */

    public void play(View view) {
        if (speeches == null || speeches.length == 0) {
            return;
        }

        if (!mediaPlayer.isPlaying()) {
            OptimizeHIT
                    .sendEvent(
                            GAnalitycsEventNames.SOLUTION_TEXT_TO_SPEECH.CATEGORY,
                            GAnalitycsEventNames.SOLUTION_TEXT_TO_SPEECH.ACTION,
                            "Play");

            if (isSpeechReady) {
                playMediaPlayer(false);
            } else {
                if (!engineInitialized) {
                    NotificationHelper.showNotification(SPEECH_ERROR,
                            (SuperActivity) getActivity());

                    return;
                }

                mediaPlayer.reset();

                Locker.lock(getActivity());

                speechDialog.show();
                needsToResumePlaying = true;
                hasSpoked = false;

                setTTSListener();
                loadTTS();
            }
        } else {
            OptimizeHIT.sendEvent(
                    GAnalitycsEventNames.SOLUTION_TEXT_TO_SPEECH.CATEGORY,
                    GAnalitycsEventNames.SOLUTION_TEXT_TO_SPEECH.ACTION,
                    "Pause");

            pauseMediaPlayer();
        }
    }

    private void initializeMediaPlayer() {
        String fileName = getActivity().getExternalCacheDir().getAbsolutePath() + FILENAME;
        Uri uri = Uri.parse("file://" + fileName);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(getActivity().getApplicationContext(), uri);
            mediaPlayer.prepare();
            mediaPlayer.setOnCompletionListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playMediaPlayer(final boolean noAnim) {
        requestAudioFocus();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (noAnim) {
                    playTextView.setAlpha(0);
                    pauseTextView.setAlpha(1);
                } else {
                    ObjectAnimator.ofFloat(playTextView, "alpha", 1.0f, 0.0f).setDuration(500).start();
                    ObjectAnimator.ofFloat(pauseTextView, "alpha", 0.0f, 1.0f).setDuration(500).start();
                }

                playPanelTextView.setText(R.string.pause_tts);

            }
        });
        mediaPlayer.start();
    }

    private void pauseMediaPlayer() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                ObjectAnimator.ofFloat(playTextView, "alpha", 0.0f, 1.0f).setDuration(500).start();
                ObjectAnimator.ofFloat(pauseTextView, "alpha", 1.0f, 0.0f).setDuration(500).start();

                playPanelTextView.setText(R.string.play_tts);

            }
        });

        mediaPlayer.pause();

        audioManager.abandonAudioFocus(null);
    }

    private boolean requestAudioFocus() {
        int result = audioManager.requestAudioFocus(null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * TTS Methods
     */

    @SuppressLint("NewApi")
    private void loadTTS() {
        HashMap<String, String> hashRender = new HashMap<String, String>();
        hashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, UTTERANCE_ID);

        String fileName = getActivity().getExternalCacheDir().getAbsolutePath() + FILENAME;
        File file = new File(fileName);

        if (solutionPosition > -1) {
            Settings settings = DBTalker.sharedDB(getActivity()).initSettings();
            textToSpeech.setSpeechRate(settings.speechSpeed());
            textToSpeech.setLanguage(Locale.US);

            if (Build.VERSION.SDK_INT >= 21) {
                textToSpeech.synthesizeToFile(speeches[solutionPosition], null, file,
                        UTTERANCE_ID);
            } else {
                textToSpeech.synthesizeToFile(speeches[solutionPosition], hashRender, fileName);
            }
        }
    }

    private void setTTSListener() {
        if (Build.VERSION.SDK_INT >= 15) {
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {

                @Override
                public void onDone(String utteranceId) {
                    isSpeechReady = true;
                    initializeMediaPlayer();
                    playMediaPlayer(false);

                    speechDialog.dismiss();

                    Locker.unlock(getActivity());
                }

                @Override
                public void onError(String utteranceId) {
                    speechDialog.dismiss();

                    NotificationHelper.showNotification(SPEECH_ERROR,
                            (SuperActivity) getActivity());

                    Locker.unlock(getActivity());
                }

                @Override
                public void onStart(String utteranceId) {
                }
            });
        } else {
            textToSpeech.setOnUtteranceCompletedListener(new OnUtteranceCompletedListener() {
                @Override
                public void onUtteranceCompleted(String utteranceId) {
                    isSpeechReady = true;
                    initializeMediaPlayer();
                    playMediaPlayer(false);

                    speechDialog.dismiss();

                    Locker.unlock(getActivity());
                }
            });
        }
    }

    /**
     * These are server API calls Handler Methods, which give responses to that calls
     */

    /**
     * SolutionHandler Methods
     */

    @Override
    public void solutionSuccess(int solutionId, String title,
                                String html, String speech) {

        if (accessMethod.equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_PUSH_NOTIFICATION)
                || accessMethod.equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_DEEPLINKSOLUTION)) {
            speeches = new String[]{speech};
            htmls = new String[]{html};
            solutions = new ArrayList<>();
            solutions.add(new Solution(solutionId, title));
            solutionPosition = 0;

            getActivity().getIntent().putExtra(SOLUTIONS_EXTRA_FRAGMENT, solutions);
        }

        DBTalker.sharedDB(getActivity()).saveHistory(solutionId, title, isVoiceHistory);

        isSpeechReady = false;

        if (forwardingSolution) {
            solutionPosition++;
            forwardingSolution = false;
        }
        if (backingSolution) {
            solutionPosition--;
            backingSolution = false;
        }

        currentSolutionId = solutions.get(solutionPosition).solutionId();

        // Set rating bar value for current solution
        android.support.v7.widget.AppCompatRatingBar ratingBar = (android.support.v7.widget.AppCompatRatingBar) solutionFragmentView
                .findViewById(R.id.rating_bar);

        if( solutionPosition >= 0 ) {
            currentSolution = solutions.get(solutionPosition);
            ratingBar.setRating( currentSolution.rating() );
        }

        if (solutionsListView.getVisibility() == View.VISIBLE) {
            solutionsListView.setVisibility(View.GONE);
            solutionWebView.setVisibility(View.VISIBLE);
            toolbarRelativeLayout.setVisibility(View.VISIBLE);

            if (setAutomaticClosed || solutionPosition < 0) {
                setAutomaticFrameLayout.setVisibility(View.GONE);
            } else {
                setAutomaticFrameLayout.setVisibility(View.VISIBLE);
            }
        }

        sendScreen();

        checkIsFavorite();

        setupNavigation();

        speeches[solutionPosition] = speech;
        htmls[solutionPosition] = html;
        solutionWebView.loadData(htmls[solutionPosition], mime, encoding);

        if (textToSpeech.isSpeaking()) {

            textToSpeech.stop();

            OptimizeHIT
                    .sendEvent(
                            GAnalitycsEventNames.SOLUTION_TEXT_TO_SPEECH.CATEGORY,
                            GAnalitycsEventNames.SOLUTION_TEXT_TO_SPEECH.ACTION,
                            "Stop");

        }

        if (DBTalker.sharedDB(getActivity()).initSettings().isAutoStartSpeech()) {
            play(null);
        }

        loadingDialog.dismiss();

        Locker.unlock(getActivity());
    }

    @Override
    public void solutionFailure(String error) {
        loadingDialog.dismiss();

        if (solutions == null) {
            solutions = new ArrayList<>();
            solutions.add(new Solution(currentSolutionId, ""));
        }

        if (solutionWebView.getVisibility() == View.VISIBLE) {
            setupNavigation();
        }

        NotificationHelper.showNotification(error, (SuperActivity) getActivity());

        Locker.unlock(getActivity());
    }

    /**
     * SendHandler Methods
     */

    @Override
    public void onInputSubmitted(String feedback) {
        sendDialog.show();

        Locker.lock(getActivity());

//        OptimizeHIT.sendEvent(
//                GAnalitycsEventNames.SEND_CUSTOM_FEEDBACK.CATEGORY,
//                GAnalitycsEventNames.SEND_CUSTOM_FEEDBACK.ACTION,
//                GAnalitycsEventNames.SEND_CUSTOM_FEEDBACK.LABEL);

        String accessWith = accessMethod;

        if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_OPTIQUERY)
                && solutions.size() > 1) {
            accessWith = APITalker.ACCESS_METHODS.ACCESS_METHOD_MULTIVOICE;
        }

        APITalker.sharedTalker().leaveFeedback(
                User.sharedUser(getActivity()).hash(), accessWith,
                solutions.get(solutionPosition).solutionId(), voiceQuery,
                feedback, this);
    }

    @Override
    public void onInputCanceled() {

    }

    /**
     * FeedbackHandler Methods
     */

    @Override
    public void leaveFeedackSuccess() {
        sendDialog.dismiss();

        NotificationHelper.showNotification(THANKS_ERROR_LOL, (SuperActivity) getActivity());

        Locker.unlock(getActivity());
    }

    @Override
    public void leaveFeedbackFailure(String error) {
        sendDialog.dismiss();

        NotificationHelper.showNotification(error, (SuperActivity) getActivity());

        Locker.unlock(getActivity());
    }

    /**
     * AnimationListener Methods
     */

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        animationsDone++;

        if (animationsDone == 2) {
            animationsDone = 0;
            LinearLayout actionBar = (LinearLayout) solutionFragmentView.findViewById(R.id.action_tool_bars);
            actionBar.setVisibility(View.GONE);
            bottomBarRelativeLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    /**
     * FeedbackMenuDialogHandler Methods
     */

    @Override
    public void sendInstantFeedback(String feedback) {
//        OptimizeHIT.sendEvent(GAnalitycsEventNames.SEND_FEEDBACK.CATEGORY,
//                GAnalitycsEventNames.SEND_FEEDBACK.ACTION,
//                GAnalitycsEventNames.SEND_FEEDBACK.LABEL);

        isFeedbackMenuShown = false;
        onInputSubmitted(feedback);
    }

    @Override
    public void sendFeedback() {
        isFeedbackMenuShown = false;

        Bundle arguments = new Bundle();
        arguments.putString(InputDialog.TITLE, getString(R.string.send_feedback));
        arguments.putString(InputDialog.DESCRIPTION, getString(R.string.please_type_comment));
        arguments.putString(InputDialog.OK_BUTTON, getString(R.string.send));
        arguments.putString(InputDialog.ERROR_DESCRIPTION, getString(R.string.feedback_min_5_letters));
        arguments.putString(InputDialog.ERROR_DEFAULT, "Required");
        arguments.putBoolean(InputDialog.ERROR_AS_HINT, true);
        arguments.putInt(InputDialog.INVALID_MIN, 5);

        InputDialog feedbackDialog = new InputDialog();
        feedbackDialog.setHandler(this);
        feedbackDialog.setArguments(arguments);
        feedbackDialog.show(getFragmentManager(), "InputDialog");
    }

    @Override
    public void feedbackMenuDismiss() {
        isFeedbackMenuShown = false;
    }

    /**
     * OnInitListener Methods
     */

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            engineInitialized = true;

            if (solutionWebView.getVisibility() != View.VISIBLE) {
                return;
            }

            if (!hasSpoked && !mediaPlayer.isPlaying()
                    && (DBTalker.sharedDB(getActivity()).initSettings().isAutoStartSpeech() || needsToResumePlaying)
                    && speeches != null && speeches[solutionPosition] != null && !speeches[solutionPosition].isEmpty()) {
                mediaPlayer.setOnCompletionListener(this);
                play(null);

                needsToResumePlaying = false;
                hasSpoked = true;
            }

            if (mediaPlayer != null) {
                mediaPlayer.setOnCompletionListener(this);
            }
        } else {
            textToSpeech.shutdown();
            textToSpeech = new TextToSpeech(getActivity(), this);
        }
    }

    /**
     * OnCompletionListener Methods
     */

    @Override
    public void onCompletion(MediaPlayer mp) {
        ObjectAnimator.ofFloat(playTextView, "alpha", 0.0f, 1.0f).setDuration(500).start();
        ObjectAnimator.ofFloat(pauseTextView, "alpha", 1.0f, 0.0f).setDuration(500).start();
    }

    /**
     * Analytics Helper
     */

    private void sendScreen() {
        if (accessMethod.equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_BROWSE)) {
            OptimizeHIT
                    .sendScreen(
                            GAnalyticsScreenNames.SOLUTION_VIEW,
                            solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_BROWSE_SCREEN );
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_FAVORITES)) {
            OptimizeHIT
                    .sendScreen(
                            GAnalyticsScreenNames.SOLUTION_VIEW,
                            solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_FAVORITES_SCREEN );
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_PEER_FAVORITES)) {
            OptimizeHIT
                    .sendScreen(
                            GAnalyticsScreenNames.SOLUTION_VIEW,
                            solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_PEER_FAVORITES_SCREEN );
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_HISTORY)) {
            OptimizeHIT
                    .sendScreen(
                            GAnalyticsScreenNames.SOLUTION_VIEW,
                            solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_HISTORY_SCREEN );
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_OPTIQUERY)) {
            if (solutions.size() > 1) {
                OptimizeHIT
                        .sendScreen(
                                GAnalyticsScreenNames.SOLUTION_VIEW,
                                solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_MULTI_VOICE_SCREEN );
            } else {
                OptimizeHIT
                        .sendScreen(
                                GAnalyticsScreenNames.SOLUTION_VIEW,
                                solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_OPTIQUERY_SCREEN );
            }
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_DEEPLINKSOLUTION)) {
            OptimizeHIT
                    .sendScreen(
                            GAnalyticsScreenNames.SOLUTION_VIEW,
                            solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_DEEP_LINKS);
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_PUSH_NOTIFICATION)) {
            OptimizeHIT
                    .sendScreen(
                            GAnalyticsScreenNames.SOLUTION_VIEW,
                            solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_PUSH_NOTIFICATIONS);
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_SUGGESTION)){
            OptimizeHIT.sendScreen(
                    GAnalyticsScreenNames.SOLUTION_VIEW,
                    solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_SUGGESTED_SOLUTIONS_SCREEN);
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_REACTIVATION)){
            OptimizeHIT.sendScreen(
                    GAnalyticsScreenNames.SOLUTION_VIEW,
                    solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_REACTIVATION);
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_LOCATION)) {
            OptimizeHIT.sendScreen(
                    GAnalyticsScreenNames.SOLUTION_VIEW,
                    solutions.get(solutionPosition).title(), null, GAnalyticsScreenNames.ACCESSED_USING_LOCATION);
        }
    }

    private String determineCallType() {
        if (accessMethod.equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_BROWSE)) {
            return APITalker.CALL_TYPES.CALL_TYPE_BROWSE_SOLUTIONS;
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_FAVORITES)) {
            return APITalker.CALL_TYPES.CALL_TYPE_FAVORITES;
        } else if (accessMethod
                .equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_HISTORY)) {
            return APITalker.CALL_TYPES.CALL_TYPE_HISTORY;
        } else {
            return null;
        }
    }

    /**
     * UI Helpers
     */

    private void setupSolutionTools() {
        solutionBackButtonRelativeLayout.setOnClickListener(this);
        solutionForwardButtonRelativeLayout.setOnClickListener(this);
        View playPanelView = playPanelRelativeLayout;
        playPanelView.setOnClickListener(this);

        Button hideSetAutospeach = (Button) solutionFragmentView.findViewById(R.id.close_set_automatic);
        hideSetAutospeach.setOnClickListener(this);
    }

    private void setupIcons() {
        Button ok = (Button) solutionFragmentView.findViewById(R.id.right_button);
        ok.setText(R.string.done);
        ok.setOnClickListener(this);
        ok.setTextSize(15);
        ok.setVisibility(View.VISIBLE);

        LinearLayout leftButtonLayout = (LinearLayout) solutionFragmentView.findViewById(R.id.left_button_layout);
        leftButtonLayout.setVisibility(View.VISIBLE);

        Button back = (Button) solutionFragmentView.findViewById(R.id.left_button);
        back.setText(R.string.icon_angle_left);
        back.setTypeface(fonts.fontello());
        back.setOnClickListener(this);

        playTextView.setTypeface(fonts.fontello());
        pauseTextView.setTypeface(fonts.fontello());

        TextView backwardIcon = (TextView) solutionFragmentView.findViewById(R.id.solution_backward_icon);
        TextView forwardIcon = (TextView) solutionFragmentView.findViewById(R.id.solution_forward_icon);
        backwardIcon.setTypeface(fonts.fontello());
        forwardIcon.setTypeface(fonts.fontello());
        backwardIcon.setOnClickListener(this);
        forwardIcon.setOnClickListener(this);

        int primaryColor = User.sharedUser(getActivity()).primaryColor();
        forwardIcon.setTextColor(primaryColor);
        backwardIcon.setTextColor(primaryColor);

        if (getActivity() instanceof SolutionActivity) {
            titleTextView.setText(R.string.solution);
        } else if (getActivity() instanceof ResultsActivity) {
            titleTextView.setText(R.string.results);
            solutionFragmentView.findViewById(R.id.right_button).setVisibility(View.GONE);
        }
    }

    private void setupAnimations() {
//		slideUpOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_out);
//		slideUpOutAnimation.setFillAfter(true);
//		slideUpOutAnimation.setAnimationListener(this);
//		slideDownOutAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_out);
//		slideDownOutAnimation.setFillAfter(true);
//		slideDownOutAnimation.setAnimationListener(this);
//		slideUpInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up_in);
//		slideUpInAnimation.setFillAfter(true);
//		slideDownInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_down_in);
//		slideDownInAnimation.setFillAfter(true);
    }

    private void setupDialogs() {
        loadingDialog = new LoadingDialog(getActivity());
        sendDialog = new LoadingDialog(getActivity());
        speechDialog = new LoadingDialog(getActivity());
    }

    private void setupButtonBar() {
        barLeftButton.setOnClickListener(this);
        barRightButton.setOnClickListener(this);
    }

    private void setupSolutionWebView() {
        solutionWebView.getSettings().setSupportZoom(true);
        solutionWebView.getSettings().setBuiltInZoomControls(true);
        solutionWebView.getSettings().setDisplayZoomControls(false);
        solutionWebView.getSettings().setLoadWithOverviewMode(true);
        solutionWebView.getSettings().setUseWideViewPort(true);
        solutionWebView.getSettings().setJavaScriptEnabled(true);
        solutionWebView.setWebChromeClient(new WebChromeClient());
        // We set this web client in order to retain solution WebView scroll state after orientation change
        solutionWebView.setWebViewClient(solutionWebViewClient);
        solutionWebView.loadData(htmls[solutionPosition], mime, encoding);
    }

    // this web client in helps to retain solution WebView scroll state after orientation change
    WebViewClient solutionWebViewClient = new WebViewClient() {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            if (!isScrolled) {

                // We use post delayed because the scroll will work AFTER the page is loaded.
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        solutionWebView.scrollBy(0, scrollPosition);
                    }
                }, 250);

                isScrolled = true;

            }

        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    };

    @SuppressLint("SetJavaScriptEnabled")
    private void setupSolutionsAndWatson(Bundle savedInstanceState) {
        if (getActivity() instanceof ResultsActivity) {
            hasWatsonAccess = getActivity().getIntent().getBooleanExtra(ResultsActivity.HAS_WATSON_ACCESS_EXTRA, false);
            hasVoiceAccess = getActivity().getIntent().getBooleanExtra(ResultsActivity.HAS_VOICE_ACCESS_EXTRA, false);
            showingResults = true;
            currentBarButton = 0;
        }

        accessMethod = getActivity().getIntent().getStringExtra(SolutionActivity.EXTRA_ACCESS_METHOD);
        voiceQuery = getActivity().getIntent().getStringExtra(VOICE_QUERY_EXTRA);

        isVoiceHistory = getActivity().getIntent().getBooleanExtra(IS_VOICE_HISTORY, false);


        // OptiQueryFragment send solutions ( as the result of the search ) with the help of SolutionActivity.EXTRA_SOLUTIONS
        // and Intent to the ResultsActivity. Here we first get the ResultsActivity ( with getActivity() call ), then the Intent, sent
        // by OptiQueryFragment, and finally the solutions ( search results ).
        solutions = getActivity().getIntent().getParcelableArrayListExtra(SolutionActivity.EXTRA_SOLUTIONS);
        solutionPosition = getActivity().getIntent().getIntExtra(POSITION_EXTRA, 0);

        if (!hasWatsonAccess && !hasVoiceAccess) {
            solutionWebView.setVisibility(View.GONE);
            toolbarRelativeLayout.setVisibility(View.GONE);
            playPanelRelativeLayout.setVisibility(View.GONE);
            solutionBackButtonRelativeLayout.setVisibility(View.GONE);
            solutionForwardButtonRelativeLayout.setVisibility(View.GONE);
            bottomBarRelativeLayout.setVisibility(View.GONE);
            solutionFragmentView.findViewById(R.id.no_result).setVisibility(View.VISIBLE);

            return;
        }

        Bundle watsonState = null;
        if (savedInstanceState != null) {
            currentBarButton = savedInstanceState.getInt(CURRENT_BAR_BUTTON);

            watsonState = savedInstanceState.getBundle(SAVE_WATSON_PAGE);
            watsonHasBackOption = savedInstanceState.getBoolean(SAVE_WATSON_HAS_BACK_OPTION, false);
        } else {
            speeches = new String[solutions.size()];
            htmls = new String[solutions.size()];

            if (solutionPosition > -1) {
                speeches[solutionPosition] = getActivity().getIntent().getStringExtra(SPEECH_EXTRA);
                htmls[solutionPosition] = getActivity().getIntent().getStringExtra(HTML_EXTRA);
            }
        }

        if (hasVoiceAccess && solutions.size() > 0) {
            if (showingResults && solutionPosition == -1) {
                if (solutions.size() > 1) {
                    adapter = new SimpleSolutionsAdapter(getActivity(), solutions);

                    solutionsListView.setAdapter(adapter);
                    adapter.setOnSolutionItemClickListener(this);
                    adapter.setRecyclerView(solutionsListView);
                    solutionsListView.setVisibility(View.VISIBLE);
                    solutionWebView.setVisibility(View.GONE);
                    toolbarRelativeLayout.setVisibility(View.GONE);
                    playPanelRelativeLayout.setVisibility(View.GONE);
                    bottomBarRelativeLayout.setVisibility(View.GONE);
                } else {
                    solutionPosition = 1;
                }
            }

            if (solutionPosition > -1) {
                currentSolutionId = solutions.get(solutionPosition).solutionId();

                setupSolutionWebView();
                setupNavigation();

                checkIsFavorite();
            }
        }

        if (hasWatsonAccess) {
            watsonWebView.setVerticalScrollBarEnabled(true);
            watsonWebView.setHorizontalScrollBarEnabled(true);
            watsonWebView.requestFocusFromTouch();
            watsonWebView.getSettings().setJavaScriptEnabled(true);
            watsonWebView.getSettings().setSupportZoom(true);
            watsonWebView.getSettings().setBuiltInZoomControls(true);
            watsonWebView.getSettings().setDisplayZoomControls(false);
            watsonWebView.getSettings().setLoadWithOverviewMode(true);
            watsonWebView.getSettings().setUseWideViewPort(true);
            watsonWebView.setWebChromeClient(new WebChromeClient());
            watsonWebView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (url.startsWith("data")) {
                        watsonHasBackOption = false;

                        solutionBackButtonRelativeLayout.setVisibility(View.GONE);

                        return false;
                    }

                    watsonHasBackOption = true;

                    RotateAnimation animation = new RotateAnimation(0, 360,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setFillAfter(true);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.setDuration(2000);
                    animation.setRepeatCount(Animation.INFINITE);

                    watsonLoadingTextView.setTypeface(fonts.fontello());
                    watsonLoadingTextView.setVisibility(View.VISIBLE);
                    watsonLoadingTextView.setAnimation(animation);
                    watsonLoadingTextView.getAnimation().start();

                    solutionBackButtonRelativeLayout.setVisibility(View.VISIBLE);

                    TextView backText = (TextView) solutionFragmentView.findViewById(R.id.solution_back);
                    backText.setText(R.string.go_back_responses);

                    view.loadUrl(url);

                    return true;
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (url.startsWith("data")) {
                        watsonHasBackOption = false;

                        if (currentBarButton == 1) {
                            solutionFragmentView.findViewById(
                                    R.id.solution_back_button).setVisibility(
                                    View.GONE);
                        }

                        return;
                    }

                    watsonHasBackOption = true;

                    solutionBackButtonRelativeLayout.setVisibility(View.VISIBLE);

                    TextView backText = (TextView) solutionFragmentView.findViewById(R.id.solution_back);
                    backText.setText(R.string.go_back_responses);

                    if (watsonLoadingTextView.getAnimation() != null) {
                        watsonLoadingTextView.getAnimation().cancel();
                    }

                    watsonLoadingTextView.clearAnimation();
                    watsonLoadingTextView.setVisibility(View.GONE);
                }
            });

            if (watsonState == null) {
                watsonWebView.loadData(ResultsActivity.getWatsonHtml(), mime, encoding);
            } else {
                watsonWebView.restoreState(watsonState);

                if (watsonHasBackOption && currentBarButton == 1) {
                    RotateAnimation animation = new RotateAnimation(0, 360,
                            Animation.RELATIVE_TO_SELF, 0.5f,
                            Animation.RELATIVE_TO_SELF, 0.5f);
                    animation.setFillAfter(true);
                    animation.setInterpolator(new LinearInterpolator());
                    animation.setDuration(2000);
                    animation.setRepeatCount(Animation.INFINITE);

                    watsonLoadingTextView.setVisibility(View.VISIBLE);
                    watsonLoadingTextView.setAnimation(animation);
                    watsonLoadingTextView.getAnimation().start();

                    solutionBackButtonRelativeLayout.setVisibility(View.VISIBLE);

                    TextView backText = (TextView) solutionFragmentView.findViewById(R.id.solution_back);
                    backText.setText(R.string.go_back_responses);
                }
            }

            if (adapter != null) {
                solutionsListView.setAdapter(adapter);
                adapter.setOnSolutionItemClickListener(this);
                solutionsListView.setVisibility(View.VISIBLE);
            }

            if (!hasVoiceAccess) {
                solutionWebView.setVisibility(View.GONE);
                toolbarRelativeLayout.setVisibility(View.GONE);
                playPanelRelativeLayout.setVisibility(View.GONE);
                solutionBackButtonRelativeLayout.setVisibility(View.GONE);
                solutionForwardButtonRelativeLayout.setVisibility(View.GONE);
                bottomBarRelativeLayout.setVisibility(View.VISIBLE);
                watsonWebView.setVisibility(View.VISIBLE);

                currentBarButton = 1;
            }
        }

        if (hasVoiceAccess && hasWatsonAccess) {
            solutionFragmentView.findViewById(R.id.watson_action_bar_tabs).setVisibility(View.VISIBLE);
            solutionFragmentView.findViewById(R.id.content_title).setVisibility(View.INVISIBLE);

            setupButtonBar();

        }

        if (currentBarButton == 0) {
            currentBarButton = 1;
            showSolutions();
        } else {
            currentBarButton = 0;
            showWatson();
        }

    }

    private void setupPushNotification() {
        Intent intent = getActivity().getIntent();

        if (intent.getExtras().get(GcmBroadcastReceiver.EXTRA_SOLUTION_ID) instanceof String) {
            String solutionIdString = intent.getStringExtra(GcmBroadcastReceiver.EXTRA_SOLUTION_ID);
            currentSolutionId = Integer.parseInt(solutionIdString);
        } else {
            currentSolutionId = intent.getIntExtra(GcmBroadcastReceiver.EXTRA_SOLUTION_ID, -1);
        }

        accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_PUSH_NOTIFICATION;
        isVoiceHistory = false;

        if (currentSolutionId == -1) {
            NotificationHelper.showNotification(TalkersConstants.SOLUTION_INVALID, (SuperActivity) getActivity());

            return;
        }

        if (setAutomaticClosed) {
            setAutomaticFrameLayout.setVisibility(View.GONE);
        } else {
            setAutomaticFrameLayout.setVisibility(View.VISIBLE);
        }

        solutionPosition = 0;

        if (htmls != null && htmls[solutionPosition] != null && !htmls[solutionPosition].isEmpty()) {
            setupSolutionWebView();

            solutions = getActivity().getIntent().getParcelableArrayListExtra(SOLUTIONS_EXTRA_FRAGMENT);
            solutionPosition = getActivity().getIntent().getIntExtra(POSITION_EXTRA, 0);

            setupNavigation();

            checkIsFavorite();

            return;
        }

        Locker.lock(getActivity());

        loadingDialog.show();

        APITalker.sharedTalker().getSolution(
                User.sharedUser(getActivity()).hash(),
                currentSolutionId,
                APITalker.CALL_TYPES.CALL_TYPE_PUSH_NOTIFICATION, this);
    }

    private void setupSchemeSolution() {
        String url = getActivity().getIntent().getDataString();
        Pattern pattern = Pattern.compile("[0-9]+");
        Matcher matcher = pattern.matcher(url);

        String solutionIdString = "";

        if (matcher.find()) {
            solutionIdString = matcher.group();
        }

        int solutionId = -1;

        try {
            solutionId = Integer.parseInt(solutionIdString);
        } catch (NumberFormatException e) {
        } catch (NullPointerException e) {
        }

        currentSolutionId = solutionId;
        accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_DEEPLINKSOLUTION;
        isVoiceHistory = false;
        solutionPosition = 0;

        if (currentSolutionId == -1) {
            NotificationHelper.showNotification(TalkersConstants.SOLUTION_INVALID,
                    (SuperActivity) getActivity());

            return;
        }

        if (setAutomaticClosed) {
            setAutomaticFrameLayout.setVisibility(View.GONE);
        } else {
            setAutomaticFrameLayout.setVisibility(View.VISIBLE);
        }

        if (htmls != null && htmls[solutionPosition] != null && !htmls[solutionPosition].isEmpty()) {
            setupSolutionWebView();

            solutions = getActivity().getIntent().getParcelableArrayListExtra(SOLUTIONS_EXTRA_FRAGMENT);
            solutionPosition = getActivity().getIntent().getIntExtra(POSITION_EXTRA, 0);

            setupNavigation();

            checkIsFavorite();

            return;
        }

        Locker.lock(getActivity());

        loadingDialog.show();

        APITalker.sharedTalker().getSolution(
                User.sharedUser(getActivity()).hash(), currentSolutionId,
                APITalker.CALL_TYPES.CALL_TYPE_DEEP_LINK, this);
    }

    /**
     * OnItemClickListener Methods
     */

    @Override
    public void onSolutionItemClick(Solution solution, int position) {
        solutionPosition = position;

        Locker.lock(getActivity());

        loadingDialog.show();

        APITalker.sharedTalker().getSolution(
                User.sharedUser(getActivity()).hash(),
                solution.solutionId(),
                CALL_TYPES.CALL_TYPE_OPTIQUERY, this);
    }

    /**
     * Refresh Data Methods
     */

    public void refreshData() {
        if (solutionPosition >= 0) {
            checkIsFavorite();
        }
    }

    public void reloadContent() {
        if (getActivity().getIntent().getDataString() != null
                && !getActivity().getIntent().getDataString().isEmpty()) {
            setupSchemeSolution();
        } else if (getActivity().getIntent().getAction() != null
                && getActivity().getIntent().getAction().equals(GcmBroadcastReceiver.NOTIFICATION_ACTION)) {
            setupPushNotification();
        }

        refreshData();
    }

    /**
     * Broadcast Receivers
     */

    private BroadcastReceiver newSolutionDataReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            refreshData();
        }

    };

    /**
     * Connection Change Methods
     */

    public void onConnectionUpdate(boolean isConnected) {
        if (isConnected && (solutionPosition < 0 || solutions == null
                || solutions.isEmpty() || solutions.get(solutionPosition).title().isEmpty())) {
            if (getActivity().getIntent().getDataString() != null
                    && !getActivity().getIntent().getDataString().isEmpty()) {
                setupSchemeSolution();
            } else if (getActivity().getIntent().getAction() != null
                    && getActivity().getIntent().getAction().equals(GcmBroadcastReceiver.NOTIFICATION_ACTION)) {
                setupPushNotification();
            }
        }
    }

    /**
     * Listener Interface
     */

    public interface SolutionFragmentListener {
        void checkMarkClicked(ArrayList<Solution> addedFavorites, ArrayList<Solution> removedFavorites);
        void backClicked(ArrayList<Solution> addedFavorites, ArrayList<Solution> removedFavorites);
    }

    public void setFragmentListener(SolutionFragmentListener listener) {
        this.listener = listener;
    }
}