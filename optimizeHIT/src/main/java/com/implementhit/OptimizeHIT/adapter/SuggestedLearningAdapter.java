package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.SolutionProvider;
import com.implementhit.OptimizeHIT.adapter.viewholders.EmptyHeaderViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SectionHeaderViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SimpleSolutionViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

/**
 * Created by victor on 7/28/16.
 */
public class SuggestedLearningAdapter extends RecyclerView.Adapter<SuperViewHolder> implements SectionHeaderViewHolder.SectionHeaderProvider,
        OnItemViewClickListener, SolutionProvider {

    public static final String REACTIOVATION_HEADER = "reactivationHeader";
    public static final String SUGGESTIONS_HEADER = "suggestionsHeader";
    public static final String LOCATION_BASED_HEADER = "locationBasedHeader";
    public static final String EMPTY_SUGGESTIONS_HEADER = "emptySuggestionsHeader";

    private static final int TYPE_SOLUTION = 1;
    private static final int TYPE_SECTION_HEADER = 2;
    private static final int TYPE_EMPTY_HEADER = 3;

    private OnSolutionItemClickListener listener;
    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private Context context;
    private ArrayList<Solution> solutions;

    public SuggestedLearningAdapter(Context context, ArrayList<Solution> solutions) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.solutions = solutions;
        this.context = context;
    }

    @Override
    public SuperViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_SOLUTION) {
            View view = inflater.inflate(R.layout.item_simple_solution, parent, false);
            return new SimpleSolutionViewHolder(view, context, this, this);
        }
        if (viewType == TYPE_EMPTY_HEADER) {
            View view = inflater.inflate(R.layout.item_no_suggestions, parent, false);
            return new EmptyHeaderViewHolder(view);
        }
        if (viewType == TYPE_SECTION_HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new SectionHeaderViewHolder(view, this);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(SuperViewHolder holder, int position) {
        holder.processPosition(position);
    }

    @Override
    public int getItemCount() {
        return solutions.size();
    }

    @Override
    public int getItemViewType(int position) {
        Solution solution = solutions.get(position);

        if (solution.title().equals(EMPTY_SUGGESTIONS_HEADER)) {
            return TYPE_EMPTY_HEADER;
        }
        if (solution.title().equals(LOCATION_BASED_HEADER)) {
            return TYPE_SECTION_HEADER;
        }
        if (solution.title().equals(REACTIOVATION_HEADER)) {
            return TYPE_SECTION_HEADER;
        }
        if (solution.title().equals(SUGGESTIONS_HEADER)) {
            return TYPE_SECTION_HEADER;
        }

        return TYPE_SOLUTION;
    }

    @Override
    public void onItemViewClick(View itemView, SuperViewHolder viewHolder) {
        int position = recyclerView.getChildAdapterPosition(itemView);
        listener.onSolutionItemClick(solutions.get(position), position);
    }

    @Override
    public String sectionHeaderForPosition(int position) {
        String title = solutions.get(position).title();
        int headerId = 0;

        if (title.equals(LOCATION_BASED_HEADER)) {
            headerId = R.string.based_on_current_location;
        }
        if (title.equals(REACTIOVATION_HEADER)) {
            headerId = R.string.reactivation_qiuck_overview;
        }
        if (title.equals(SUGGESTIONS_HEADER)) {
            headerId = R.string.suggestions;
        }

        return context.getString(headerId);
    }

    @Override
    public Solution solutionForPosition(int position) {
        return solutions.get(position);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setSolutions(ArrayList<Solution> solutions) {
        this.solutions = solutions;
        notifyDataSetChanged();
    }

    public void setOnSolutionItemClickListener(OnSolutionItemClickListener listener) {
        this.listener = listener;
    }

}