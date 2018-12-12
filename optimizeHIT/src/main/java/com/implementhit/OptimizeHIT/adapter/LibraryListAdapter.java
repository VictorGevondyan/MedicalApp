package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnLibraryItemClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.SolutionProvider;
import com.implementhit.OptimizeHIT.adapter.providers.BadgeCountProvider;
import com.implementhit.OptimizeHIT.adapter.viewholders.LibraryViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SearchGhostViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SectionHeaderViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SimpleSolutionViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.fragments.LibraryFragment;
import com.implementhit.OptimizeHIT.models.Solution;
import com.implementhit.OptimizeHIT.models.User;

import java.util.ArrayList;
import java.util.Locale;

public class LibraryListAdapter extends RecyclerView.Adapter<SuperViewHolder> implements
		SolutionProvider, LibraryViewHolder.LibraryItemProvider, SectionHeaderViewHolder.SectionHeaderProvider, OnItemViewClickListener,
		BadgeCountProvider {
	public static final int TYPE_SOLUTION = 1;
	private static final int TYPE_SECTION_HEADER = 2;
	private static final int TYPE_SEARCH_HEADER = 4;
	private static final int TYPE_LIBRARY = 5;

	private static final int LIBRARY_ITEMS_COUNT = 4;

	private OnSolutionItemClickListener onSolutionItemClickListener;
	private OnLibraryItemClickListener onLibraryItemClickListener;
	private SearchGhostViewHolder.SearchHeaderProtocol searchHeaderProtocol;

	private LayoutInflater inflater;
	private RecyclerView recyclerView;
	private Context context;
	private ArrayList<Solution> solutions;

	private boolean isSearching;

	private String[] libraryIcons;
	private String[] libraryTitles;

    /**
     *  Adapter's methods
     */

	public LibraryListAdapter(Context context, ArrayList<Solution> solutions) {
		super();

		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.solutions = solutions;
		this.context = context;

		libraryTitles = context.getResources().getStringArray(R.array.library_titles);
		libraryIcons = context.getResources().getStringArray(R.array.library_icons);
	}

	public void setSearching(boolean searching) {
		isSearching = searching;
	}

	public ArrayList<Solution> getSolutions() {
		return solutions;
	}

	public void setSolutions(ArrayList<Solution> solutions) {
		this.solutions = solutions;
		notifyDataSetChanged();
	}

    public Solution getItem(int position) {
		if (isSearching) {
			return solutions.get(position - 1);
		}

        return solutions.get(position - LIBRARY_ITEMS_COUNT - 2);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

	public void setSearchHeaderProtocol(SearchGhostViewHolder.SearchHeaderProtocol searchHeaderProtocol) {
		this.searchHeaderProtocol = searchHeaderProtocol;
	}

	public void setOnSolutionItemClickListener(OnSolutionItemClickListener onSolutionItemClickListener) {
		this.onSolutionItemClickListener = onSolutionItemClickListener;
	}

	public void setOnLibraryItemClickListener(OnLibraryItemClickListener onLibraryItemClickListener) {
		this.onLibraryItemClickListener = onLibraryItemClickListener;
	}

	@Override
	public SuperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_SOLUTION) {
			View view = inflater.inflate(R.layout.item_simple_solution, parent, false);
			return new SimpleSolutionViewHolder(view, context, this, this);
		}
		if (viewType == TYPE_SECTION_HEADER) {
			View view = inflater.inflate(R.layout.item_section_header, parent, false);
			return new SectionHeaderViewHolder(view, this);
		}
		if (viewType == TYPE_SEARCH_HEADER) {
			LinearLayout view = new LinearLayout(context);
			view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			return new SearchGhostViewHolder(view, searchHeaderProtocol);
		}
		if (viewType == TYPE_LIBRARY) {
			View view = inflater.inflate(R.layout.item_library, parent, false);
			return new LibraryViewHolder(view, context, this,this, this);
		}

		return null;
	}

	@Override
	public void onBindViewHolder(SuperViewHolder holder, int position) {
		holder.processPosition(position);
	}

	@Override
	public int getItemCount() {
		if (isSearching) {
			return solutions.size() + 1;
		}

		return solutions.size() + LIBRARY_ITEMS_COUNT + 2;
	}

	@Override
	public int getItemViewType(int position) {
		if (position == 0) {
			return TYPE_SEARCH_HEADER;
		}

		if (isSearching) {
			return TYPE_SOLUTION;
		}

		if (position <= LIBRARY_ITEMS_COUNT) {
			return TYPE_LIBRARY;
		}
		if (position == LIBRARY_ITEMS_COUNT + 1) {
			return TYPE_SECTION_HEADER;
		}

		return TYPE_SOLUTION;
	}

	@Override
	public void onItemViewClick(View itemView, SuperViewHolder viewHolder) {
		if (viewHolder instanceof LibraryViewHolder) {
			int position = recyclerView.getChildAdapterPosition(itemView) - 1;

			LibraryFragment.LibraryItem[] libraryItemValues = LibraryFragment.LibraryItem.values();
			onLibraryItemClickListener.onLibraryItemClick(libraryItemValues[position]);
		} else if (viewHolder instanceof SimpleSolutionViewHolder) {
			int position = recyclerView.getChildAdapterPosition(itemView);
			int solutionPosition = isSearching ? position - 1 : position - LIBRARY_ITEMS_COUNT - 2;
			Solution solution = getItem(position);

			onSolutionItemClickListener.onSolutionItemClick(solution, solutionPosition);
		}
	}

	@Override
	public Solution solutionForPosition(int position) {
		return getItem(position);
	}

	@Override
	public String libraryIconForPosition(int position) {
		return libraryIcons[position - 1];
	}

	@Override
	public String libraryTitleForPosition(int position) {
		return libraryTitles[position - 1];
	}


	@Override
	public String sectionHeaderForPosition(int position) {
		return context.getString(R.string.categories).toUpperCase(Locale.US);
	}

	@Override
	public int getBadgeCount(int position ) {
		// if position equals 1, it means, that it is the position of suggested learnings item in Library List, so we should process
		// unead suggested learnings badge
		if( position == LibraryListAdapter.TYPE_SOLUTION) {
			User user = User.sharedUser(context);
			int unreadSuggestedLearnings = user.reactivations();
			return unreadSuggestedLearnings;
		}

		return 0;
	}

}