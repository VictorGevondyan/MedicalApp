package com.implementhit.OptimizeHIT.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.EditableSolutionsAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.viewholders.OnRemoveSolutionListener;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.SolutionRequestListener;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.dialogs.LoadingDialog;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.Locker;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import java.util.ArrayList;

public class EditableSolutionsListActivity extends SuperActivity implements OnSolutionItemClickListener, View.OnClickListener, SolutionRequestListener, OnRemoveSolutionListener {
    public static final String EXTRA_SOLUTIONS_TYPE = "solutionType";
    public static final String TYPE_LIKES = "likes";
    public static final String TYPE_FAVORITES = "favorites";

    private final String SAVED_SOLUTIONS = "savedSolutions";

    private ArrayList<Solution> solutions;

    private LoadingDialog loadingDialog;
    private EditableSolutionsAdapter adapter;

    private String type;
    private int solutionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editable_solutions_list);

        Intent incomingIntent = getIntent();
        type = incomingIntent.getStringExtra(EXTRA_SOLUTIONS_TYPE);

        solutions = new ArrayList<>();

        loadingDialog = new LoadingDialog(this);

        View actionBarView = findViewById(R.id.action_bar);
        LinearLayout leftButtonLayout = (LinearLayout) actionBarView.findViewById(R.id.left_button_layout);
        Button leftButton = ( Button ) actionBarView.findViewById(R.id.left_button);
//        Button rightButton = ( Button ) actionBarView.findViewById(R.id.right_button);
        TextView contentTitleTextView = ( TextView )actionBarView.findViewById(R.id.content_title);

        Typeface fontelloTypeface = FontsHelper.sharedHelper(this).fontello();

        leftButtonLayout.setVisibility(View.VISIBLE);

        leftButton.setText(R.string.icon_angle_left);
        leftButton.setTypeface(fontelloTypeface);
        leftButton.setOnClickListener(this);

//        rightButton.setVisibility(View.VISIBLE);
//        rightButton.setText(R.string.edit);
//        rightButton.setTextSize(15);
//        rightButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.solutions_list);

        adapter = new EditableSolutionsAdapter(this, solutions);

        adapter.setRecyclerView(recyclerView);
        adapter.setOnSolutionItemClickListener(this);
        adapter.setOnRemoveSolutionListener(this);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (savedInstanceState != null) {
            solutions = savedInstanceState.getParcelableArrayList(SAVED_SOLUTIONS);

            adapter.restoreState(savedInstanceState);

//            setupRightButton();

        }

//        if (type.equals(TYPE_FAVORITES)) {
//
//            contentTitleTextView.setText(R.string.your_favorites);
//
//            OptimizeHIT.sendScreen(GAnalyticsScreenNames.YOUR_FAVORITES_SCREEN, null, null, null);
//
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(SAVED_SOLUTIONS, adapter.getSolutions());

        adapter.saveState(outState);
    }

    @Override
    protected void onResume() {
        reloadContent();

        IntentFilter updateSolutionsFilter = new IntentFilter(APITalker.ACTION_NEW_SOLUTION_DATA);
        registerReceiver(solutionsUpdateReceiver, updateSolutionsFilter);
        IntentFilter updateFavoritesFilter = new IntentFilter(APITalker.ACTION_UPDATE_HISTORY);
        registerReceiver(favoritesUpdateReceiver, updateFavoritesFilter);

        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(solutionsUpdateReceiver);
        unregisterReceiver(favoritesUpdateReceiver);
    }

    @Override
    protected void refreshStateAfterLogin(boolean isInitialLogin) {

    }

    @Override
    public void finish() {
        super.finish();

        overridePendingTransition(R.anim.hold,R.anim.slide_right_out);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.left_button) {
 //            if (adapter.isCurrentlyEditing()) {
//                changeEditMode();
//            } else {
                onBackPressed();
//            }
        } else if (view.getId() == R.id.right_button) {
            changeEditMode();
        }
    }

    @Override
    public void onSolutionItemClick(Solution solution, int solutionPosition) {
        DBTalker
                .sharedDB(this)
                .saveHistory(solution.solutionId(), solution.title(), false);

        this.solutionPosition = solutionPosition;

        String callType = type.equals(TYPE_LIKES)
                ? APITalker.CALL_TYPES.CALL_TYPE_LIKES
                : APITalker.CALL_TYPES.CALL_TYPE_FAVORITES;


        loadingDialog.show();
        Locker.lock(this);
        APITalker
                .sharedTalker()
                .getSolution(
                        User.sharedUser(this).hash(),
                        solution.solutionId(),
                        callType,
                        this
                );
    }

    @Override
    public void onRemoveSolution(Solution solution) {
        String likeType;

        if (type.equals(TYPE_FAVORITES)) {
            likeType = APITalker.LIKE_TYPES.LIKE_TYPE_FAVORITE;
        } else {
            likeType = APITalker.LIKE_TYPES.LIKE_TYPE_LIKE;
        }

        APITalker
                .sharedTalker()
                .removeLike(
                        User.sharedUser(this).hash(),
                        likeType,
                        solution.solutionId(),
                        null
                );
    }

    @Override
    public void solutionSuccess(int solutionId, String title, String html, String speech) {
        String accessMethod = type.equals(TYPE_LIKES)
                ? APITalker.ACCESS_METHODS.ACCESS_METHOD_LIKES
                : APITalker.ACCESS_METHODS.ACCESS_METHOD_FAVORITES;

        loadingDialog.hide();

        Intent intent = new Intent(this, SolutionActivity.class);
        intent.putExtra(SolutionActivity.EXTRA_HTML, html);
        intent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);
        intent.putExtra(SolutionActivity.EXTRA_SOLUTIONS, adapter.getSolutions());
        intent.putExtra(SolutionActivity.EXTRA_POSITION, solutionPosition);
        intent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, accessMethod);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_left_in, R.anim.hold);

        Locker.unlock(this);
    }

    @Override
    public void solutionFailure(String error) {

        loadingDialog.hide();

        NotificationHelper.showNotification(error, this);

        Locker.unlock(this);

    }

    private void changeEditMode() {
        adapter.changeEditingMode();

//        setupRightButton();
    }

//    private void setupRightButton(){
//        Button rightButton = ( Button ) findViewById(R.id.right_button);
//
//        if (adapter.isCurrentlyEditing()) {
//            rightButton.setText(R.string.done);
//        } else {
//            rightButton.setText(R.string.edit);
//        }
//    }

    private void reloadContent() {

        if (type.equals(TYPE_FAVORITES)) {
            solutions = DBTalker.sharedDB(this).getFavorites();
        }

        adapter.setSolutions(solutions);

        adapter.notifyDataSetChanged();

    }

    BroadcastReceiver solutionsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadContent();
        }
    };

    BroadcastReceiver favoritesUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadContent();
        }
    };

}