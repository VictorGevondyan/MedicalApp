package com.implementhit.OptimizeHIT.activity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.FavoriteSolutionsAdapter;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by victor on 1/26/17.
 */

public class FavoriteSolutionsActivity extends SuperActivity implements
        View.OnClickListener, SolutionRequestListener, OnSolutionItemClickListener {

    private FavoriteSolutionsAdapter adapter;
    private int currentButton = 1;

    private ArrayList<Solution> yourFavoriteSolutions;
    private ArrayList<Solution> peerFavoriteSolutions;

    private LoadingDialog solutionLoading;

    private RecyclerView favoritesList;
    private LinearLayoutManager layoutManager;

    private String currentAccessMethod = "";

    private int solutionPosition;
    private AtomicBoolean listItemClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_solutions);

        OptimizeHIT.sendScreen(GAnalyticsScreenNames.FAVORITES_SCREEN,
                null, null, null);

        yourFavoriteSolutions = new ArrayList<>();
        peerFavoriteSolutions = new ArrayList<>();

        adapter = new FavoriteSolutionsAdapter(this, new ArrayList<Solution>());

        layoutManager = new LinearLayoutManager(this);

        favoritesList = (RecyclerView) findViewById(R.id.suggestions_list);
        favoritesList.setAdapter(adapter);
        favoritesList.setLayoutManager(layoutManager);

        adapter.setRecyclerView(favoritesList);
        adapter.setOnSolutionItemClickListener(this);

        Button barButtonLeft = (Button) findViewById(R.id.bar_button_left);
        Button barButtonRight = (Button) findViewById(R.id.bar_button_right);
        barButtonLeft.setText(R.string.your_favorites);
        barButtonRight.setText(R.string.peer_favorites);
        barButtonLeft.setOnClickListener(this);
        barButtonRight.setOnClickListener(this);

        solutionLoading = new LoadingDialog(this);

        listItemClicked = new AtomicBoolean();

        Typeface fontello = FontsHelper.sharedHelper(this).fontello();

        TextView titleTextView = (TextView) findViewById(R.id.content_title);
        titleTextView.setText(R.string.favorite_solutions);

        LinearLayout leftButtonLayout = (LinearLayout) findViewById(R.id.left_button_layout);
        leftButtonLayout.setVisibility(View.VISIBLE);


        Button leftButton = (Button) findViewById(R.id.left_button);
        leftButton.setText(R.string.icon_angle_left);
        leftButton.setTypeface(fontello);
        leftButton.setOnClickListener(this);
    }

	/*
     * Tab Navigation helpers
	 */

    @Override
    public void onResume() {
        super.onResume();

        setupFavoriteSolutions();
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
        barButtonRight.setTextColor(primaryColor);
        barButtonLeft.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.results_bar_left_active_orange, primaryColor));
        barButtonRight.setBackground(ColorUtil.getTintedDrawable(this, R.drawable.results_bar_right_inactive_grey, primaryColor));

        adapter.setSolutions(yourFavoriteSolutions);

        favoritesList.scrollToPosition(0);

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

        adapter.setSolutions(peerFavoriteSolutions);

        favoritesList.scrollToPosition(0);

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

        solutionPosition = position;

        String callType = APITalker.CALL_TYPES.CALL_TYPE_FAVORITES;

        if ( currentButton == 1 ) {

            Locker.lock(this);

            solutionLoading.show();

            currentAccessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_FAVORITES;

            APITalker.sharedTalker().getSolution(
                    User.sharedUser(this).hash(),
                    solution.solutionId(), callType, this);

        } else if ( currentButton == 2 ) {
            Locker.lock(this);

            solutionLoading.show();

            currentAccessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_LOCATION;

            APITalker.sharedTalker().getSolution(
                    User.sharedUser(this).hash(),
                    solution.solutionId(),
                    callType, this);
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
        solutionIntent.putParcelableArrayListExtra( SolutionActivity.EXTRA_SOLUTIONS, adapter.getSolutions() );
        solutionIntent.putExtra(SolutionActivity.EXTRA_POSITION, solutionPosition);
        solutionIntent.putExtra(SolutionActivity.EXTRA_HTML, html);
        solutionIntent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);

        startActivityForResult(solutionIntent, SolutionActivity.REQUEST_CODE,
                options.toBundle());

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

    public void setupFavoriteSolutions() {

        yourFavoriteSolutions.clear();
        peerFavoriteSolutions.clear();


        yourFavoriteSolutions = DBTalker.sharedDB(this).getFavorites();
        peerFavoriteSolutions = DBTalker.sharedDB(this).getPeerFavorites();


//        if (reactivations.size() == 0) {
//            noReactivations = true;
////			yourFavoriteSolutions.add(new Solution(-1, SolutionsListAdapter.EMPTY_SUGGESTIONS_HEADER));
//        } else {
//            yourFavoriteSolutions.add(new Solution(-1, SuggestedLearningAdapter.REACTIOVATION_HEADER));
//            yourFavoriteSolutions.addAll(reactivations);
//        }
//
//        yourFavoriteSolutions.add(new Solution(-1, SuggestedLearningAdapter.SUGGESTIONS_HEADER));
//
//        if (suggestions.size() == 0) {
//            yourFavoriteSolutions.add(new Solution(-1, SuggestedLearningAdapter.EMPTY_SUGGESTIONS_HEADER));
//        } else {
//            yourFavoriteSolutions.addAll(suggestions);
//        }
//
//        peerFavoriteSolutions.clear();
//        peerFavoriteSolutions.add(new Solution(-1, SuggestedLearningAdapter.LOCATION_BASED_HEADER));
//
//        if (locationBasedDemo.size() == 0) {
//            peerFavoriteSolutions.add(new Solution(-1, SuggestedLearningAdapter.EMPTY_SUGGESTIONS_HEADER));
//        } else {
//            peerFavoriteSolutions.addAll(locationBasedDemo);
//        }

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
    protected void refreshStateAfterLogin(boolean isInitialLogin) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
//
//        if (currentButton == 2) {
//            outState.putBoolean(SUGGESTED_SOLUTIONS_FRAGMENT, true);
//        } else {
//            outState.putBoolean(SUGGESTED_SOLUTIONS_FRAGMENT, false);
//        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

//        if (savedInstanceState.getBoolean(SUGGESTED_SOLUTIONS_FRAGMENT)) {
//            currentButton = 2;
//        } else {
//            currentButton = 1;
//        }
//
    }

}


















