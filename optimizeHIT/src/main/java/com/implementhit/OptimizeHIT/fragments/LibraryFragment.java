package com.implementhit.OptimizeHIT.fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.implementhit.OptimizeHIT.OptimizeHIT;
import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.activity.SuperActivity;
import com.implementhit.OptimizeHIT.adapter.LibraryListAdapter;
import com.implementhit.OptimizeHIT.adapter.listeners.OnLibraryItemClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.viewholders.SearchGhostViewHolder;
import com.implementhit.OptimizeHIT.analytics.GAnalyticsScreenNames;
import com.implementhit.OptimizeHIT.api.APITalker;
import com.implementhit.OptimizeHIT.database.DBTalker;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;
import com.implementhit.OptimizeHIT.util.CustomEditText;
import com.implementhit.OptimizeHIT.util.DimensionUtil;
import com.implementhit.OptimizeHIT.util.FontsHelper;
import com.implementhit.OptimizeHIT.views.ExtendedLinearLayoutManager;

import java.util.ArrayList;

public class LibraryFragment extends Fragment implements View.OnClickListener, OnBackPressedListener,
        SearchGhostViewHolder.SearchHeaderProtocol, TextWatcher, OnSolutionItemClickListener, OnLibraryItemClickListener {

    public enum LibraryItem {
        SUGGESTED_LEARNING,
        FAVORITES,
        HISTORY,
        FIND_THE_CODE;

        private final int value;

        LibraryItem() {
            this.value = ordinal();
        }

    }

    private final String SAVE_SOLUTIONS = "saveSolutions";
    private final String SAVE_SCROLL_POSITION = "saveScrollPosition";
    private final String SAVE_SEARCH_HEIGHT = "saveSearchHeight";
    private final String SAVE_IS_SEARCHING = "saveIsSearching";
    private final String SAVE_CURSOR_POSITION = "saveCursorPosition";
    private final String SAVE_SEARCH_QUERY = "saveSearchQuery";

    User user;
    String userHash;
    DBTalker dbTalker;
    private LibraryFragmentListener listener;

    private View fragmentView;
    LinearLayout searchBar;
    private CustomEditText searchFieldCustomEditText;
    private LibraryListAdapter adapter;
    private ArrayList<Solution> solutions;
    private TextView clearButton;
    private RecyclerView solutionsList;
    private ExtendedLinearLayoutManager solutionListLinearLayoutManager;

    private boolean isSearching = false;

    private int scrolledPosition;
    private int searchBarHeight;

    private boolean needsOpenSearch = false;
    private String searchQuery;
    private int cursorPosition;

    private LibraryItem currentLibraryItem;

    OnSearchSolutionsListener searchSolutionsListener;

    public ArrayList<Solution> getSolutions() {
        return adapter.getSolutions();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        OptimizeHIT.sendScreen(GAnalyticsScreenNames.LIBRARY_SCREEN, null, null, null);
        fragmentView = inflater.inflate(R.layout.fragment_solution_list, container, false);

        searchBar = (LinearLayout) fragmentView.findViewById(R.id.solution_header);

        user = User.sharedUser(getActivity());
        userHash = user.hash();
        dbTalker = DBTalker.sharedDB(getActivity());

        if (savedInstanceState != null) {
            solutions = savedInstanceState.getParcelableArrayList(SAVE_SOLUTIONS);
            scrolledPosition = savedInstanceState.getInt(SAVE_SCROLL_POSITION, 0);
            searchBarHeight = savedInstanceState.getInt(SAVE_SEARCH_HEIGHT, 0);
            cursorPosition = savedInstanceState.getInt(SAVE_CURSOR_POSITION, -1);
            isSearching = savedInstanceState.getBoolean(SAVE_IS_SEARCHING, false);
            searchQuery = savedInstanceState.getString(SAVE_SEARCH_QUERY);
            needsOpenSearch = isSearching;
        } else {
            initSolutions();
        }

         initListView();

        getActivity()
                .getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
//                                fragmentView.requestLayout();

                                if (needsOpenSearch) {
                                    needsOpenSearch = false;
                                    prepareSearchBar(true);
                                }
                            }
                        });

        return fragmentView;
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(newSolutionDataReceiver);
        closeKeyboard();
    }

    @Override
    public void onResume() {

        IntentFilter intentFilter = new IntentFilter(APITalker.ACTION_NEW_SOLUTION_DATA);
        getActivity().registerReceiver(newSolutionDataReceiver, intentFilter);

        super.onResume();

        searchBar.bringToFront();

        if (scrolledPosition != 0) {
            searchBar.setY(-searchBarHeight);
        }

        // refresh the rating of solution after navigating back
        // from that solution page to the solutions list
        refreshSolutionsRating();

        if (SuperActivity.FROM_LOGGED_OUT) {
            Button cancelSearchButton = (Button) fragmentView.findViewById(R.id.cancel_search);
            cancelSearchButton.performClick();
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (fragmentView != null) {
            outState.putInt(SAVE_SEARCH_HEIGHT, searchBar == null ? 0 : searchBar.getMeasuredHeight());
            outState.putString(SAVE_SEARCH_QUERY, getSearchQuery());
            outState.putInt(SAVE_CURSOR_POSITION, getCursorPosition());
        }

        outState.putParcelableArrayList(SAVE_SOLUTIONS, solutions);
        outState.putInt(SAVE_SCROLL_POSITION, scrolledPosition);
        outState.putBoolean(SAVE_IS_SEARCHING, isSearching);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof LibraryFragmentListener) {
            listener = (LibraryFragmentListener) activity;
        } else if ( activity instanceof OnSearchSolutionsListener ){
            if( isSearching ){
                searchSolutionsListener.onHideRefreshButton();
            } else {
                searchSolutionsListener.onShowRefreshButton();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        listener = null;
    }

    @Override
    public boolean onBackPressed() {
        if (isSearching) {
            recoverSearchBar(false);
            return true;
        }

        return false;
    }

    /**
     * Solutions Helper
     */

    private void initSolutions() {
        solutions = dbTalker.getCategories();
        OptimizeHIT.sendScreen(GAnalyticsScreenNames.BROWSE_CATEGORIES_SCREEN, null, null, null);
    }

    /**
     * UI Helpers
     */

    @SuppressLint("ClickableViewAccessibility")
    private void initListView() {
        adapter = new LibraryListAdapter(getActivity(), solutions);
        adapter.setOnLibraryItemClickListener(this);
        adapter.setOnSolutionItemClickListener(this);
        adapter.setSearchHeaderProtocol(this);

        solutionListLinearLayoutManager = new ExtendedLinearLayoutManager(getActivity());
        solutionListLinearLayoutManager.setVerticalScrollListener(verticalScrollListener);

        solutionsList = (RecyclerView) fragmentView.findViewById(R.id.suggestions_list);

        adapter.setRecyclerView(solutionsList);

        searchFieldCustomEditText = (CustomEditText) fragmentView.findViewById(R.id.search_field);

        searchFieldCustomEditText.setOnHideKeyboardListener(searchHideKeyboardListener);

        RelativeLayout searchPanel = (RelativeLayout) fragmentView.findViewById(R.id.search_panel);
        searchPanel.setVisibility(View.VISIBLE);
        searchPanel.setOnClickListener(this);
        Button cancelSearchButton = (Button) fragmentView.findViewById(R.id.cancel_search);
        cancelSearchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (!isSearching) {
                    return;
                }

                OptimizeHIT.sendScreen(
                        GAnalyticsScreenNames.BROWSE_CATEGORIES_SCREEN,
                        null, null, null);
                recoverSearchBar(false);

            }
        });

        final Button searchFieldGhostButton = (Button) fragmentView.findViewById(R.id.search_field_ghost);
        searchFieldGhostButton.setOnClickListener(this);

        Typeface fontello = FontsHelper.sharedHelper(getActivity()).fontello();

        TextView searchIcon = (TextView) fragmentView.findViewById(R.id.search_icon);
        searchIcon.setTypeface(fontello);
        clearButton = (Button) fragmentView.findViewById(R.id.clear_icon);
        clearButton.setTypeface(fontello);
        clearButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!isSearching
                        || searchFieldCustomEditText.getText().length() == 0) {
                    return;
                }

                searchFieldCustomEditText.setText("");
            }
        });

        searchFieldCustomEditText.addTextChangedListener(this);

        solutionsList.setAdapter(adapter);
        solutionsList.setLayoutManager(solutionListLinearLayoutManager);
        solutionsList.setOverScrollMode(ListView.OVER_SCROLL_NEVER);

        if (scrolledPosition != 0) {
            solutionsList.scrollToPosition(scrolledPosition);
        }

        if (isSearching) {
            prepareSearchBar(true);
        }
    }

    CustomEditText.OnHideKeyboardListener searchHideKeyboardListener = new CustomEditText.OnHideKeyboardListener() {
        @Override
        public void onHideKeyboard() {
            searchFieldCustomEditText.setFocusable(false);
            searchFieldCustomEditText.setFocusableInTouchMode(false);
        }
    };

    private void setupNoSolutionsBar() {
        boolean forceClose = searchQuery == null || searchQuery.isEmpty();

        View noData;

        noData = fragmentView.findViewById(R.id.no_data);

        if (solutions.size() != 0 || forceClose) {
            noData.setVisibility(View.GONE);
        } else {
            // We do so  because we want the search bar come visible, when there is no search results
            searchBar.setY(0);

            noData.setVisibility(View.VISIBLE);
        }
    }

    private void toggleSearchingText(boolean show) {
        TextView searching = (TextView) fragmentView.findViewById(R.id.searching);
        searching.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void closeKeyboard() {
        if (getActivity() != null) {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(searchFieldCustomEditText.getWindowToken(), 0);

        }
    }

    public String getSearchQuery() {
        return searchFieldCustomEditText.getText().toString();
    }

    /**
     * OnItemClickListener Methods
     */

    @Override
    public void onSolutionItemClick(Solution solution, int solutionPosition) {

        if (solutions.isEmpty()) {
            return;
        }

        if (System.currentTimeMillis() - SuperActivity.savedLastClickTime < 200) {
            return;
        }

        SuperActivity.savedLastClickTime = System.currentTimeMillis();

        if (!isSearching) {
            if (solution.isSkipsSubcategory()) {
                listener.openAlphabeticalSolutions(solution);
            } else {
                listener.openSubCategory(solution);
            }
        } else {
            String solutionType = APITalker.CALL_TYPES.CALL_TYPE_BROWSE_SOLUTIONS;
            listener.openSolution(solution, solutionType);
        }

    }

    @Override
    public void onLibraryItemClick(LibraryItem libraryItem) {
        listener.openLibraryItem(libraryItem);
        currentLibraryItem = libraryItem;
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

        if ((view.getId() == R.id.search_panel
                || view.getId() == R.id.search_field_ghost)
                && !isSearching) {
            /*OptimizeHIT.sendScreen(
                    GAnalyticsScreenNames.SEARCH_SOLUTIONS_SCREEN,
                    null,
                    null);*/
            prepareSearchBar(false);
        }
    }

    /**
     * TextWatcher Methods
     */

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence sequence, int start, int before, int count) {

        if (!isSearching) {
            return;
        }

        searchQuery = sequence.toString();

        if (searchQuery.length() != 0) {
            solutions = dbTalker.searchSolutions(searchQuery);
        } else {
            // We do so  because we want the search bar come visible, when there is no search results
            if (searchBar != null) {
                searchBar.setY(0);
            }

            solutions = new ArrayList<>();
        }

        setupNoSolutionsBar();
        toggleSearchingText(solutions.size() == 0 && sequence.length() == 0);

        if (clearButton != null) {
            if (searchQuery.isEmpty()) {
                clearButton.setVisibility(View.GONE);
            } else {
                clearButton.setVisibility(View.VISIBLE);
            }
        }

        if (solutionsList != null) {
            solutionsList.setDescendantFocusability(ListView.FOCUS_AFTER_DESCENDANTS);
        }

        if (adapter != null) {
            adapter.setSolutions(solutions);
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    /**
     * Search Helpers
     */

    public void prepareSearchBar(boolean force) {

        if( searchSolutionsListener != null ) {
            searchSolutionsListener.onHideRefreshButton();
        }

        adapter.setSearching(true);
        isSearching = true;

        if (force) {
            forceSearchBarShown();
        } else {
            animateSearchBarShown();
        }

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, 0);

        clearButton = (Button) fragmentView.findViewById(R.id.clear_icon);

        if (searchQuery == null || searchQuery.isEmpty()) {
            clearButton.setVisibility(View.GONE);
        } else {
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    private void forceSearchBarShown() {
        final Button cancelSearchButton = (Button) fragmentView.findViewById(R.id.cancel_search);
        final Button searchFieldGhostButton = (Button) fragmentView.findViewById(R.id.search_field_ghost);
        final RelativeLayout searchBar = (RelativeLayout) fragmentView.findViewById(R.id.search_bar);
        final RelativeLayout searchPanel = (RelativeLayout) fragmentView.findViewById(R.id.search_panel);
        final LinearLayout searchControls = (LinearLayout) fragmentView.findViewById(R.id.search_controls);

        ViewGroup.LayoutParams searchBarLayoutParams = searchBar.getLayoutParams();
        searchBarLayoutParams.width = 2 * searchPanel.getMeasuredWidth() / 3;
        searchBar.setLayoutParams(searchBarLayoutParams);
        searchBar.requestLayout();

        ViewGroup.LayoutParams cancelSearchButtonLayoutParams = cancelSearchButton.getLayoutParams();
        cancelSearchButtonLayoutParams.width = searchPanel.getMeasuredWidth() / 3 - (int) DimensionUtil.dpToPx(15, getResources());
        cancelSearchButton.setLayoutParams(cancelSearchButtonLayoutParams);
        cancelSearchButton.requestLayout();

        searchFieldGhostButton.setVisibility(View.GONE);
        searchFieldCustomEditText.setVisibility(View.VISIBLE);
        searchFieldCustomEditText.setText(searchQuery);
        searchFieldCustomEditText.requestFocus();
        searchFieldCustomEditText.setSelection(cursorPosition);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) searchControls.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
    }

    private void animateSearchBarShown() {
        final Button cancelSearchButton = (Button) fragmentView.findViewById(R.id.cancel_search);
        final Button searchFieldGhostButton = (Button) fragmentView.findViewById(R.id.search_field_ghost);
        final RelativeLayout searchBar = (RelativeLayout) fragmentView.findViewById(R.id.search_bar);
        final LinearLayout searchControls = (LinearLayout) fragmentView.findViewById(R.id.search_controls);

        final int endValue = 4 * solutionsList.getWidth() / 5;
        ValueAnimator barWidthAnimator = ValueAnimator.ofInt(solutionsList.getWidth(), endValue - (int) DimensionUtil.dpToPx(7, getResources()));

        barWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                int value = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = searchBar.getLayoutParams();
                layoutParams.width = value;
                searchBar.setLayoutParams(layoutParams);
                searchBar.requestLayout();

                if (value <= endValue) {

                    searchFieldGhostButton.setVisibility(View.GONE);
                    searchFieldCustomEditText.setVisibility(View.VISIBLE);
                    searchFieldCustomEditText.setText("");
                    searchFieldCustomEditText.requestFocus();

                }

            }

        });

        barWidthAnimator.setDuration(250);
        barWidthAnimator.start();

        ValueAnimator cancelWidthAnimator = ValueAnimator.ofInt(0, solutionsList.getWidth() / 3 - (int) DimensionUtil.dpToPx(50, getResources()));
        cancelWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (Integer) animation.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = cancelSearchButton.getLayoutParams();
                layoutParams.width = val;
                cancelSearchButton.setLayoutParams(layoutParams);
                cancelSearchButton.requestLayout();
            }
        });
        cancelWidthAnimator.setDuration(250);
        cancelWidthAnimator.start();

        searchControls.animate().setDuration(250);
        searchControls.animate().x(searchBar.getLeft() + searchBar.getPaddingLeft());
        searchControls.animate().start();
        searchControls.invalidate();    }


    private void recoverSearchBar(boolean force) {

        if( searchSolutionsListener != null ) {
            searchSolutionsListener.onShowRefreshButton();
        }

        adapter.setSearching(false);
        isSearching = false;

        closeKeyboard();

        RelativeLayout searchPanel = (RelativeLayout) fragmentView.findViewById(R.id.search_panel);
        final Button searchFieldGhostButton = (Button) fragmentView.findViewById(R.id.search_field_ghost);
        final Button cancelSearchButton = (Button) fragmentView.findViewById(R.id.cancel_search);
        final RelativeLayout searchBar = (RelativeLayout) fragmentView.findViewById(R.id.search_bar);
        LinearLayout searchControls = (LinearLayout) fragmentView.findViewById(R.id.search_controls);

        if (force) {
            searchBar.getLayoutParams().width = searchPanel.getMeasuredWidth();
            searchBar.requestLayout();
            cancelSearchButton.getLayoutParams().width = 0;
            cancelSearchButton.requestLayout();
            searchControls.setX((searchPanel.getMeasuredWidth() - searchControls.getMeasuredWidth()) / 2);
            searchFieldGhostButton.setVisibility(View.VISIBLE);
            searchFieldCustomEditText.setVisibility(View.GONE);
        } else {
            final int endValue = searchPanel.getMeasuredWidth();
            ValueAnimator barWidthAnimator = ValueAnimator.ofInt(
                    2 * searchPanel.getMeasuredWidth() / 3 - (int) DimensionUtil.dpToPx(15, getResources()),
                    searchPanel.getMeasuredWidth());
            barWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = searchBar.getLayoutParams();
                    layoutParams.width = value;
                    searchBar.setLayoutParams(layoutParams);
                    searchBar.requestLayout();

                    if (value >= endValue) {
                        searchFieldGhostButton.setVisibility(View.VISIBLE);
                        searchFieldCustomEditText.setVisibility(View.GONE);
                    }
                }
            });
            barWidthAnimator.setDuration(250);
            barWidthAnimator.start();
            ValueAnimator cancelWidthAnimator = ValueAnimator.ofInt(
                    searchPanel.getMeasuredWidth() / 3 - (int) DimensionUtil.dpToPx(15, getResources()), 0);
            cancelWidthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int val = (Integer) animation.getAnimatedValue();
                    ViewGroup.LayoutParams layoutParams = cancelSearchButton.getLayoutParams();
                    layoutParams.width = val;
                    cancelSearchButton.setLayoutParams(layoutParams);
                    cancelSearchButton.requestLayout();
                }
            });
            cancelWidthAnimator.setDuration(250);
            cancelWidthAnimator.start();

            searchControls.animate().setDuration(250);
            searchControls.animate().x((searchPanel.getMeasuredWidth() - searchControls.getMeasuredWidth()) / 2);
            searchControls.animate().start();
        }

        searchFieldCustomEditText.setText("");
        clearButton = (Button) fragmentView.findViewById(R.id.clear_icon);
        clearButton.setVisibility(View.INVISIBLE);

        recoverFromSearch();
    }

    private void recoverFromSearch() {
        solutions = dbTalker.getCategories();

        setupNoSolutionsBar();
        toggleSearchingText(false);

        solutionsList.setAdapter(null);
        adapter.setSolutions(solutions);
        solutionsList.setAdapter(adapter);
    }

    public int getCursorPosition() {
        return searchFieldCustomEditText.getSelectionEnd();
    }

    @Override
    public int getSearchBarHeight() {
        return searchBar.getMeasuredHeight();
    }

    private ExtendedLinearLayoutManager.VerticalScrollListener verticalScrollListener = new ExtendedLinearLayoutManager.VerticalScrollListener() {

        @Override
        public boolean onScrolledBy(int dy) {

            View noDataToDisplay = getView().findViewById(R.id.no_data);
            boolean noDataIsVisible = noDataToDisplay.getVisibility() == View.VISIBLE;
            boolean noNeedToScroll = solutionListLinearLayoutManager.findLastCompletelyVisibleItemPosition() == adapter.getItemCount() - 1;

            if( ( adapter.getItemCount() - 1  == 0  &&  !noDataIsVisible ) || noNeedToScroll ){
                scrolledPosition = solutionListLinearLayoutManager.findFirstVisibleItemPosition();
                return true;
            }

            if ( dy > 0  &&  Math.abs(searchBar.getY()) < searchBar.getMeasuredHeight() ) {
                searchBar.setY(searchBar.getY() - dy);

                if (Math.abs(searchBar.getY()) > searchBar.getMeasuredHeight()) {
                    searchBar.setY(-searchBar.getMeasuredHeight());
                }
            } else if (dy < 0 && searchBar.getY() < 0 && solutionListLinearLayoutManager.findFirstVisibleItemPosition() == 0) {
                searchBar.setY(searchBar.getY() - dy);

                if (searchBar.getY() > 0) {
                    searchBar.setY(0);
                }
            }

            scrolledPosition = solutionListLinearLayoutManager.findFirstVisibleItemPosition();

            return true;
        }

    };

    /**
     * Listeners
     */

    public interface LibraryFragmentListener {
        void openSubCategory(Solution solution);
        void openSolution(Solution solution, String solutionType);
        void openAlphabeticalSolutions(Solution category);
        void openLibraryItem(LibraryItem libraryItem);
    }

    /**
     * Refresh Data Methods
     */

    public void refreshData() {
        initSolutions();
        adapter.setSolutions(solutions);
        adapter.notifyDataSetChanged();

        if (solutionsList.getAdapter() == null) {
            solutionsList.setAdapter(adapter);
        }

        setupNoSolutionsBar();
    }

    // Refresh solution rating, if it was changed, when user clicks some search result item,
    // and then set the rating of that item. After that, when user navigates back to the search results list,
    // we must refresh the rating of that item.
    public void refreshSolutionsRating(){

        if ( searchQuery != null && searchQuery.length() != 0 ) {
            solutions = dbTalker.searchSolutions(searchQuery);
        }

        adapter.setSolutions(solutions);
        adapter.notifyDataSetChanged();

        if (solutionsList.getAdapter() == null) {
            solutionsList.setAdapter(adapter);
        }

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


    public interface OnSearchSolutionsListener{
        void onHideRefreshButton();
        void onShowRefreshButton();
    }

    public void setOnSearchSolutionsListener( OnSearchSolutionsListener searchSolutionsListener ){
        this.searchSolutionsListener = searchSolutionsListener;
    }

}