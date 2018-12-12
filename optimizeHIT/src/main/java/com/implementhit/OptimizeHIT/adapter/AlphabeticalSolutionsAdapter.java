package com.implementhit.OptimizeHIT.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.implementhit.OptimizeHIT.R;
import com.implementhit.OptimizeHIT.adapter.listeners.OnItemViewClickListener;
import com.implementhit.OptimizeHIT.adapter.listeners.OnSolutionItemClickListener;
import com.implementhit.OptimizeHIT.adapter.providers.SolutionProvider;
import com.implementhit.OptimizeHIT.adapter.viewholders.SectionHeaderViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SimpleSolutionViewHolder;
import com.implementhit.OptimizeHIT.adapter.viewholders.SuperViewHolder;
import com.implementhit.OptimizeHIT.models.Solution;

import java.util.ArrayList;

/**
 * Created by victor on 7/27/16.
 */
public class AlphabeticalSolutionsAdapter extends RecyclerView.Adapter<SuperViewHolder> implements OnItemViewClickListener, SolutionProvider, SectionHeaderViewHolder.SectionHeaderProvider {
    private static final int TYPE_SOLUTION = 1;
    private static final int TYPE_SECTION_HEADER = 2;

    private LayoutInflater inflater;
    private RecyclerView recyclerView;
    private Context context;
    private ArrayList<Solution> solutions;
    private OnSolutionItemClickListener solutionItemClickListener;

    /**
     *  Adapter's methods
     */

    public AlphabeticalSolutionsAdapter(Context context, ArrayList<Solution> solutions) {
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.solutions = solutions;

        setupSolutions();
    }

    private void setupSolutions() {
        if (solutions.isEmpty()) {
            return;
        }

        this.solutions.add(0, new Solution(-1, String.valueOf(solutions.get(0).title().charAt(0))));

        for (int index = 1 ; index < solutions.size() ; index++) {
            Solution currentSolution = solutions.get(index);
            Solution previousSolution = solutions.get(index - 1);

            if (currentSolution.title().charAt(0) != previousSolution.title().charAt(0)) {
                solutions.add(index, new Solution(-1, String.valueOf(currentSolution.title().charAt(0))));
            }
        }
    }

    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

    public void setSolutions(ArrayList<Solution> solutions) {
        this.solutions = solutions;
        setupSolutions();
        notifyDataSetChanged();
    }

    public Solution getItem(int position) {
        return solutions.get(position);
    }

    public void setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
    }

    public void setOnSolutionItemClickListener( OnSolutionItemClickListener solutionItemClickListener ){
        this.solutionItemClickListener = solutionItemClickListener;
    }

    public void saveState(Bundle outState) {
        outState.putParcelableArrayList("solutions", solutions);
    }

    public void restoreState(Bundle savedState) {
        solutions = savedState.getParcelableArrayList("solutions");
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

        return null;
    }

    @Override
    public int getItemViewType(int position) {
        if (solutions.get(position).solutionId() == -1) {
            return TYPE_SECTION_HEADER;
        } else {
            return TYPE_SOLUTION;
        }
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
    public void onItemViewClick(View itemView, SuperViewHolder viewHolder) {
        int position = recyclerView.getChildAdapterPosition(itemView);
        solutionItemClickListener.onSolutionItemClick( solutions.get(position), position);
    }

    @Override
    public Solution solutionForPosition(int position) {
        return getItem(position);
    }

    @Override
    public String sectionHeaderForPosition(int position) {
        return solutions.get(position).title();
    }
}
