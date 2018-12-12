package com.implementhit.OptimizeHIT.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.SimpleSolutionsAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
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

public class SimpleSolutionsListActivity extends SuperActivity implements  OnSolutionItemClickListener,
        View.OnClickListener, SolutionRequestListener {
    public static final String EXTRA_SOLUTIONS_TYPE = "solutionType";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_SUBCATEGORY = "subCategory";

    public static final String TYPE_PEER_FAVORITES = "peerFavorites";
    public static final String TYPE_HISTORY = "history";
    public static final String TYPE_SUBCATEGORIES = "subcategories";
    public static final String TYPE_SOLUTIONS = "solutions";

    private static final String SAVED_SOLUTIONS = "savedSolutions";

    private ArrayList<Solution> solutions;
    private LoadingDialog loadingDialog;
    private SimpleSolutionsAdapter adapter;
    private String type;
    private int solutionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_solutions_list);

        Intent incomingIntent = getIntent();
        type = incomingIntent.getStringExtra(EXTRA_SOLUTIONS_TYPE);

        Solution category = incomingIntent.getParcelableExtra(EXTRA_CATEGORY);

        ArrayList<Solution> solutions = new ArrayList<>();

        LinearLayout header = (LinearLayout) findViewById(R.id.header);
        TextView headerTitle = (TextView) header.findViewById(R.id.section_title);

        if (type.equals(TYPE_SUBCATEGORIES)) {

            headerTitle.setText(getResources().getString(R.string.subcategories_for, category.title()));

            OptimizeHIT.sendScreen(GAnalyticsScreenNames.SUBCATEGORIES_SCREEN, null, null, null);

        } else if (type.equals(TYPE_SOLUTIONS)) {

            headerTitle.setText(getResources().getString(R.string.solutions_for_category, category.title()));

            OptimizeHIT.sendScreen(GAnalyticsScreenNames.SOLUTIONS_SCREEN, null, null, null);

        } else {
            header.setVisibility(View.GONE);
        }

        View actionBarView = findViewById(R.id.action_bar);

        LinearLayout leftButtonLayout = (LinearLayout) actionBarView.findViewById(R.id.left_button_layout);
        leftButtonLayout.setVisibility(View.VISIBLE);

        Button actionBarBackButton = ( Button ) actionBarView.findViewById(R.id.left_button);
        TextView contentTitleTextView = ( TextView )actionBarView.findViewById(R.id.content_title);

        Typeface fontelloTypeface = FontsHelper.sharedHelper(this).fontello();

        actionBarBackButton.setText(R.string.icon_angle_left);
        actionBarBackButton.setTypeface(fontelloTypeface);
        actionBarBackButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.solutions_list);

        adapter = new SimpleSolutionsAdapter(this, solutions);

        adapter.setRecyclerView(recyclerView);
        adapter.setOnSolutionItemClickListener(this);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadingDialog = new LoadingDialog(this);

        if (savedInstanceState != null) {
            solutions = savedInstanceState.getParcelableArrayList(SAVED_SOLUTIONS);
        }

        if (type.equals(TYPE_HISTORY)) {

            contentTitleTextView.setText(R.string.history);

            OptimizeHIT.sendScreen(GAnalyticsScreenNames.HISTORY_SCREEN, null, null, null);

        } else if (type.equals(TYPE_PEER_FAVORITES)) {

            contentTitleTextView.setText(R.string.peer_favorites);

//            OptimizeHIT.sendScreen(GAnalyticsScreenNames.PEER_FAVORITES_SCREEN, null, null, null);

        } else if (type.equals(TYPE_SOLUTIONS)) {
            contentTitleTextView.setText(R.string.solutions);
        } else if (type.equals(TYPE_SUBCATEGORIES)) {
            contentTitleTextView.setText(R.string.subcategories);
        }

        adapter.setSolutions(solutions);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if( type.equals(TYPE_PEER_FAVORITES) ){
            ArrayList<Solution> simpleSolutions = adapter.getSolutions();
            outState.putParcelableArrayList(SAVED_SOLUTIONS, simpleSolutions );
        }
    }

    @Override
    protected void onResume() {
        reloadData();

        IntentFilter updateSolutionsFilter = new IntentFilter(APITalker.ACTION_NEW_SOLUTION_DATA);
        registerReceiver(solutionsUpdateReceiver, updateSolutionsFilter);

        IntentFilter updateHistoryFilter = new IntentFilter(APITalker.ACTION_UPDATE_HISTORY);
        registerReceiver(historyUpdateReceiver, updateHistoryFilter);

        IntentFilter updatePeerFavoritesFilter = new IntentFilter(APITalker.ACTION_UPDATE_PEER_FAVORITES);
        registerReceiver(peerFavoritesUpdateReceiver, updatePeerFavoritesFilter);

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(solutionsUpdateReceiver);
        unregisterReceiver(historyUpdateReceiver);
        unregisterReceiver(peerFavoritesUpdateReceiver);
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
        onBackPressed();
    }

    @Override
    public void onSolutionItemClick(Solution solution, int solutionPosition) {
        if (type.equals(TYPE_SUBCATEGORIES)) {
            Intent intent = new Intent(this, SimpleSolutionsListActivity.class);
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_SOLUTIONS_TYPE, SimpleSolutionsListActivity.TYPE_SOLUTIONS);
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_CATEGORY, getIntent().getParcelableExtra(EXTRA_CATEGORY));
            intent.putExtra(SimpleSolutionsListActivity.EXTRA_SUBCATEGORY, solution);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_left_in, R.anim.hold);
        } else {
            DBTalker
                    .sharedDB(this)
                    .saveHistory(solution.solutionId(), solution.title(), false);

            this.solutionPosition = solutionPosition;

            String callType;
            if (type.equals(TYPE_HISTORY)) {
                callType = APITalker.CALL_TYPES.CALL_TYPE_HISTORY;
            } else if (type.equals(TYPE_PEER_FAVORITES)) {
                callType = APITalker.CALL_TYPES.CALL_TYPE_FAVORITES;
            } else {
                callType = APITalker.CALL_TYPES.CALL_TYPE_BROWSE_SOLUTIONS;
            }

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
    }

    @Override
    public void solutionSuccess(int solutionId, String title, String html, String speech) {

        String accessMethod;
        if (type.equals(TYPE_HISTORY)) {
            accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_HISTORY;
        } else if (type.equals(TYPE_PEER_FAVORITES)) {
            accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_PEER_FAVORITES;
        } else {
            accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_BROWSE;
        }

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

    BroadcastReceiver solutionsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadData();
        }
    };

    private BroadcastReceiver historyUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (type.equals(TYPE_HISTORY)) {
                reloadData();
            }
        }
    };

     private BroadcastReceiver peerFavoritesUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (type.equals(TYPE_PEER_FAVORITES)) {
                reloadData();
            }
        }
    };

    private void reloadData() {

        Solution category = getIntent().getParcelableExtra(EXTRA_CATEGORY);
        Solution subCategory = getIntent().getParcelableExtra(EXTRA_SUBCATEGORY);

        if (type.equals(TYPE_HISTORY)) {
            solutions = DBTalker
                    .sharedDB(this)
                    .getHistory();
        } else if (type.equals(TYPE_PEER_FAVORITES)) {
            solutions = DBTalker
                    .sharedDB(this)
                    .getPeerFavorites();
        } else if (type.equals(TYPE_SOLUTIONS)) {
            solutions = DBTalker
                    .sharedDB(this)
                    .getSolutions(category.solutionId(), subCategory.solutionId());
        } else if (type.equals(TYPE_SUBCATEGORIES)) {
            solutions = DBTalker
                    .sharedDB(this)
                    .getSubCategories(category.solutionId());
        }

        adapter.setSolutions(solutions);
        adapter.notifyDataSetChanged();
    }

}
