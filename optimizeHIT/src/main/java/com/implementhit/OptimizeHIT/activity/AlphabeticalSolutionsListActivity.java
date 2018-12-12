package com.implementhit.OptimizeHIT.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.AlphabeticalSolutionsAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.api.SolutionRequestListener;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.util.NotificationHelper;

import java.util.ArrayList;

public class AlphabeticalSolutionsListActivity extends SuperActivity implements View.OnClickListener, OnSolutionItemClickListener, SolutionRequestListener {
    public static final String EXTRA_CATEGORY = "category";

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#";

    private AlphabeticalSolutionsAdapter adapter;
    private int solutionPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alphabetical_solutions_list);

        LinearLayout alphabeticalLinearLayout = (LinearLayout) findViewById(R.id.alphabet_container);
        int primaryColor = User.sharedUser(this).primaryColor();

        for (int index = 0 ; index < ALPHABET.length() ; index++) {
            String character = ALPHABET.substring(index, index + 1);
            Button characterButton = (Button) getLayoutInflater().inflate(R.layout.item_alphabet, alphabeticalLinearLayout, false);
            characterButton.setText(character);
            characterButton.setTextColor(primaryColor);

            alphabeticalLinearLayout.addView(characterButton);
        }

        adapter = new AlphabeticalSolutionsAdapter(this, new ArrayList<Solution>());

        Solution category = getIntent().getParcelableExtra(EXTRA_CATEGORY);
        ArrayList<Solution> solutions;

        if (savedInstanceState != null) {
            adapter.restoreState(savedInstanceState);
        } else {
            solutions = DBTalker
                    .sharedDB(this)
                    .getSolutions(category.solutionId(), -1);
            adapter.setSolutions(solutions);
        }

        View actionBarView = findViewById(R.id.action_bar);
        LinearLayout leftButtonLayout = (LinearLayout) actionBarView.findViewById(R.id.left_button_layout);
        Button actionBarBackButton = (Button) actionBarView.findViewById(R.id.left_button);
        TextView contentTitleTextView = (TextView) actionBarView.findViewById(R.id.content_title);

        contentTitleTextView.setText(R.string.solutions);

        Typeface fontelloTypeface = FontsHelper.sharedHelper(this).fontello();

        leftButtonLayout.setVisibility(View.VISIBLE);

        actionBarBackButton.setText(R.string.icon_angle_left);
        actionBarBackButton.setTypeface(fontelloTypeface);
        actionBarBackButton.setOnClickListener(this);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.solutions_list);

        adapter.setRecyclerView(recyclerView);
        adapter.setOnSolutionItemClickListener(this);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
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
        finish();
    }

    @Override
    public void onSolutionItemClick(Solution solution, int solutionPosition) {
        DBTalker
                .sharedDB(this)
                .saveHistory(solution.solutionId(), solution.title(), false);

        this.solutionPosition = solutionPosition;

        String callType = APITalker.CALL_TYPES.CALL_TYPE_BROWSE_SOLUTIONS;

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        adapter.saveState(outState);
    }

    @Override
    public void solutionSuccess(int solutionId, String title, String html, String speech) {
        String accessMethod = APITalker.ACCESS_METHODS.ACCESS_METHOD_BROWSE;

        Intent intent = new Intent(this, SolutionActivity.class);
        intent.putExtra(SolutionActivity.EXTRA_HTML, html);
        intent.putExtra(SolutionActivity.EXTRA_SPEECH, speech);
        intent.putExtra(SolutionActivity.EXTRA_SOLUTIONS, adapter.getSolutions());
        intent.putExtra(SolutionActivity.EXTRA_POSITION, solutionPosition);
        intent.putExtra(SolutionActivity.EXTRA_ACCESS_METHOD, accessMethod);
        startActivity(intent);
    }

    @Override
    public void solutionFailure(String error) {
        NotificationHelper.showNotification(error, this);
    }
}
