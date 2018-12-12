package com.implementhit.OptimizeHIT.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.SuggestedLearningAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.SolutionRequestListener;
import com.implementhit.OptimizeHIT.api.SuggestedLearningRequestListener;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.ColorUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class SuggestedSolutionsActivity extends SuperActivity implements
            View.OnClickListener, SolutionRequestListener,
            SuggestedLearningRequestListener, OnSolutionItemClickListener {

    private SuggestedLearningAdapter adapter;
    private int currentButton = 1;

    private ArrayList<Solution> allSuggestions;
    private ArrayList<Solution> locationBased;

    private LoadingDialog refresgingDialog;
    private LoadingDialog solutionLoading;

    private int allSuggestionsListState;
    private int locationSuggestionsListState;

    private RecyclerView suggestionsList;
    private LinearLayoutManager layoutManager;

    private String currentAccessMethod = "";
    private int solutionPosition;
    private ArrayList<Solution> currentSolutions;
    private ArrayList<Solution> reactivations;
    private ArrayList<Solution> suggestions;
    private ArrayList<Solution> locationBasedDemo;
    private int currentSolutionType;
    private boolean noReactivations = false;
    private AtomicBoolean listItemClicked;

    private Button rightButton;

    private final static String  SUGGESTED_SOLUTIONS_FRAGMENT = "suggestedSolutionsFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_learning);

        OptimizeHIT.sendScreen(GAnalyticsScreenNames.SUGGESTED_SOLUTIONS_SCREEN,
                null, null, null);

        allSuggestions = new ArrayList<>();
        locationBased = new ArrayList<>();

        adapter = new SuggestedLearningAdapter(this, new ArrayList<Solution>());

        layoutManager = new LinearLayoutManager(this);

        suggestionsList = (RecyclerView) findViewById(R.id.suggestions_list);
        suggestionsList.setAdapter(adapter);
        suggestionsList.setLayoutManager(layoutManager);
        adapter.setOnSolutionItemClickListener(this);
        adapter.setRecyclerView(suggestionsList);

        Button barButtonLeft = (Button) findViewById(R.id.bar_button_left);
        Button barButtonRight = (Button) findViewById(R.id.bar_button_right);
        barButtonLeft.setText(R.string.suggestions_all);
        barButtonRight.setText(R.string.suggestions_location_based);
        barButtonLeft.setOnClickListener(this);
        barButtonRight.setOnClickListener(this);

        refresgingDialog = new LoadingDialog(this);
        solutionLoading = new LoadingDialog(this);

        listItemClicked = new AtomicBoolean();

        Typeface fontello = FontsHelper.sharedHelper(this).fontello();

        TextView titleTextView = (TextView) findViewById(R.id.content_title);
        titleTextView.setText(R.string.suggested_solutions);

        LinearLayout leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
        leftButtonLayout.setVisibility(View.VISIBLE);


        Button leftButton = (Button) findViewById(R.id.left_button);
        leftButton.setText(R.string.icon_angle_left);
        leftButton.setTypeface(fontello);
        leftButton.setOnClickListener(this);

        rightButton = (Button) findViewById(R.id.right_button);
        rightButton.setText(R.string.icon_arrows_cw);
        rightButton.setTypeface(fontello);
        rightButton.setOnClickListener(this);
        rightButton.setVisibility(View.VISIBLE);
        rightButton.setOnClickListener(this);
    }

	/*
     * Tab Navigation helpers
	 */

    @Override
    public void onResume() {
        super.onResume();

        reloadSuggestedLearning();
    }

    public void reloadSuggestedLearning() {
        ArrayList<Solution> reactivations = DBTalker.sharedDB(this).getSuggestedSolutions(APITalker.SUGGESTION_TYPES.REACTIVATION);
        ArrayList<Solution> suggestions = DBTalker.sharedDB(this).getSuggestedSolutions(APITalker.SUGGESTION_TYPES.SUGGESTION);
        ArrayList<Solution> locationBasedDemo = DBTalker.sharedDB(this).getSuggestedSolutions(APITalker.SUGGESTION_TYPES.LOCATION_BASED);

        setupSuggestions(reactivations, suggestions, locationBasedDemo);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold, R.anim.slide_right_out);
    }

    /*
	 * OnItemClickListener Methods
	 */

    private void leftBarButtonClicked() {
        if (currentButton == 1) {
            return;
        }

        int primaryColor = User.sharedUser(this).primaryColor();

        Button barButtonLeft = (Button) findViewById(R.id.bar_button_left);
        Button barButtonRight = (Button) findViewById(R.id.bar_button_right);
        barButtonLeft.setTextColor(getResources().getColor(R.color.background_light_grey));
        barButtonRight.setTextColor(User.sharedUser(this).primaryColor());
//        barButtonLeft.setBackgroundResource(R.drawable.results_bar_left_active_orange);
//        barButtonRight.setBackgroundResource(R.drawable.results_bar_right_inactive_grey);
        barButtonLeft.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.results_bar_left_active_orange, primaryColor));
        barButtonRight.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.results_bar_right_inactive_grey, primaryColor));

        locationSuggestionsListState = layoutManager.findFirstVisibleItemPosition();

        adapter.setSolutions(allSuggestions);

        suggestionsList.scrollToPosition(0);

        currentButton = 1;
    }

    private void rightBarButtonClicked() {

        if (currentButton == 2) {
            return;
        }

        int primaryColor = User.sharedUser(this).primaryColor();

        Button barButtonLeft = (Button) findViewById(R.id.bar_button_left);
        Button barButtonRight = (Button) findViewById(R.id.bar_button_right);
        barButtonRight.setTextColor(getResources().getColor(R.color.background_light_grey));
        barButtonLeft.setTextColor(primaryColor);
        barButtonRight.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.results_bar_right_active_orange, primaryColor));
        barButtonLeft.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.results_bar_left_inactive_grey, primaryColor));

        allSuggestionsListState = layoutManager.findFirstVisibleItemPosition();

        adapter.setSolutions(locationBased);

        suggestionsList.scrollToPosition(0);

        currentButton = 2;
    }

    @Override
    public void onSolutionItemClick(Solution solution, int position) {
        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        if (listItemClicked.get()) {
            return;
        }

        listItemClicked.set(true);

        if (currentButton == 1) {
            Locker.lock(this);

            solutionLoading.show();

            String callType = solution.type() == 1 ? APITalker.CALL_TYPES.CALL_TYPE_REACTIVATION
                    : APITalker.CALL_TYPES.CALL_TYPE_SUGGESTION;
            currentAccessMethod = solution.type() == 1 ? APITalker.ACCESS_METHODS.ACCESS_METHOD_REACTIVATION
                    : APITalker.ACCESS_METHODS.ACCESS_METHOD_SUGGESTION;
            currentSolutionType = solution.type();

            if (solution.type() == 1) {
                solutionPosition = position - 1;
                currentSolutions = reactivations;
            } else {
                currentSolutions = suggestions;

                if (noReactivations) {
                    solutionPosition = position - 1;
                } else {
                    solutionPosition = position - 2 - reactivations.size();
                }
            }

            APITalker.sharedTalker().getSolution(
                    User.sharedUser(this).hash(),
                    solution.solutionId(), callType, this);
        } else if (currentButton == 2) {
            Locker.lock(this);

            solutionLoading.show();

            currentAccessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_LOCATION;
            currentSolutions = locationBasedDemo;
            currentSolutionType = solution.type();

            solutionPosition = position - 1;

            APITalker.sharedTalker().getSolution(
                    User.sharedUser(this).hash(),
                    solution.solutionId(),
                    APITalker.CALL_TYPES.CALL_TYPE_LOCATION, this);
        }
    }

    /**
     * OnClickListener Methods
     */

    @Override
    public void onClick(View view) {

        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        if (view.getId() == R.id.bar_button_left) {
            leftBarButtonClicked();
        } else if (view.getId() == R.id.bar_button_right) {
            rightBarButtonClicked();
        } else if (view.getId() == R.id.right_button) {

            Animation animationRotateCenter = AnimationUtils.loadAnimation(
                    SuggestedSolutionsActivity.this, R.anim.rotated_load);
            animationRotateCenter.setFillAfter(true);
            animationRotateCenter.setInterpolator(new LinearInterpolator());
            animationRotateCenter.setRepeatCount(Animation.INFINITE);

            rightButton.startAnimation(animationRotateCenter);

            APITalker.sharedTalker().getSuggestedLearning(
                            User.sharedUser(this).hash(),
                            this,
                            this
                    );

        } else if (view.getId() == R.id.left_button) {
            onBackPressed();
        }

    }

    /**
     * Refresh Suggestions Helper
     */

    @Override
    public void solutionSuccess(int solutionId, String title,
                                String html, String speech) {
        DBTalker.sharedDB(this).saveHistory(solutionId, title, false);

        ActivityOptions options = ActivityOptions.makeCustomAnimation(this, R.anim.slide_left_in, R.anim.hold);
        Intent solutionIntent = new Intent(this, SolutionActivity.class);
        solutionIntent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, currentAccessMethod);
        solutionIntent.putParcelableArrayListExtra(SolutionActivity.EXTRA_SOLUTIONS, currentSolutions);
        solutionIntent.putExtra(SolutionActivity.EXTRA_POSITION, solutionPosition);
        solutionIntent.putExtra(SolutionActivity.EXTRA_HTML, html);
        solutionIntent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);

        startActivityForResult(solutionIntent, SolutionActivity.REQUEST_CODE,
                options.toBundle());

        APITalker.sharedTalker().markViewed(
                User.sharedUser(this).hash(),
                solutionId,
                currentSolutionType, null);

        DBTalker.sharedDB(this).markReactivationViewed(solutionId);

        if (currentAccessMethod.equals(APITalker.ACCESS_METHODS.ACCESS_METHOD_REACTIVATION)) {
            Solution solution = reactivations.get(solutionPosition);

            if (!solution.viewed()) {
                solution.setViewed(true);
                adapter.notifyDataSetChanged();

                User user = User.sharedUser(this);
                int reactivations = user.reactivations();
                reactivations--;
                user.setReactivations(reactivations);
            }
        }

        solutionLoading.dismiss();

        Locker.unlock(this);

        listItemClicked.set(false);
    }

    @Override
    public void solutionFailure(String error) {
        NotificationHelper.showNotification(error, (SuperActivity) this);

        solutionLoading.dismiss();

        listItemClicked.set(false);

        Locker.unlock(this);
    }

    public void setupSuggestions(ArrayList<Solution> reactivations,
                                 ArrayList<Solution> suggestions,
                                 ArrayList<Solution> locationBasedDemo) {
        this.reactivations = reactivations;
        this.suggestions = suggestions;
        this.locationBasedDemo = locationBasedDemo;

        allSuggestions.clear();

        if (reactivations.size() == 0) {
            noReactivations = true;
//			allSuggestions.add(new Solution(-1, SolutionsListAdapter.EMPTY_SUGGESTIONS_HEADER));
        } else {
            allSuggestions.add(new Solution(-1, SuggestedLearningAdapter.REACTIOVATION_HEADER));
            allSuggestions.addAll(reactivations);
        }

        allSuggestions.add(new Solution(-1, SuggestedLearningAdapter.SUGGESTIONS_HEADER));

        if (suggestions.size() == 0) {
            allSuggestions.add(new Solution(-1, SuggestedLearningAdapter.EMPTY_SUGGESTIONS_HEADER));
        } else {
            allSuggestions.addAll(suggestions);
        }

        locationBased.clear();
        locationBased.add( new Solution(-1, SuggestedLearningAdapter.LOCATION_BASED_HEADER) );

        if (locationBasedDemo.size() == 0) {
            locationBased.add(new Solution(-1, SuggestedLearningAdapter.EMPTY_SUGGESTIONS_HEADER));
        } else {
            locationBased.addAll(locationBasedDemo);
        }

        if (currentButton == 1) {
            currentButton = 2;
            leftBarButtonClicked();
        } else if (currentButton == 2) {
            currentButton = 1;
            rightBarButtonClicked();
        }
    }

    /**
     * SuggestedLearningHandler Methods
     */

    @Override
    public void onSuggestedLearningSuccess(ArrayList<Solution> reactivations,
                                           ArrayList<Solution> suggestions, ArrayList<Solution> locationBased,
                                           int viewedCount) {
        reloadSuggestedLearning();
        refresgingDialog.dismiss();
        rightButton.clearAnimation();
        Locker.unlock(this);
    }

    @Override
    public void onSuggestedLearningFail(String error) {
        refresgingDialog.dismiss();
        NotificationHelper.showNotification(error, this);
        rightButton.clearAnimation();
        Locker.unlock(this);
    }

    @Override
    protected void refreshStateAfterLogin(boolean isInitialLogin) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (currentButton == 2) {
            outState.putBoolean(SUGGESTED_SOLUTIONS_FRAGMENT, true);
        } else {
            outState.putBoolean(SUGGESTED_SOLUTIONS_FRAGMENT, false);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        if (savedInstanceState.getBoolean(SUGGESTED_SOLUTIONS_FRAGMENT)) {
            currentButton = 2;
        } else {
            currentButton = 1;
        }


    }

}
